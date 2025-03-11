package com.markwang.tiendavirtualapp_kotlin.Vendedor.Orden

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityDetalleOrdenVactivityBinding

class DetalleOrdenVActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleOrdenVactivityBinding

    private var idOrden = ""
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList : ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdapdador : AdapdadorProductoOrden

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenVactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: ""

        datosOrden()
        productosOrden()

        binding.IbActualizarOrden.setOnClickListener {
            estadoOrdenMenu()
        }

    }

    private fun estadoOrdenMenu() {
        val popupMenu = PopupMenu(this, binding.IbActualizarOrden)

        popupMenu.menu.add(Menu.NONE, 0, 0, "Pedido entregada")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Pedido cancelada")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val itemId = item.itemId

            if (itemId == 0){
                //Marcar el pedido como entregado
                actualizarEstado("Entregado")
            }else if(itemId == 1){
                //Marcar el pedido cancelada
                actualizarEstado("Cancelado")
            }

            return@setOnMenuItemClickListener true

        }

    }

    private fun actualizarEstado(estado: String) {
        val hashMap = HashMap<String, Any>()
        hashMap["estadoOrden"] = estado

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden)
        ref.updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(this, "El estado del pedido ha pasado a: ${estado}", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                Toast.makeText(this, "Ha ocurrido un error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun productosOrden(){
        productosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden).child("Productos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProductoOrden = ds.getValue(ModeloProductoOrden::class.java)
                    productosArrayList.add(modeloProductoOrden!!)
                }

                productoOrdenAdapdador = AdapdadorProductoOrden(this@DetalleOrdenVActivity, productosArrayList)
                binding.ordenesRv.adapter = productoOrdenAdapdador

                binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })






    }

    private fun direccionCliente(uidCliente : String){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidCliente)
            .addValueEventListener(object : ValueEventListener {
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
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val idOrden = "${snapshot.child("idOrden").value}"
                val costo = "${snapshot.child("costo").value}"
                val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                val estadoOrden = "${snapshot.child("estadoOrden").value}"

                val fecha = Constantes().obtenerFecha(tiempoOrden.toLong())

                binding.idOrdenD.text = idOrden
                binding.fechaOrdenD.text = fecha
                binding.estadoOrdenD.text = estadoOrden
                binding.costoOrdenD.text = costo

                if (estadoOrden.equals("Solicitud recibida")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenVActivity, R.color.azul_marino_oscuro))
                }else if(estadoOrden.equals("En preparación")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenVActivity, R.color.naranja))
                }else if (estadoOrden.equals("Entregado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenVActivity, R.color.verde_oscuro2))
                }else if (estadoOrden.equals("Cancelado")){
                    binding.estadoOrdenD.setTextColor(ContextCompat.getColor(this@DetalleOrdenVActivity, R.color.rojo))
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}