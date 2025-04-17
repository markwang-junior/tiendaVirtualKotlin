package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityLoginClienteBinding

class LoginClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginClienteBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val TAG = "LoginClienteActivity"

    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "TiendaVirtualPrefs"
    private val USER_TYPE_KEY = "user_type"
    private val USER_ID_KEY = "user_id"
    private val USER_LOGGED_IN = "user_logged_in"
    private val AUTH_PROVIDER = "auth_provider"
    private val USER_EMAIL = "user_email"
    private val USER_NAME = "user_name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        // Configuración mejorada para Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // Solicitar perfil también
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Verificar si ya hay una sesión activa
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && prefs.getBoolean(USER_LOGGED_IN, false)) {
            // Ya hay una sesión, redirigir directamente
            Log.d(TAG, "Usuario ya logueado: ${currentUser.uid}")
            startActivity(Intent(this, MainActivityCliente::class.java))
            finish()
            return
        }

        binding.btnLoginC.setOnClickListener {
            validarInfo()
        }

        /*Iniciar sesion con una cuenta de google*/
        binding.btnLoginGoogle.setOnClickListener {
            googleLogin()
        }

        binding.tvRegistrarC.setOnClickListener {
            startActivity(Intent(this@LoginClienteActivity, RegistroClienteActivity::class.java))
        }

        binding.tvRecuperarPass.setOnClickListener {
            startActivity(Intent(this@LoginClienteActivity, RecuperarPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""
    private fun validarInfo() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email inválido"
            binding.etEmail.requestFocus()
        } else if (email.isEmpty()) {
            binding.etEmail.error = "Ingrese email"
            binding.etEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.etPassword.error = "Ingrese password"
            binding.etPassword.requestFocus()
        } else {
            loginCliente()
        }
    }

    private fun loginCliente() {
        progressDialog.setMessage("Ingresando")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                // Guardar información de la sesión
                guardarSesion("email", email)

                startActivity(Intent(this, MainActivityCliente::class.java))
                finishAffinity()
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()

                // Mejorar el manejo de errores
                when (e) {
                    is FirebaseAuthInvalidUserException -> {
                        // Email no existe
                        mostrarErrorSnackbar("El correo electrónico no está registrado")
                        binding.etEmail.error = "Correo no registrado"
                        binding.etEmail.requestFocus()
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Contraseña incorrecta
                        mostrarErrorSnackbar("Contraseña incorrecta")
                        binding.etPassword.error = "Contraseña incorrecta"
                        binding.etPassword.requestFocus()
                    }
                    else -> {
                        // Otro error
                        mostrarErrorSnackbar("Error al iniciar sesión: ${e.message}")
                    }
                }
            }
    }

    private fun mostrarErrorSnackbar(mensaje: String) {
        val snackbar = Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
        snackbar.setAction("Reintentar") {
            // No hacemos nada, solo cerramos el snackbar
        }
        snackbar.show()
    }

    private fun googleLogin() {
        Log.d(TAG, "Iniciando proceso de login con Google")
        progressDialog.setMessage("Preparando inicio de sesión con Google")
        progressDialog.show()

        // Cerrar cualquier sesión anterior de Google
        mGoogleSignInClient.signOut().addOnCompleteListener {
            val googleSignInIntent = mGoogleSignInClient.signInIntent
            googleSignInARL.launch(googleSignInIntent)
        }
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        progressDialog.dismiss()

        if (resultado.resultCode == RESULT_OK) {
            Log.d(TAG, "Google Sign-In exitoso, procesando...")
            //si el usuario seleccionó una cuenta del cuadro de díalogo
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                Log.d(TAG, "Google Sign-In: ID Token obtenido")
                autenticacionGoogle(cuenta.idToken, cuenta.email, cuenta.displayName)
            } catch (e: Exception) {
                Log.e(TAG, "Error en Google Sign-In: ${e.message}")
                mostrarErrorSnackbar("Error con Google: ${e.message}")
            }
        } else {
            Log.d(TAG, "Google Sign-In cancelado por el usuario")
            mostrarErrorSnackbar("Operación de login con Google cancelada")
        }
    }

    private fun autenticacionGoogle(idToken: String?, email: String?, displayName: String?) {
        progressDialog.setMessage("Autenticando con Google")
        progressDialog.show()

        Log.d(TAG, "Iniciando autenticación con Firebase usando token de Google")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultadoAuth ->
                Log.d(TAG, "Autenticación con Firebase exitosa: ${resultadoAuth.user?.uid}")

                if (resultadoAuth.additionalUserInfo!!.isNewUser) {
                    //si el usuario es nuevo, registrar su informacion
                    Log.d(TAG, "Usuario nuevo, registrando información en la BD")
                    llenarInfoBD(email, displayName)
                } else {
                    //si el usuario ya se registró con anterioridad
                    progressDialog.dismiss()

                    // Guardar información de la sesión con Google
                    Log.d(TAG, "Usuario existente, guardando sesión")
                    guardarSesion("google", email, displayName)

                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e(TAG, "Error en autenticación con Firebase: ${e.message}")
                mostrarErrorSnackbar("Error con Google: ${e.message}")
            }
    }

    private fun guardarSesion(proveedor: String, email: String? = null, nombre: String? = null) {
        Log.d(TAG, "Guardando información de sesión: proveedor=$proveedor, email=$email, uid=${firebaseAuth.uid}")

        val editor = prefs.edit()
        editor.putBoolean(USER_LOGGED_IN, true)
        editor.putString(USER_ID_KEY, firebaseAuth.uid)
        editor.putString(USER_TYPE_KEY, "Cliente")
        editor.putString(AUTH_PROVIDER, proveedor)

        // Guardar email y nombre si están disponibles
        if (email != null) {
            editor.putString(USER_EMAIL, email)
        }

        if (nombre != null) {
            editor.putString(USER_NAME, nombre)
        }

        editor.apply()
    }

    private fun llenarInfoBD(email: String? = null, nombre: String? = null) {
        progressDialog.setMessage("Guardando información")

        val uid = firebaseAuth.uid
        val nombreC = nombre ?: firebaseAuth.currentUser?.displayName
        val emailC = email ?: firebaseAuth.currentUser?.email
        val tiempoRegistro = Constantes().obtenerTiempoD()

        val datosCliente = HashMap<String, Any>()

        datosCliente["uid"] = "$uid"
        datosCliente["nombres"] = "$nombreC"
        datosCliente["email"] = " $emailC"
        datosCliente["telefono"] = ""
        datosCliente["dni"] = ""
        datosCliente["proveedor"] = "google"
        datosCliente["tRegistro"] = "$tiempoRegistro"
        datosCliente["imagen"] = ""
        datosCliente["tipoUsuario"] = "Cliente"
        datosCliente["perfilCompleto"] = false // Indicar que el perfil está incompleto

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uid!!)
            .setValue(datosCliente)
            .addOnSuccessListener {
                progressDialog.dismiss()

                Log.d(TAG, "Información guardada en BD, guardando sesión")
                // Guardar información de la sesión con Google
                guardarSesion("google", emailC, nombreC)

                // Dirigir a MainActivityCliente con bandera para abrir perfil
                val intent = Intent(this, MainActivityCliente::class.java)
                intent.putExtra("ABRIR_PERFIL", true)
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Bienvenido. Por favor completa tu perfil", Toast.LENGTH_LONG)
                    .show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e(TAG, "Error al guardar datos en BD: ${e.message}")
                mostrarErrorSnackbar("Error al guardar datos: ${e.message}")
            }
    }

    override fun onStart() {
        super.onStart()

        // Verificar si ya hay una sesión activa al iniciar
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && prefs.getBoolean(USER_LOGGED_IN, false)) {
            Log.d(TAG, "onStart: Usuario ya logueado: ${currentUser.uid}")
            startActivity(Intent(this, MainActivityCliente::class.java))
            finish()
        }
    }
}