package com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorOrdenCompraV
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloOrdenCompra
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentOrdenesVBinding


class FragmentOrdenesV : Fragment() {

    private lateinit var binding : FragmentOrdenesVBinding
    private lateinit var mContext : Context

    private lateinit var ordenesArrayList : ArrayList<ModeloOrdenCompra>
    private lateinit var ordenAdapdador : AdapdadorOrdenCompraV

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOrdenesVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verOrdenes()
    }

    private fun verOrdenes() {
        ordenesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()
                for (ds in snapshot.children){
                    val modelo = ds.getValue(ModeloOrdenCompra::class.java)
                    ordenesArrayList.add(modelo!!)
                }

                ordenAdapdador = AdapdadorOrdenCompraV(mContext, ordenesArrayList)
                binding.ordenesRv.adapter = ordenAdapdador
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}