package com.markwang.tiendavirtualapp_kotlin.Vendedor.Productos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProducto
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityProductosCatVactivityBinding

class ProductosCatVActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProductosCatVactivityBinding
    private var nombreCat = ""

    private lateinit var productoArrayList : ArrayList<ModeloProducto>
    private lateinit var adapdadorProductos: AdapdadorProducto


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductosCatVactivityBinding.inflate((layoutInflater))
        setContentView(binding.root)

        nombreCat = intent.getStringExtra("nombreCat").toString()

        binding.txtProductoCat.text = "Categoria - ${nombreCat}"

        listarProductos(nombreCat)

    }

    private fun listarProductos(nombreCat : String) {
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.orderByChild("categoria").equalTo(nombreCat).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProducto = ds.getValue(ModeloProducto::class.java)
                    productoArrayList.add(modeloProducto!!)
                }
                adapdadorProductos = AdapdadorProducto(this@ProductosCatVActivity, productoArrayList)
                binding.productosRV.adapter = adapdadorProductos
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}