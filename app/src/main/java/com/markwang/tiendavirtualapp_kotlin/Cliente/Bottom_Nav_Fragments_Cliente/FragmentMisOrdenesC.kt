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
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorOrdenCompra
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloOrdenCompra
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentMisOrdenesCBinding

class FragmentMisOrdenesC : Fragment() {

    private lateinit var binding : FragmentMisOrdenesCBinding

    private lateinit var mContext : Context
    private lateinit var ordenesArrayList: ArrayList<ModeloOrdenCompra>
    private lateinit var ordenAdapdador : AdapdadorOrdenCompra

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMisOrdenesCBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verOrdenes()
    }

    private fun verOrdenes() {
        ordenesArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        val uid = FirebaseAuth.getInstance() .currentUser?.uid
        ref.orderByChild("ordenadoPor").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val modelo = ds.getValue(ModeloOrdenCompra::class.java)
                        ordenesArrayList.add(modelo!!)
                    }

                    ordenAdapdador = AdapdadorOrdenCompra(mContext, ordenesArrayList)
                    binding.ordenesRv.adapter = ordenAdapdador
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}