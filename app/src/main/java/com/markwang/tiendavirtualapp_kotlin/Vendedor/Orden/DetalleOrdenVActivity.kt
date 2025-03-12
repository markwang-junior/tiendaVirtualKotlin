package com.markwang.tiendavirtualapp_kotlin.Vendedor.Orden

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoOrden
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProductoOrden
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityDetalleOrdenVactivityBinding

class DetalleOrdenVActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleOrdenVactivityBinding

    private var idOrden = ""
    private var uidCliente = "" // Variable para almacenar el UID del cliente
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productosArrayList: ArrayList<ModeloProductoOrden>
    private lateinit var productoOrdenAdapdador: AdapdadorProductoOrden

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleOrdenVactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        idOrden = intent.getStringExtra("idOrden") ?: ""

        // Añadir esta línea para manejar el evento de clic en el botón de regreso
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        datosOrden()
        productosOrden()

        binding.IbActualizarOrden.setOnClickListener {
            estadoOrdenMenu()
        }

        // Manejar el evento de clic en el botón de información del cliente
        binding.verInfoCliente.setOnClickListener {
            if (uidCliente.isNotEmpty() && uidCliente != "null") {
                mostrarInfoCliente(uidCliente)
            } else {
                Toast.makeText(this, "No se pudo obtener la información del cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para mostrar el diálogo con información del cliente
    private fun mostrarInfoCliente(uidCliente: String) {
        // Crear el diálogo
        val dialog = Dialog(this)

        // Usar el nombre correcto del archivo layout
        dialog.setContentView(R.layout.dialog_info_cliente)

        // Obtener referencias a las vistas del diálogo
        val tvNombresC = dialog.findViewById<TextView>(R.id.tvNombresC)
        val tvDniC = dialog.findViewById<TextView>(R.id.tvDniC)
        val tvTelC = dialog.findViewById<TextView>(R.id.tvTelC)
        val tvDireccionC = dialog.findViewById<TextView>(R.id.tvDireccionC)
        val ibCerrar = dialog.findViewById<ImageButton>(R.id.ibCerrar)
        val btnLlamar = dialog.findViewById<MaterialButton>(R.id.btnLlamar)
        val btnSms = dialog.findViewById<MaterialButton>(R.id.btnSms)
        val ivPerfil = dialog.findViewById<ImageView>(R.id.ivPerfil)

        // Cargar los datos del cliente desde Firebase
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidCliente).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Log para depuración
                Log.d("InfoCliente", "Datos obtenidos: ${snapshot.value}")

                // Obtener los datos del cliente
                val nombres = snapshot.child("nombres").value?.toString() ?: ""
                val dni = snapshot.child("dni").value?.toString() ?: ""
                val telefono = snapshot.child("telefono").value?.toString() ?: ""
                val direccion = snapshot.child("direccion").value?.toString() ?: ""
                val imagenUrl = snapshot.child("imagen").value?.toString() ?: ""

                // Mostrar los datos en el diálogo
                tvNombresC.text = if (nombres.isNotEmpty() && nombres != "null") nombres else "No disponible"
                tvDniC.text = if (dni.isNotEmpty() && dni != "null") dni else "No disponible"
                tvTelC.text = if (telefono.isNotEmpty() && telefono != "null") telefono else "No disponible"
                tvDireccionC.text = if (direccion.isNotEmpty() && direccion != "null") direccion else "No disponible"

                // Cargar la imagen de perfil si está disponible
                if (imagenUrl.isNotEmpty() && imagenUrl != "null") {
                    try {
                        Glide.with(this@DetalleOrdenVActivity)
                            .load(imagenUrl)
                            .placeholder(R.drawable.img_perfil)
                            .into(ivPerfil)
                    } catch (e: Exception) {
                        Log.e("InfoCliente", "Error al cargar imagen: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InfoCliente", "Error en Firebase: ${error.message}")
                Toast.makeText(this@DetalleOrdenVActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Configurar el evento de cierre
        ibCerrar.setOnClickListener {
            dialog.dismiss()
        }

        // Configurar los botones de llamar y SMS (por ahora sin funcionalidad)
        btnLlamar.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de llamada no implementada", Toast.LENGTH_SHORT).show()
        }

        btnSms.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de SMS no implementada", Toast.LENGTH_SHORT).show()
        }

        // Ajustar el diálogo para que no se cancele al tocar fuera
        dialog.setCanceledOnTouchOutside(false)

        // Mostrar el diálogo
        dialog.show()
    }

    private fun estadoOrdenMenu() {
        val popupMenu = PopupMenu(this, binding.IbActualizarOrden)

        // Añadir la nueva opción "En preparación" como índice 0
        popupMenu.menu.add(Menu.NONE, 0, 0, "En preparación")
        // Mover las opciones existentes a índices 1 y 2
        popupMenu.menu.add(Menu.NONE, 1, 1, "Pedido entregada")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Pedido cancelada")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId

            if (itemId == 0) {
                // Nueva opción: Marcar el pedido como en preparación
                actualizarEstado("En preparación")
            } else if (itemId == 1) {
                // Marcar el pedido como entregado
                actualizarEstado("Entregado")
            } else if (itemId == 2) {
                // Marcar el pedido como cancelado
                actualizarEstado("Cancelado")
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun actualizarEstado(estado: String) {
        val hashMap = HashMap<String, Any>()
        hashMap["estadoOrden"] = estado

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden)
        ref.updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "El estado del pedido ha pasado a: ${estado}",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ha ocurrido un error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun productosOrden() {
        productosArrayList = ArrayList()
        val ref =
            FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden).child("Productos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productosArrayList.clear()
                for (ds in snapshot.children) {
                    val modeloProductoOrden = ds.getValue(ModeloProductoOrden::class.java)
                    productosArrayList.add(modeloProductoOrden!!)
                }

                productoOrdenAdapdador =
                    AdapdadorProductoOrden(this@DetalleOrdenVActivity, productosArrayList)
                binding.ordenesRv.adapter = productoOrdenAdapdador

                binding.cantidadOrdenD.text = snapshot.childrenCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetalleOrdenVActivity, "Error al cargar productos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun direccionCliente(uidCliente: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(uidCliente)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val direccion = snapshot.child("direccion").value?.toString() ?: ""
                    val nombres = snapshot.child("nombres").value?.toString() ?: ""

                    // Textos a mostrar con valores por defecto en caso de que estén vacíos
                    val nombreMostrar = if (nombres.isNotEmpty() && nombres != "null") nombres else "Cliente sin nombre"
                    val direccionMostrar = if (direccion.isNotEmpty() && direccion != "null") direccion else "Dirección no disponible"

                    // Construir el texto final a mostrar
                    binding.direccionOrdenD.text = "Cliente: $nombreMostrar\n$direccionMostrar"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetalleOrdenVActivity, "Error al cargar dirección: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun datosOrden() {
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes").child(idOrden)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val idOrden = "${snapshot.child("idOrden").value}"
                val costo = "${snapshot.child("costo").value}"
                val tiempoOrden = "${snapshot.child("tiempoOrden").value}"
                val estadoOrden = "${snapshot.child("estadoOrden").value}"
                // Obtener el ID del cliente que realizó el pedido y guardarlo en la variable de clase
                uidCliente = "${snapshot.child("ordenadoPor").value}"

                val fecha = Constantes().obtenerFecha(tiempoOrden.toLong())

                binding.idOrdenD.text = idOrden
                binding.fechaOrdenD.text = fecha
                binding.estadoOrdenD.text = estadoOrden
                binding.costoOrdenD.text = costo

                if (estadoOrden.equals("Solicitud recibida")) {
                    binding.estadoOrdenD.setTextColor(
                        ContextCompat.getColor(
                            this@DetalleOrdenVActivity,
                            R.color.azul_marino_oscuro
                        )
                    )
                } else if (estadoOrden.equals("En preparación")) {
                    binding.estadoOrdenD.setTextColor(
                        ContextCompat.getColor(
                            this@DetalleOrdenVActivity,
                            R.color.naranja
                        )
                    )
                } else if (estadoOrden.equals("Entregado")) {
                    binding.estadoOrdenD.setTextColor(
                        ContextCompat.getColor(
                            this@DetalleOrdenVActivity,
                            R.color.verde_oscuro2
                        )
                    )
                } else if (estadoOrden.equals("Cancelado")) {
                    binding.estadoOrdenD.setTextColor(
                        ContextCompat.getColor(
                            this@DetalleOrdenVActivity,
                            R.color.rojo
                        )
                    )
                }

                // Llamar a la función para cargar la dirección del cliente
                direccionCliente(uidCliente)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DetalleOrdenVActivity,
                    "Error al cargar datos: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}