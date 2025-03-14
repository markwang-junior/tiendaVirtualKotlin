package com.markwang.tiendavirtualapp_kotlin.Cliente.Orden

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoOrden
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProductoOrden
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityDetalleOrdenCactivityBinding

class DetalleOrdenCActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleOrdenCactivityBinding
    private var idOrden = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList : ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdapter : AdapdadorProductoOrden

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenCactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: ""

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        datosOrden()
        direccionCliente()
        productosOrden()
    }

    private fun productosOrden(){
        productosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden).child("Productos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProductoOrden = ds.getValue(ModeloProductoOrden::class.java)
                    productosArrayList.add(modeloProductoOrden!!)
                }

                productoOrdenAdapter = AdapdadorProductoOrden(this@DetalleOrdenCActivity, productosArrayList)
                binding.ordenesRv.adapter = productoOrdenAdapter

                binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })






    }

    private fun direccionCliente(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val direccion = "${snapshot.child("direccion").value}"

                    if (direccion.isNotEmpty()){
                        //si el usuario registró su ubicación
                        binding.direccionOrdenD.text = direccion
                    }else  {
                        //si el usuario no registró su ubicación
                        binding.direccionOrdenD.text = "Registro su ubicación para continuar"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun datosOrden(){
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val idOrden = "${snapshot.child("idOrden").value}"
                val costo = "${snapshot.child("costo").value}"
                val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                val estadoOrden = "${snapshot.child("estadoOrden").value}"

                val fecha = Constantes().obtenerFecha(tiempoOrden.toLong())

                binding.idOrdenD.text = idOrden
                binding.fechaOrdenD.text = fecha
                binding.estadoOrdenD.text = estadoOrden
                binding.costoOrdenD.text = costo.plus(" €")

                if (estadoOrden.equals("Solicitud recibida")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.azul_marino_oscuro))
                }else if(estadoOrden.equals("En preparación")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.naranja))
                }else if (estadoOrden.equals("Entregado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.verde_oscuro2))
                }else if (estadoOrden.equals("Cancelado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenCActivity, R.color.rojo))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}