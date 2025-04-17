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
import com.markwang.tiendavirtualapp_kotlin.Cliente.LoginClienteActivity
import com.markwang.tiendavirtualapp_kotlin.Cliente.MainActivityCliente
import com.markwang.tiendavirtualapp_kotlin.Vendedor.LoginVendedorActivity
import com.markwang.tiendavirtualapp_kotlin.Vendedor.MainActivityVendedor

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "TiendaVirtualPrefs"
    private val USER_TYPE_KEY = "user_type"
    private val USER_ID_KEY = "user_id"
    private val USER_LOGGED_IN = "user_logged_in"
    private val AUTH_PROVIDER = "auth_provider"
    private val INSTALLATION_FIRST_RUN = "first_run_after_install"
    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Configurar Firebase para persistencia de autenticación
        firebaseAuth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Añadir un listener para los cambios de estado de autenticación
        firebaseAuth.addAuthStateListener { auth ->
            Log.d(TAG, "Auth state changed: Usuario ${if (auth.currentUser != null) "autenticado" else "no autenticado"}")
        }

        // Verificar si es la primera ejecución después de instalar
        val isFirstRun = prefs.getBoolean(INSTALLATION_FIRST_RUN, true)

        if (isFirstRun) {
            // Es la primera ejecución después de instalar/reinstalar
            // Limpiar todas las preferencias y cerrar sesión en Firebase
            Log.d(TAG, "Primera ejecución después de instalar/reinstalar")
            limpiarPreferenciasCompletas()
            firebaseAuth.signOut()

            // Marcar que ya no es la primera ejecución
            val editor = prefs.edit()
            editor.putBoolean(INSTALLATION_FIRST_RUN, false)
            editor.apply()
        }

        verBienvenida()
    }

    private fun verBienvenida() {
        object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // No hacemos nada durante el tick
            }

            override fun onFinish() {
                comprobarSesionUsuario()
            }
        }.start()
    }

    private fun comprobarSesionUsuario() {
        val firebaseUser = firebaseAuth.currentUser
        val isLoggedIn = prefs.getBoolean(USER_LOGGED_IN, false)
        val savedUserId = prefs.getString(USER_ID_KEY, "")
        val authProvider = prefs.getString(AUTH_PROVIDER, "")
        val tipoUsuario = prefs.getString(USER_TYPE_KEY, "")

        Log.d(TAG, "Firebase user: ${firebaseUser?.uid}")
        Log.d(TAG, "Saved login state: $isLoggedIn")
        Log.d(TAG, "Saved user ID: $savedUserId")
        Log.d(TAG, "Auth provider: $authProvider")
        Log.d(TAG, "Tipo usuario: $tipoUsuario")

        // Después de reinstalar, siempre ir a la pantalla de selección de tipo
        if (firebaseUser == null) {
            // No hay usuario autenticado en Firebase, ir a la pantalla de selección
            Log.d(TAG, "No hay usuario autenticado, redirigiendo a selección de tipo")
            startActivity(Intent(this, SeleccionarTipoActivity::class.java))
            finish()
            return
        }

        // Si hay usuario autenticado pero no tenemos el tipo guardado o hay discrepancia
        if (tipoUsuario.isNullOrEmpty() || !isLoggedIn || savedUserId != firebaseUser.uid) {
            // Consultar el tipo en la base de datos
            Log.d(TAG, "Consultando tipo de usuario en Firebase")
            val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val tipoU = snapshot.child("tipoUsuario").value?.toString()
                        Log.d(TAG, "Tipo de usuario obtenido de Firebase: $tipoU")

                        if (tipoU != null) {
                            // Guardamos el tipo para futuras sesiones
                            actualizarInformacionSesion(firebaseUser.uid, tipoU)
                            navigateBasedOnUserType(tipoU)
                        } else {
                            // No se encontró el tipo, ir a selección
                            Log.d(TAG, "No se encontró el tipo de usuario, redirigiendo a selección")
                            limpiarPreferencias()
                            firebaseAuth.signOut()
                            startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error al consultar usuario: ${error.message}")
                        limpiarPreferencias()
                        firebaseAuth.signOut()
                        startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                        finish()
                    }
                })
        } else {
            // Tenemos el tipo guardado, navegar directamente
            Log.d(TAG, "Navegando con tipo guardado: $tipoUsuario")
            navigateBasedOnUserType(tipoUsuario)
        }
    }

    private fun actualizarInformacionSesion(uid: String, tipoUsuario: String) {
        val editor = prefs.edit()
        editor.putBoolean(USER_LOGGED_IN, true)
        editor.putString(USER_ID_KEY, uid)
        editor.putString(USER_TYPE_KEY, tipoUsuario)

        // Determinar el proveedor si no está guardado
        if (!prefs.contains(AUTH_PROVIDER)) {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val providers = currentUser.providerData.map { it.providerId }
                when {
                    providers.contains("google.com") -> editor.putString(AUTH_PROVIDER, "google")
                    providers.contains("phone") -> editor.putString(AUTH_PROVIDER, "phone")
                    else -> editor.putString(AUTH_PROVIDER, "email")
                }
            }
        }

        editor.apply()
        Log.d(TAG, "Información de sesión actualizada")
    }

    private fun limpiarPreferencias() {
        val editor = prefs.edit()
        editor.remove(USER_LOGGED_IN)
        editor.remove(USER_TYPE_KEY)
        editor.remove(USER_ID_KEY)
        editor.remove(AUTH_PROVIDER)
        editor.apply()
        Log.d(TAG, "Preferencias limpiadas")
    }

    private fun limpiarPreferenciasCompletas() {
        val editor = prefs.edit()
        editor.clear() // Elimina todas las preferencias
        editor.apply()
        Log.d(TAG, "Todas las preferencias limpiadas")
    }

    private fun navigateBasedOnUserType(tipoUsuario: String) {
        Log.d(TAG, "Navegando basado en tipo: $tipoUsuario")

        when (tipoUsuario.lowercase()) {
            "vendedor" -> {
                Log.d(TAG, "Navegando a MainActivityVendedor")
                startActivity(Intent(this@SplashScreenActivity, MainActivityVendedor::class.java))
                finish()
            }
            "cliente" -> {
                Log.d(TAG, "Navegando a MainActivityCliente")
                startActivity(Intent(this@SplashScreenActivity, MainActivityCliente::class.java))
                finish()
            }
            else -> {
                // Tipo desconocido, ir a selección
                Log.w(TAG, "Tipo de usuario desconocido: $tipoUsuario")
                limpiarPreferencias()
                firebaseAuth.signOut()
                startActivity(Intent(this@SplashScreenActivity, SeleccionarTipoActivity::class.java))
                finish()
            }
        }
    }
}