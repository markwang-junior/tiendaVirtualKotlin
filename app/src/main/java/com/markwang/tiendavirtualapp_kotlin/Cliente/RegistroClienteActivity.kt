package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityRegistroClienteBinding

class RegistroClienteActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegistroClienteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnRegistrarC.setOnClickListener {
            validarInformacion()
        }
    }

    private var nombres =""
    private var email =""
    private var password =""
    private var cpassword =""
    private fun validarInformacion() {
        nombres = binding.etNombreC.text.toString().trim()
        email   = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        cpassword = binding.etCPassword.text.toString().trim()

        if (nombres.isEmpty()){
            binding.etNombreC.error ="ingrese nombres"
            binding.etNombreC.requestFocus()
        }else if (email.isEmpty()){
            binding.etEmail.error ="ingrese email"
            binding.etEmail.requestFocus()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error ="Email no válido"
            binding.etEmail.requestFocus()
        }else if (password.isEmpty()){
            binding.etPassword.error ="Ingrese password"
            binding.etPassword.requestFocus()
        }else if (password.length < 6) {
            binding.etPassword.error = "Necesita más de 6 car."
            binding.etPassword.requestFocus()
        } else if (cpassword.isEmpty()) {
            binding.etCPassword.error = "Confirme password."
            binding.etCPassword.requestFocus()
        } else if (password!=cpassword) {
            binding.etCPassword.error = "No coinciden las contraseñas"
            binding.etCPassword.requestFocus()
        }else{
            registrarCliente()
        }
    }

    private fun registrarCliente(){
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                insertarInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun insertarInfo(){
        progressDialog.setMessage("Guardando información")

        val uid = firebaseAuth.uid
        val nombresC = nombres
        val emailC = email
        val tiempoRegistro = Constantes().obtenerTiempoD()

        val datosCliente = HashMap<String, Any>()

        datosCliente["uid"] = "$uid"
        datosCliente["nombres"] = "$nombresC"
        datosCliente["email"] = "$emailC"
        datosCliente["telefono"] = ""
        datosCliente["dni"] = ""
        datosCliente["proveedor"] = "email"
        datosCliente["tRegistro"] = "$tiempoRegistro"
        datosCliente["imagen"] = ""
        datosCliente["tipoUsuario"] = "Cliente"
        datosCliente["perfilCompleto"] = false // Indicar que el perfil está incompleto

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid!!)
            .setValue(datosCliente)
            .addOnSuccessListener {
                progressDialog.dismiss()
                // Dirigir a MainActivityCliente con bandera para abrir perfil
                val intent = Intent(this@RegistroClienteActivity, MainActivityCliente::class.java)
                intent.putExtra("ABRIR_PERFIL", true)
                startActivity(intent)
                finishAffinity()
                Toast.makeText(this, "Registro exitoso. Por favor completa tu perfil", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}