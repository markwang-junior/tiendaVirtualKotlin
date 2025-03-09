package com.markwang.tiendavirtualapp_kotlin

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale


class Constantes {

    fun obtenerTiempoD(): Long {
        return System.currentTimeMillis()
    }

    fun obtenerFecha(tiempo : Long) : String{
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = tiempo

        return  android.text.format.DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

     /*Agregar producto a la base de datos*/
    fun agregarProductoFav(context: Context, idProducto: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val tiempo = obtenerTiempoD()  // No necesitas hacer `Constantes().obtenerTiempoD()`

        val hashMap = HashMap<String, Any>()
        hashMap["idProducto"] = idProducto
        hashMap["idFav"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        firebaseAuth.uid?.let { uid ->  // Evita posibles null-pointer exceptions
            ref.child(uid).child("Favoritos").child(idProducto)
                .setValue(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(context, "Producto agregado a favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /*Eliminar producto de la base de datos*/
    fun eliminarProductoFav(context: Context, idProducto: String) {
        val firebaseAuth = FirebaseAuth.getInstance()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        firebaseAuth.uid?.let { uid ->  // Evita posibles null-pointer exceptions
            ref.child(uid).child("Favoritos").child(idProducto)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Producto eliminado de favoritos", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
