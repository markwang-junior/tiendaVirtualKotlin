package com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProducto
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentMisProductosVBinding
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentProductosVBinding

class FragmentProductosV : Fragment() {

    private lateinit var binding : FragmentProductosVBinding
    private lateinit var mContext : Context

    private lateinit var productoArrayList : ArrayList<ModeloProducto>
    private lateinit var adapdadorProductos: AdapdadorProducto

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable : Runnable?= null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductosVBinding.inflate(inflater, container, false   )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listarProductos()

        binding.etBuscarProducto.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(nombreP: CharSequence?, start: Int, before: Int, count: Int) {
                val nombreProducto = nombreP.toString()

                searchRunnable = Runnable {
                    if (nombreProducto.isNotEmpty()){
                        /*Buscar un producto*/
                        buscarProducto(nombreProducto)
                    }else{
                        /*MOstrar todos los productos*/
                        listarProductos()
                    }
                }
                
                handler.postDelayed(searchRunnable!!, 1000)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun buscarProducto(nombreProducto: String) {
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
            .orderByChild("nombre").startAt(nombreProducto).endAt(nombreProducto +"\uf8ff")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    /*Si el producto existe*/
                    for (ds in snapshot.children){
                        val producto = ds.getValue(ModeloProducto::class.java)
                        productoArrayList.add(producto!!)
                    }

                    adapdadorProductos = AdapdadorProducto(mContext, productoArrayList)
                    binding.productosRV.adapter = adapdadorProductos
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })



    }

    private fun listarProductos() {
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProducto = ds.getValue(ModeloProducto::class.java)
                    productoArrayList.add(modeloProducto!!)
                }
                adapdadorProductos = AdapdadorProducto(mContext, productoArrayList)
                binding.productosRV.adapter = adapdadorProductos
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}