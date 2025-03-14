package com.markwang.tiendavirtualapp_kotlin.Calificacion

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityCalificarProductoBinding

class CalificarProductoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCalificarProductoBinding
    private var idProducto = ""
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificarProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        idProducto = intent.getStringExtra("idProducto").toString()
        firebaseAuth = FirebaseAuth.getInstance()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.IbCalificar.setOnClickListener{
            val opinion = binding.etOpinion.text.toString().trim()
            val rating = binding.ratingBar.rating

            if (rating <=0){
                /*Si el usuario no hizo una calificacion con estrellas*/
                Toast.makeText(this, "Por favor, seleccione una calificación antes de continuar", Toast.LENGTH_SHORT).show()
            }else if(opinion.isEmpty()){
                Toast.makeText(this, "Por favor, escriba una opinión", Toast.LENGTH_SHORT).show()
            }else{
                enviarCalificacion(idProducto, rating,opinion)
            }

        }


        cargarInfoProducto()
        cargarImgProducto()
        comprobarCalificacion(idProducto)
    }

    private fun comprobarCalificacion(idProducto: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        val uidUsuario = firebaseAuth.uid

        ref.child(idProducto).child("Calificaciones").get().addOnSuccessListener { dataSnapshot->
            var calificado = false

            for (calificacionSnapshot in dataSnapshot.children){
                val uidCliente = calificacionSnapshot.child("uidUsuario").getValue(String::class.java)

                if (uidCliente == uidUsuario){
                    calificado = true
                    break
                }
            }

            if (calificado){
                Toast.makeText(this, "Ya has calificado este producto",
                    Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "No has calificado este producto",
                    Toast.LENGTH_SHORT).show()

            }




        }.addOnFailureListener {
            Toast.makeText(this, "Error al comprobar las calificaciones: ${it.message}",
                Toast.LENGTH_SHORT).show()

        }




    }

    private fun enviarCalificacion(idProducto: String, rating : Float , opinion : String) {
        val ref = FirebaseDatabase.getInstance().getReference("Productos/$idProducto/calificaciones")
        val nuevaReseniaId = ref.push().key

        val resenia = mapOf(
            "idResenia" to nuevaReseniaId,
            "uidUsuario" to firebaseAuth.uid,
            "calificacion" to rating,
            "resenia" to opinion
        )
        if (nuevaReseniaId != null){
            ref.child(nuevaReseniaId).setValue(resenia).addOnCompleteListener{ task->
                if (task.isSuccessful){
                    Toast.makeText(this, "Reseña enviada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(this, "Error al agregar la reseña", Toast.LENGTH_SHORT).show()
                }
            }
        }



    }

    private fun cargarImgProducto() {
        val database = FirebaseDatabase.getInstance()
        val productoRef = database.getReference("Productos").child(idProducto).child("Imagenes")

        productoRef.orderByKey().limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val primeraImg = snapshot.children.firstOrNull()
                    val imagenUrl = primeraImg?.child("imagenUrl")?.getValue(String::class.java)

                    if (imagenUrl != null){
                        Glide.with(this@CalificarProductoActivity)
                            .load(imagenUrl)
                            .placeholder(R.drawable.item_img_producto)
                            .into(binding.imagenProducto)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun cargarInfoProducto() {
        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val modeloProducto = snapshot.getValue(ModeloProducto::class.java)
                    val nombre = modeloProducto?.nombre
                    binding.tvNombreP.text = nombre
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}