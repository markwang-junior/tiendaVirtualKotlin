package com.markwang.tiendavirtualapp_kotlin.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorCategoriaC
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoAleatorio
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloCategoria
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentTiendaCBinding

class FragmentTiendaC : Fragment() {

    private lateinit var binding : FragmentTiendaCBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var categoriaArrayList : ArrayList<ModeloCategoria>
    private lateinit var adapdadorCategoria : AdapdadorCategoriaC

    private lateinit var productoArrayList: ArrayList<ModeloProducto>
    private lateinit var adapdadorProducto : AdapdadorProductoAleatorio

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTiendaCBinding.inflate(LayoutInflater.from(mContext), container ,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        leerInfoCliente()
        listarCategorias()
        obtenerProductoAlea()
    }

    private fun leerInfoCliente(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    var direccion = "${snapshot.child("direccion").value}"

                    binding.bienvenidaTXT.setText("Bienvenido(a): ${nombres}")
                    binding.direccionTXT.setText("${direccion}")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun obtenerProductoAlea() {
        productoArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProducto = ds.getValue(ModeloProducto::class.java)
                    productoArrayList.add(modeloProducto!!)
                }

                val listaAleatoria = productoArrayList.shuffled().take(5)

                adapdadorProducto = AdapdadorProductoAleatorio(mContext, listaAleatoria)
                binding.productosAleatRV.adapter = adapdadorProducto
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun listarCategorias() {
        categoriaArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Categorias")
            .orderByChild("categoria")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriaArrayList.clear()
                for (ds in snapshot.children){
                    val modeloCat = ds.getValue(ModeloCategoria::class.java)
                    categoriaArrayList.add(modeloCat!!)
                }

                adapdadorCategoria = AdapdadorCategoriaC(mContext, categoriaArrayList)
                binding.categoriaRV.adapter = adapdadorCategoria
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })






    }
}