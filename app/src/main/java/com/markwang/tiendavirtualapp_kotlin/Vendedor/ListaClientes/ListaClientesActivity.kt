package com.markwang.tiendavirtualapp_kotlin.Vendedor.ListaClientes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorCliente
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloUsuario
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityListaClientesBinding

class ListaClientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaClientesBinding

    private lateinit var clientesArrayList: ArrayList<ModeloUsuario>
    private lateinit var adapdadorCliente: AdapdadorCliente

    // Variable para almacenar el teléfono del cliente seleccionado
    private var telefonoSeleccionado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaClientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar botón de regreso
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar listeners para los botones
        configurarAdaptadorClientes()

        // Cargar la lista de clientes
        listarClientes()
    }

    private fun configurarAdaptadorClientes() {
        clientesArrayList = ArrayList()
        adapdadorCliente = AdapdadorCliente(this, clientesArrayList)
        adapdadorCliente.setOnLlamarClickListener { telefono ->
            telefonoSeleccionado = telefono
            if (telefono.isNotEmpty() && telefono != "null" && telefono != "Sin teléfono") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    llamarCliente(telefono)
                } else {
                    permisoLlamar.launch(Manifest.permission.CALL_PHONE)
                }
            } else {
                Toast.makeText(this, "El cliente no ha registrado un número de teléfono", Toast.LENGTH_SHORT).show()
            }
        }

        adapdadorCliente.setOnSmsClickListener { telefono ->
            telefonoSeleccionado = telefono
            if (telefono.isNotEmpty() && telefono != "null" && telefono != "Sin teléfono") {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    enviarSms(telefono)
                } else {
                    permisoSms.launch(Manifest.permission.SEND_SMS)
                }
            } else {
                Toast.makeText(this, "El cliente no ha registrado un número de teléfono", Toast.LENGTH_SHORT).show()
            }
        }

        binding.clienteRV.adapter = adapdadorCliente
    }

    private fun listarClientes() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvNoClientes.visibility = View.GONE

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.orderByChild("tipoUsuario").equalTo("Cliente")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    clientesArrayList.clear()

                    if (snapshot.exists() && snapshot.childrenCount > 0) {
                        for (ds in snapshot.children) {
                            val modeloCliente = ds.getValue(ModeloUsuario::class.java)
                            // Comprobar los valores nulos o vacíos y asignar valores por defecto
                            if (modeloCliente != null) {
                                // Asegurar que los campos tengan valores por defecto si están vacíos
                                if (modeloCliente.nombres.isEmpty() || modeloCliente.nombres == "null") {
                                    modeloCliente.nombres = "Sin nombre"
                                }
                                if (modeloCliente.email.isEmpty() || modeloCliente.email == "null") {
                                    modeloCliente.email = "Sin email"
                                }
                                if (modeloCliente.dni.isEmpty() || modeloCliente.dni == "null") {
                                    modeloCliente.dni = "Sin DNI"
                                }
                                if (modeloCliente.telefono.isEmpty() || modeloCliente.telefono == "null") {
                                    modeloCliente.telefono = "Sin teléfono"
                                }
                                if (modeloCliente.direccion.isEmpty() || modeloCliente.direccion == "null") {
                                    modeloCliente.direccion = "Sin dirección"
                                }

                                clientesArrayList.add(modeloCliente)
                            }
                        }

                        adapdadorCliente.notifyDataSetChanged()
                        binding.tvNoClientes.visibility = View.GONE
                    } else {
                        // Mostrar mensaje de "No hay clientes"
                        binding.tvNoClientes.visibility = View.VISIBLE
                    }

                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ListaClientesActivity,
                        "Error al cargar clientes: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Función para realizar la llamada
    private fun llamarCliente(telefono: String) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$telefono")
        startActivity(intent)
    }

    // Función para enviar SMS
    private fun enviarSms(telefono: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:$telefono")
        intent.putExtra("sms_body", "Estimado cliente, le escribimos de la tienda virtual...")
        startActivity(intent)
    }

    // Permisos para llamada
    private val permisoLlamar = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            llamarCliente(telefonoSeleccionado)
        } else {
            Toast.makeText(this,
                "El permiso para realizar llamadas fue denegado",
                Toast.LENGTH_SHORT).show()
        }
    }

    // Permisos para SMS
    private val permisoSms = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enviarSms(telefonoSeleccionado)
        } else {
            Toast.makeText(this,
                "El permiso para enviar SMS fue denegado",
                Toast.LENGTH_SHORT).show()
        }
    }
}