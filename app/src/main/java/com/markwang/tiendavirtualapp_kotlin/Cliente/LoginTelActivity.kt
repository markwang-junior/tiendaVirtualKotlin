package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.FirebaseDatabase
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityLoginTelBinding
import java.util.concurrent.TimeUnit

class LoginTelActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginTelBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private var forceResendingToken : ForceResendingToken? = null
    private lateinit var mCallback : OnVerificationStateChangedCallbacks
    private var mVerification : String? = null

    // Variables para manejo mejorado de intentos
    private var numeroIntentos = 0
    private val MAX_INTENTOS = 3
    private val COOLDOWN_TIME = 60000L // 60 segundos de espera
    private var lastAttemptTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginTelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.rlTelefono.visibility = View.VISIBLE
        binding.rlCodigoVer.visibility = View.GONE

        phoneLoginCallbacks()

        binding.btnEnviarCodigo.setOnClickListener {
            validarData()
        }

        binding.btnVerificarCod.setOnClickListener {
            val otp = binding.etCodVer.text.toString().trim()
            if (otp.isEmpty()){
                binding.etCodVer.error = "Ingrese código"
                binding.etCodVer.requestFocus()
                mostrarErrorSnackbar("Debe ingresar el código de verificación")
            } else if (otp.length < 6){
                binding.etCodVer.error = "El código debe contener 6 caracteres"
                binding.etCodVer.requestFocus()
                mostrarErrorSnackbar("El código debe contener 6 caracteres")
            } else {
                verificarCodTel(otp)
            }
        }

        binding.tvReenviarCod.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            if (forceResendingToken != null) {
                if (numeroIntentos < MAX_INTENTOS) {
                    reenviarCodVer()
                    numeroIntentos++
                    lastAttemptTime = currentTime

                    // Actualiza el texto para mostrar cuántos intentos quedan
                    val intentosRestantes = MAX_INTENTOS - numeroIntentos
                    Toast.makeText(this, "Te quedan $intentosRestantes intentos", Toast.LENGTH_SHORT).show()
                } else {
                    // Verifica si ha pasado suficiente tiempo para resetear
                    if (currentTime - lastAttemptTime > COOLDOWN_TIME) {
                        // Reset del contador
                        numeroIntentos = 0
                        reenviarCodVer()
                        numeroIntentos++
                        lastAttemptTime = currentTime
                    } else {
                        // Calcula cuánto tiempo falta para poder intentar de nuevo
                        val tiempoRestante = (COOLDOWN_TIME - (currentTime - lastAttemptTime)) / 1000
                        mostrarErrorSnackbar("Has excedido el número máximo de intentos. Espera $tiempoRestante segundos.")
                    }
                }
            } else {
                mostrarErrorSnackbar("No se puede reenviar el código todavía")
            }
        }
    }

    private fun verificarCodTel(otp: String) {
        progressDialog.setMessage("Verificando código...")
        progressDialog.show()

        val credencial = PhoneAuthProvider.getCredential(mVerification!!, otp)
        signInWithPhoneAuthCredencial(credencial)
    }

    private fun signInWithPhoneAuthCredencial(credencial: PhoneAuthCredential) {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { authResult ->
                progressDialog.dismiss()
                if (authResult.additionalUserInfo!!.isNewUser){
                    guardarInfo()
                } else {
                    startActivity(Intent(this, MainActivityCliente::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        mostrarErrorSnackbar("Código inválido. Por favor verifica e intenta de nuevo.")
                        binding.etCodVer.error = "Código inválido"
                        binding.etCodVer.requestFocus()
                    }
                    else -> {
                        mostrarErrorSnackbar("Error al verificar: ${e.message}")
                    }
                }
            }
    }

    private fun guardarInfo() {
        progressDialog.setMessage("Guardando información...")
        progressDialog.show()

        val uid = firebaseAuth.uid
        val tiempoReg = Constantes().obtenerTiempoD()

        val datosCliente = HashMap<String, Any>()

        datosCliente["uid"] = "${uid}"
        datosCliente["nombres"] = ""
        datosCliente["telefono"] = "${codTelnumTel}"
        datosCliente["email"] = ""
        datosCliente["dni"] = ""
        datosCliente["proveedor"] = "telefono"
        datosCliente["tRegistro"] = tiempoReg
        datosCliente["imagen"] = ""
        datosCliente["tipoUsuario"] = "Cliente"
        datosCliente["perfilCompleto"] = false // Indicar que el perfil está incompleto

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid!!)
            .setValue(datosCliente)
            .addOnSuccessListener {
                progressDialog.dismiss()
                // Dirigir a MainActivityCliente con bandera para abrir perfil
                val intent = Intent(this@LoginTelActivity, MainActivityCliente::class.java)
                intent.putExtra("ABRIR_PERFIL", true)
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Bienvenido. Por favor completa tu perfil", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                mostrarErrorSnackbar("Error al guardar datos: ${e.message}")
            }
    }

    private fun reenviarCodVer() {
        progressDialog.setMessage("Reenviando código a ${numeroTel}")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelnumTel)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .setForceResendingToken(forceResendingToken!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var codigoTel = "" //+34
    private var numeroTel = "" //677456332
    private var codTelnumTel = "" // +34 677354332
    private fun validarData() {
        codigoTel = binding.telCodePicker.selectedCountryCodeWithPlus
        numeroTel = binding.etTelefonoC.text.toString().trim()

        if (numeroTel.isEmpty()){
            binding.etTelefonoC.error = "Ingrese número teléfono"
            binding.etTelefonoC.requestFocus()
            mostrarErrorSnackbar("Debe ingresar un número de teléfono")
            return
        }

        // Validación básica del número de teléfono
        if (numeroTel.length < 9) {
            binding.etTelefonoC.error = "Número de teléfono demasiado corto"
            binding.etTelefonoC.requestFocus()
            mostrarErrorSnackbar("El número de teléfono debe tener al menos 9 dígitos")
            return
        }

        codTelnumTel = codigoTel + numeroTel
        verificarNumeroTel()
    }

    private fun verificarNumeroTel() {
        progressDialog.setMessage("Enviando código a ${numeroTel}")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(codTelnumTel)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun phoneLoginCallbacks() {
        mCallback = object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                mVerification = verificationId
                forceResendingToken = token

                progressDialog.dismiss()

                binding.rlTelefono.visibility = View.GONE
                binding.rlCodigoVer.visibility = View.VISIBLE

                Snackbar.make(
                    binding.root,
                    "Código enviado a $codTelnumTel. Por favor, ingresa el código de 6 dígitos.",
                    Snackbar.LENGTH_LONG
                ).show()
            }

            override fun onVerificationCompleted(phoneAuthCredencial: PhoneAuthCredential) {
                // Auto-verificación completada
                signInWithPhoneAuthCredencial(phoneAuthCredencial)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()

                when (e) {
                    is FirebaseTooManyRequestsException -> {
                        // Esto indica que se superó el límite de Firebase
                        numeroIntentos = MAX_INTENTOS // Forzar el cooldown
                        lastAttemptTime = System.currentTimeMillis()
                        mostrarErrorSnackbar("Demasiados intentos. Por favor, inténtalo después de 1 hora o usa otro método de login.")

                        // Opcional: redireccionar al usuario a otro método de login
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish() // Volver a la pantalla anterior
                        }, 3000)
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        binding.etTelefonoC.error = "Número de teléfono inválido"
                        binding.etTelefonoC.requestFocus()
                        mostrarErrorSnackbar("Número de teléfono en formato inválido")
                    }
                    else -> {
                        mostrarErrorSnackbar("Error de verificación: ${e.message}")
                    }
                }
            }
        }
    }

    private fun mostrarErrorSnackbar(mensaje: String) {
        val snackbar = Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG)
        snackbar.setAction("Aceptar") {
            // No hacemos nada, solo cerramos el snackbar
        }
        snackbar.show()
    }

    override fun onBackPressed() {
        if (binding.rlCodigoVer.visibility == View.VISIBLE) {
            // Si estamos en la pantalla de verificación, volvemos a la pantalla de teléfono
            binding.rlTelefono.visibility = View.VISIBLE
            binding.rlCodigoVer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}