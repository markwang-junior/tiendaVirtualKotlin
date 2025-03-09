package com.markwang.tiendavirtualapp_kotlin.DetalleProducto

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorImgSlider
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloImgSlider
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityDetalleProductoBinding

class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleProductoBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private var idProducto = ""

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
    }

    private fun cargarInfoProducto() {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloProducto = snapshot.getValue(ModeloProducto::class.java)

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
                    TODO("Not yet implemented")
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
                    TODO("Not yet implemented")
                }
            })
    }
}