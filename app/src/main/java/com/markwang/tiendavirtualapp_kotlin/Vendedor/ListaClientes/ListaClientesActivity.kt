package com.markwang.tiendavirtualapp_kotlin.Vendedor.ListaClientes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorCliente
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloUsuario
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityListaClientesBinding

class ListaClientesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityListaClientesBinding

    private lateinit var clientesArrayList : ArrayList<ModeloUsuario>
    private lateinit var adapdadorCliente: AdapdadorCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listarCliente()

    }

    private fun listarCliente() {
        clientesArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.orderByChild("tipoUsuario").equalTo("cliente")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    clientesArrayList.clear()

                    for (ds in snapshot.children){
                        val modeloCliente = ds.getValue(ModeloUsuario::class.java)
                        clientesArrayList.add(modeloCliente!!)
                    }

                    adapdadorCliente = AdapdadorCliente(this@ListaClientesActivity, clientesArrayList)
                    binding.clienteRV.adapter = adapdadorCliente
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}