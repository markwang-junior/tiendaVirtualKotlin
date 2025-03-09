package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityActualizarPasswordBinding

class ActualizarPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnActualizarPassword.setOnClickListener {
            validarInformacion()
        }
    }

    private var pass_actual = ""
    private var pass_nueva = ""
    private var pass_nuevar = ""

    private fun validarInformacion() {
        pass_actual = binding.etPasswordActual.text.toString().trim()
        pass_nueva = binding.etPasswordNueva.text.toString().trim()
        pass_nuevar = binding.etPasswordNuevaR.text.toString().trim()

        if (pass_actual.isEmpty()) {
            binding.etPasswordActual.error = "Ingrese contraseña actual"
            binding.etPasswordActual.requestFocus()
        } else if (pass_nueva.isEmpty()) {
            binding.etPasswordNueva.error = "Ingrese nueva contraseña"
            binding.etPasswordNueva.requestFocus()
        } else if (pass_nuevar.isEmpty()) {
            binding.etPasswordNuevaR.error = "Confirmar nueva contraseña"
            binding.etPasswordNuevaR.requestFocus()
        } else if (pass_nueva != pass_nuevar) {
            binding.etPasswordNuevaR.error = "Las contraseñas no coinciden"
            binding.etPasswordNuevaR.requestFocus()
        } else {
            autenticarUsuario()
        }
    }

    private fun autenticarUsuario() {
        progressDialog.setMessage("Autenticando usuario")
        progressDialog.show() // Error corregido: Se agregó show() en lugar de dismiss()

        val autoCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), pass_actual)
        firebaseUser.reauthenticate(autoCredential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                actualizarPassword()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this@ActualizarPasswordActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarPassword() { // Error corregido: Se eliminó el tipo de retorno Void?
        progressDialog.setMessage("Cambiando password")
        progressDialog.show()

        firebaseUser.updatePassword(pass_nueva)
            .addOnSuccessListener {
                progressDialog.dismiss() // Error corregido: Se agregó esta línea para cerrar el diálogo
                Toast.makeText(this@ActualizarPasswordActivity, "La contraseña ha sido cambiada", Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
                startActivity(Intent(this@ActualizarPasswordActivity, LoginClienteActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this@ActualizarPasswordActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}