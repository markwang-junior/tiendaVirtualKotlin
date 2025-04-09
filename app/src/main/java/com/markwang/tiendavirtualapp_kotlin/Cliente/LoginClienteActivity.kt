package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLoginC.setOnClickListener {
            validarInfo()
        }

        /*Iniciar sesion con una cuenta de google*/
        binding.btnLoginGoogle.setOnClickListener {
            googleLogin()
        }

        binding.btnLoginTel.setOnClickListener {
            startActivity(Intent(this, LoginTelActivity::class.java))
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
        val googleSignInClient = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInClient)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            //si el usuario seleccionó una cuenta del cuadro de díalogo
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                autenticacionGoogle(cuenta.idToken)
            } catch (e: Exception) {
                progressDialog.dismiss()
                mostrarErrorSnackbar("Error con Google: ${e.message}")
            }
        } else {
            progressDialog.dismiss()
            mostrarErrorSnackbar("Operación de login con Google cancelada")
        }
    }

    private fun autenticacionGoogle(idToken: String?) {
        progressDialog.setMessage("Autenticando con Google")
        progressDialog.show()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultadoAuth ->
                if (resultadoAuth.additionalUserInfo!!.isNewUser) {
                    //si el usuario es nuevo, registrar su informacion
                    llenarInfoBD()
                } else {
                    //si el usuario ya se registró con anterioridad
                    progressDialog.dismiss()
                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                mostrarErrorSnackbar("Error con Google: ${e.message}")
            }
    }

    private fun llenarInfoBD() {
        progressDialog.setMessage("Guardando información")

        val uid = firebaseAuth.uid
        val nombreC = firebaseAuth.currentUser?.displayName
        val emailC = firebaseAuth.currentUser?.email
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
                mostrarErrorSnackbar("Error al guardar datos: ${e.message}")
            }
    }
}