package com.markwang.tiendavirtualapp_kotlin.DetalleProducto

import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorImgSlider
import com.markwang.tiendavirtualapp_kotlin.Calificacion.CalificarProductoActivity
import com.markwang.tiendavirtualapp_kotlin.Calificacion.MostrarCalificacionesActivity
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloImgSlider
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityDetalleProductoBinding

class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleProductoBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private var idProducto = ""
    private var modeloProducto: ModeloProducto? = null

    private lateinit var imagenSlider : ArrayList<ModeloImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //obtenemos el id del producto enviado desde el adapdador
        idProducto = intent.getStringExtra("idProducto").toString()

        cargarImagenesProd()
        cargarInfoProducto()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvDejarCalificacion.setOnClickListener {
            val intent = Intent(this, CalificarProductoActivity::class.java)
            intent.putExtra("idProducto", idProducto)
            startActivity(intent)

        }

        binding.tvPromCal.setOnClickListener {
            val intent = Intent(this, MostrarCalificacionesActivity::class.java)
            intent.putExtra("idProducto", idProducto)
            startActivity(intent)
        }

        calcularPromedioCal(idProducto)

        // Añadir evento al botón de agregar al carrito
        binding.itemAgregarCarritoP.setOnClickListener {
            if (modeloProducto != null) {
                verCarrito(modeloProducto!!)
            } else {
                Toast.makeText(this, "Espere un momento, cargando producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcularPromedioCal(idProducto: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos/$idProducto/calificaciones")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var sumaCalificaciones = 0.0
                var totalCalificaciones = 0

                for (calificacionSn in snapshot.children){
                    // Cambiar Int por Float para coincidir con el tipo guardado
                    val calificacion = calificacionSn.child("calificacion").getValue(Float::class.java)

                    if (calificacion != null){
                        sumaCalificaciones += calificacion
                        totalCalificaciones++
                    }
                }

                if (totalCalificaciones > 0){
                    val promedio = sumaCalificaciones / totalCalificaciones

                    // Redondear a 1 decimal para mejor presentación
                    val promedioFormateado = String.format("%.1f", promedio)

                    binding.tvPromCal.text = "$promedioFormateado/5"
                    binding.tvTotalCal.text = "($totalCalificaciones)"
                    binding.ratingBar.rating = promedio.toFloat()
                } else {
                    binding.tvPromCal.text = getString(R.string.tvPromCal)
                    binding.tvTotalCal.text = getString(R.string.tvTotalCal)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error adecuadamente
                Toast.makeText(this@DetalleProductoActivity,
                    "Error al cargar calificaciones: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarInfoProducto() {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    modeloProducto = snapshot.getValue(ModeloProducto::class.java)

                    val nombre = modeloProducto?.nombre
                    val descripcion = modeloProducto?.descripcion
                    val precio = modeloProducto?.precio
                    val precioDesc = modeloProducto?.precioDesc
                    val notaDesc = modeloProducto?.notaDesc

                    binding.nombrePD.text = nombre
                    binding.descripcionPD.text = descripcion
                    binding.precioPD.text = precio.plus(" €")

                    if (!precioDesc.equals("")&& !notaDesc.equals("")){
                        /*Producto con descuento*/
                        binding.precioDescPD.text = precioDesc.plus(" €")
                        binding.notaDescPD.text = notaDesc

                        binding.precioPD.paintFlags = binding.precioPD.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }else{
                        binding.precioDescPD.visibility = View.GONE
                        binding.notaDescPD.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetalleProductoActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarImagenesProd() {
        imagenSlider = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    imagenSlider.clear()
                    for (ds in snapshot.children){
                        try {
                            val modeloImgSlider = ds.getValue(ModeloImgSlider::class.java)
                            imagenSlider.add(modeloImgSlider!!)
                        }catch (e:Exception){

                        }
                    }

                    val adapdadorImgSlider = AdapdadorImgSlider(this@DetalleProductoActivity, imagenSlider)
                    binding.imagenVP.adapter = adapdadorImgSlider
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetalleProductoActivity, "Error al cargar imágenes", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private var costo : Double = 0.0
    private var costoFinal : Double = 0.0
    private var cantidadProd : Int = 0

    // Función para mostrar el diálogo del carrito
    private fun verCarrito(modeloProducto: ModeloProducto) {
        var imagenSIV : ShapeableImageView
        var nombreTv : TextView
        var descripcionTv : TextView
        var notaDescTv : TextView
        var precioOriginalTv : TextView
        var precioDescuentoTv : TextView
        var precioFinalTv : TextView
        var btnDisminuir : ImageButton
        var cantidadTv : TextView
        var btnAumentar : ImageButton
        var btnAgregarCarrito : MaterialButton

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.carrito_compras)

        imagenSIV = dialog.findViewById(R.id.imagenPCar)
        nombreTv = dialog.findViewById(R.id.nombrePCar)
        descripcionTv = dialog.findViewById(R.id.descripcionPCar)
        notaDescTv = dialog.findViewById(R.id.notaDescPCar)
        precioOriginalTv = dialog.findViewById(R.id.precioOriginalPCar)
        precioDescuentoTv = dialog.findViewById(R.id.precioDescPCar)
        precioFinalTv = dialog.findViewById(R.id.precioFinalPCar)
        btnDisminuir = dialog.findViewById(R.id.btnDisminuir)
        cantidadTv = dialog.findViewById(R.id.cantidadPCar)
        btnAumentar = dialog.findViewById(R.id.btnAumentar)
        btnAgregarCarrito = dialog.findViewById(R.id.btnAgregarCarrito)

        val productoId = modeloProducto.id
        val nombre = modeloProducto.nombre
        val descripcion = modeloProducto.descripcion
        val precio = modeloProducto.precio
        val precioDesc = modeloProducto.precioDesc
        val notaDesc = modeloProducto.notaDesc

        if (!precioDesc.equals("0") && !notaDesc.equals("")){
            /*El producto si tiene descuento*/
            notaDescTv.visibility = View.VISIBLE
            precioDescuentoTv.visibility = View.VISIBLE

            notaDescTv.setText(notaDesc)
            precioDescuentoTv.setText(precioDesc.plus(" €"))
            precioOriginalTv.setText(precio.plus(" €"))
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            costo = precioDesc.toDouble()
        }else{
            precioOriginalTv.setText(precio.plus(" €"))
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            costo = precio.toDouble()
        }

        nombreTv.setText(nombre)
        descripcionTv.setText(descripcion)

        costoFinal = costo
        cantidadProd = 1

        btnAumentar.setOnClickListener {
            costoFinal = costoFinal +costo
            cantidadProd++

            precioFinalTv.text = costoFinal.toString()
            cantidadTv.text = cantidadProd.toString()
        }

        btnDisminuir.setOnClickListener {
            if (cantidadProd > 1){
                costoFinal = costoFinal-costo
                cantidadProd--

                precioFinalTv.text = costoFinal.toString()
                cantidadTv.text = cantidadProd.toString()
            }
        }

        precioFinalTv.text = costo.toString()

        // Cargar primera imagen
        cargarImg(productoId, imagenSIV)

        btnAgregarCarrito.setOnClickListener {
            agregarCarrito(modeloProducto, costoFinal, cantidadProd)
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun agregarCarrito(modeloProducto: ModeloProducto, costoFinal: Double, cantidadProd: Int) {
        val hashMap = HashMap<String, Any>()

        hashMap["idProducto"] = modeloProducto.id
        hashMap["nombre"] = modeloProducto.nombre
        hashMap["precio"] = modeloProducto.precio
        hashMap["precioDesc"] = modeloProducto.precioDesc
        hashMap["precioFinal"] = costoFinal.toString()
        hashMap["cantidad"] = cantidadProd

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras").child(modeloProducto.id)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Se agregó al carrito el producto", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImg(productoId: String, imagenSIV: ShapeableImageView) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(productoId).child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val imagenUrl = "${ds.child("imagenUrl").value}"

                        try {
                            Glide.with(this@DetalleProductoActivity)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(imagenSIV)
                        }catch (e:Exception){
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}