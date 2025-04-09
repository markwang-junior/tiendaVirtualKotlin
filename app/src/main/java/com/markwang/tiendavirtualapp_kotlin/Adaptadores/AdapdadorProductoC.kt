package com.markwang.tiendavirtualapp_kotlin.Adaptadores

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.DetalleProducto.DetalleProductoActivity
import com.markwang.tiendavirtualapp_kotlin.Filtro.FiltroProducto
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ItemProductoCBinding

class AdapdadorProductoC : RecyclerView.Adapter<AdapdadorProductoC.HolderProducto>, Filterable {

    private lateinit var binding : ItemProductoCBinding

    private var mContext : Context
    var productosArrayList : ArrayList<ModeloProducto>
    private var filtroLista : ArrayList<ModeloProducto>
    private var filtro : FiltroProducto ?= null
    private var firebaseAuth : FirebaseAuth

    constructor(mContext: Context, productosArrayList: ArrayList<ModeloProducto>) {
        this.mContext = mContext
        this.productosArrayList = productosArrayList
        this. filtroLista = productosArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProducto {
        binding = ItemProductoCBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProducto(binding.root)
    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }

    override fun onBindViewHolder(holder: HolderProducto, position: Int) {
        val modeloProducto = productosArrayList[position]

        val nombre = modeloProducto.nombre

        cargarPrimeraImg(modeloProducto, holder)
        visualizarDescuento(modeloProducto, holder)
        comprobarFavorito(modeloProducto, holder)

        holder.item_nombre_p.text = "${nombre}"

        // Evento al presionar en el imageButton Favorito
        holder.Ib_fav.setOnClickListener {
            val favorito = modeloProducto.favorito

            if (favorito) {
                // Favorito = true
                Constantes().eliminarProductoFav(mContext, modeloProducto.id)
            } else {
                // Favorito = false
                Constantes().agregarProductoFav(mContext, modeloProducto.id)
            }
        }

        // Evento para dirigirnos a la actividad de detalle
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, DetalleProductoActivity::class.java)
            intent.putExtra("idProducto", modeloProducto.id)
            mContext.startActivity(intent)
        }

        // Evento para agregar al carrito el producto seleccionado
        holder.agregar_carrito.setOnClickListener {
            verCarrito(modeloProducto)
        }
    }

    var costo : Double = 0.0
    var costoFinal : Double = 0.0
    var cantidadProd : Int = 0

    private fun verCarrito(modeloProducto: ModeloProducto) {
        // Declarar vistas
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
        var caracteristicasTv : TextView

        val dialog = Dialog(mContext)
        dialog.setContentView(R.layout.carrito_compras) // Conservamos el mismo nombre de archivo
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Fondo transparente para el dialog

        // Inicializar vistas
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
        caracteristicasTv = dialog.findViewById(R.id.caracteristicasPCar)

        /* Obtener los datos del modelo */
        val productoId = modeloProducto.id
        val nombre = modeloProducto.nombre
        val descripcion = modeloProducto.descripcion
        val precio = modeloProducto.precio
        val precioDesc = modeloProducto.precioDesc
        val notaDesc = modeloProducto.notaDesc
        val categoria = modeloProducto.categoria

        // Determinar características genéricas según la categoría o tipo de producto
        val caracteristicasTexto = StringBuilder()

        // Estas son características genéricas que pueden aplicarse a la mayoría de productos
        caracteristicasTexto.append("✅ Producto de alta calidad\n")
        caracteristicasTexto.append("✅ Diseño exclusivo\n")

        // Detectar tipo de producto por categoría o nombre
        when {
            categoria.contains("Camisa", ignoreCase = true)
                    || categoria.contains("Ropa", ignoreCase = true)
                    || nombre.contains("Camisa", ignoreCase = true) -> {
                caracteristicasTexto.append("✅ Material duradero\n")
                caracteristicasTexto.append("✅ Fácil de lavar")
            }
            categoria.contains("Funko", ignoreCase = true)
                    || nombre.contains("Funko", ignoreCase = true) -> {
                caracteristicasTexto.append("✅ Coleccionable oficial\n")
                caracteristicasTexto.append("✅ Edición limitada")
            }
            categoria.contains("Libro", ignoreCase = true)
                    || nombre.contains("Libro", ignoreCase = true) -> {
                caracteristicasTexto.append("✅ Acabado de calidad\n")
                caracteristicasTexto.append("✅ Envío protegido")
            }
            else -> {
                // Características genéricas adicionales para cualquier producto
                caracteristicasTexto.append("✅ Envío rápido\n")
                caracteristicasTexto.append("✅ Garantía incluida")
            }
        }

        caracteristicasTv.text = caracteristicasTexto.toString()

        if (!precioDesc.equals("0") && !notaDesc.equals("")) {
            /* El producto sí tiene descuento */
            notaDescTv.visibility = View.VISIBLE
            precioDescuentoTv.visibility = View.VISIBLE

            notaDescTv.text = notaDesc
            precioDescuentoTv.text = precioDesc.plus(" €") // 80 €
            precioOriginalTv.text = precio.plus(" €")
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Marca como tachado
            costo = precioDesc.toDouble() /* Precio almacena el precio con descuento */
        } else {
            /* El producto no tiene descuento */
            notaDescTv.visibility = View.GONE
            precioDescuentoTv.visibility = View.GONE

            precioOriginalTv.text = precio.plus(" €")
            precioOriginalTv.paintFlags = precioOriginalTv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Quitamos el tachado
            costo = precio.toDouble() /* precio almacena el precio original */
        }

        /* Setear la información */
        nombreTv.text = nombre

        // Descripción sin adornos específicos
        descripcionTv.text = descripcion

        costoFinal = costo
        cantidadProd = 1

        /* Incrementar cantidad */
        btnAumentar.setOnClickListener {
            costoFinal = costoFinal + costo
            cantidadProd++

            // Actualizar precio final con formato de moneda
            precioFinalTv.text = String.format("%.2f €", costoFinal)
            cantidadTv.text = cantidadProd.toString()
        }

        /* Disminuir cantidad */
        btnDisminuir.setOnClickListener {
            /* disminuir solo si la cantidad es mayor a 1 */
            if (cantidadProd > 1) {
                costoFinal = costoFinal - costo
                cantidadProd--

                // Actualizar precio final con formato de moneda
                precioFinalTv.text = String.format("%.2f €", costoFinal)
                cantidadTv.text = cantidadProd.toString()
            }
        }

        // Inicializar precio final con formato
        precioFinalTv.text = String.format("%.2f €", costo)

        /* Obtener primera imagen */
        cargarImg(productoId, imagenSIV)

        btnAgregarCarrito.setOnClickListener {
            agregarCarrito(mContext, modeloProducto, costoFinal, cantidadProd)
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun agregarCarrito(mContext: Context, modeloProducto: ModeloProducto, costoFinal: Double, cantidadProd: Int) {
        val firebaseAuth = FirebaseAuth.getInstance()
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
                Toast.makeText(mContext, "Se agregó al carrito el producto", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarImg(productoId: String, imagenSIV: ShapeableImageView) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(productoId).child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        //Extraer la url de la primera imagen*/
                        val imagenUrl = "${ds.child("imagenUrl").value}"

                        try {
                            Glide.with(mContext)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(imagenSIV)
                        }catch (e:Exception){
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error al cargar la imagen
                }
            })
    }

    private fun comprobarFavorito(modeloProducto: ModeloProducto, holder: AdapdadorProductoC.HolderProducto) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(modeloProducto.id)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favorito = snapshot.exists()
                    modeloProducto.favorito = favorito

                    if (favorito){
                        holder.Ib_fav.setImageResource(R.drawable.ico_favorito)
                    }else{
                        holder.Ib_fav.setImageResource(R.drawable.ico_no_favorito)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error en la base de datos
                }
            })
    }

    private fun visualizarDescuento(modeloProducto: ModeloProducto, holder: HolderProducto) {
        if (modeloProducto.precioDesc != "0" && modeloProducto.notaDesc.isNotEmpty()) {
            // El producto tiene descuento, mostrar vistas relacionadas
            holder.item_nota_p.visibility = View.VISIBLE
            holder.item_precio_p_desc.visibility = View.VISIBLE

            // Establecer valores
            holder.item_nota_p.text = modeloProducto.notaDesc
            holder.item_precio_p_desc.text = "${modeloProducto.precioDesc} €"
            holder.item_precio_p.text = "${modeloProducto.precio} €"

            // Aplicar tachado al precio original
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            // No tiene descuento, ocultar vistas de descuento
            holder.item_nota_p.visibility = View.GONE
            holder.item_precio_p_desc.visibility = View.GONE

            // Mostrar solo el precio original
            holder.item_precio_p.visibility = View.VISIBLE
            holder.item_precio_p.text = "${modeloProducto.precio} €"

            // Quitar tachado del precio original
            holder.item_precio_p.paintFlags = holder.item_precio_p.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }


    private fun cargarPrimeraImg(modeloProducto: ModeloProducto, holder: AdapdadorProductoC.HolderProducto) {
        val idProducto = modeloProducto.id

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes")
            .limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val imagenUrl = "${ds.child("imagenUrl").value}"

                        try {
                            Glide.with(mContext)
                                .load(imagenUrl)
                                .placeholder(R.drawable.item_img_producto)
                                .into(holder.imagenP)
                        }catch (e:Exception){
                            // Error al cargar imagen
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error en la base de datos
                }
            })
    }

    inner class HolderProducto(itemView : View) : RecyclerView.ViewHolder(itemView){
        var imagenP = binding.imagenP
        var item_nombre_p = binding.itemNombreP
        var item_precio_p = binding.itemPrecioP
        var item_precio_p_desc = binding.itemPrecioPDesc
        var item_nota_p = binding.itemNotaP
        var Ib_fav = binding.IbFav
        var agregar_carrito = binding.itemAgregarCarritoP
    }

    override fun getFilter(): Filter {
        if (filtro == null){
            filtro = FiltroProducto(this, filtroLista)
        }
        return filtro as FiltroProducto
    }
}