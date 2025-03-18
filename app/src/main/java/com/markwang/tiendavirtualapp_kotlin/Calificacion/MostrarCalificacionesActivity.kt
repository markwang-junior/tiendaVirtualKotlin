package com.markwang.tiendavirtualapp_kotlin.Calificacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorCalificacion
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloCalificacion
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityMostrarCalificacionesBinding

class MostrarCalificacionesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMostrarCalificacionesBinding
    private var idProducto = ""

    private lateinit var calificacionesArrayList : ArrayList<ModeloCalificacion>
    private lateinit var adapdadorCalificacion: AdapdadorCalificacion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMostrarCalificacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idProducto = intent.getStringExtra("idProducto").toString()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        listarCalificaciones(idProducto)

    }

    private fun listarCalificaciones(idProducto: String) {
        calificacionesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Productos/$idProducto/calificaciones")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                calificacionesArrayList.clear()
                for (ds in snapshot.children){
                    val modeloCalificacion = ds.getValue(ModeloCalificacion::class.java)
                    calificacionesArrayList.add(modeloCalificacion!!)
                }

                adapdadorCalificacion = AdapdadorCalificacion(this@MostrarCalificacionesActivity, calificacionesArrayList)
                binding.calificacionesRV.adapter = adapdadorCalificacion
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}