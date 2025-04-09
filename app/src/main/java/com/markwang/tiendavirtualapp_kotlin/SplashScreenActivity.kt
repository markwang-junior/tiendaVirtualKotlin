package com.markwang.tiendavirtualapp_kotlin

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Cliente.MainActivityCliente
import com.markwang.tiendavirtualapp_kotlin.Vendedor.MainActivityVendedor


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "TiendaVirtualPrefs"
    private val USER_TYPE_KEY = "user_type"
    private val USER_ID_KEY = "user_id"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        verBienvenida()
    }

    private fun verBienvenida() {
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // No hacemos nada durante el tick
            }

            override fun onFinish() {
                comprobarTipoUsuario()
            }
        }.start()
    }

    private fun comprobarTipoUsuario() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            // No hay usuario autenticado, ir a la selección de tipo
            Log.d("SplashScreen", "No hay usuario autenticado")
            startActivity(Intent(this, SeleccionarTipoActivity::class.java))
            finish()
        } else {
            // Hay un usuario autenticado, consultar su tipo en Firebase
            Log.d("SplashScreen", "Usuario autenticado: ${firebaseUser.uid}")

            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tipoU = snapshot.child("tipoUsuario").value?.toString()
                        Log.d("SplashScreen", "Tipo de usuario obtenido: $tipoU")

                        if (tipoU != null) {
                            // Guardamos el tipo para futuras sesiones
                            val editor = prefs.edit()
                            editor.putString(USER_TYPE_KEY, tipoU)
                            editor.putString(USER_ID_KEY, firebaseUser.uid)
                            editor.apply()

                            navigateBasedOnUserType(tipoU)
                        } else {
                            // Si no encontramos el tipo, ir a selección de tipo
                            Log.e("SplashScreen", "No se encontró el tipo de usuario")
                            startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SplashScreen", "Error al consultar tipo de usuario: ${error.message}")
                        startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                        finish()
                    }
                })
        }
    }

    private fun navigateBasedOnUserType(tipoUsuario: String) {
        Log.d("SplashScreen", "Navegando basado en tipo: $tipoUsuario")

        when (tipoUsuario.lowercase()) {
            "vendedor" -> {
                Log.d("SplashScreen", "Navegando a MainActivityVendedor")
                startActivity(Intent(this@SplashScreenActivity, MainActivityVendedor::class.java))
                finish()
            }
            "cliente" -> {
                Log.d("SplashScreen", "Navegando a MainActivityCliente")
                startActivity(Intent(this@SplashScreenActivity, MainActivityCliente::class.java))
                finish()
            }
            else -> {
                // Tipo desconocido, ir a selección
                Log.w("SplashScreen", "Tipo de usuario desconocido: $tipoUsuario")
                startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                finish()
            }
        }
    }
}