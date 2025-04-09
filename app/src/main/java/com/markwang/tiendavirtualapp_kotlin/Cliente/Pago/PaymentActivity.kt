// app/src/main/java/com/markwang/tiendavirtualapp_kotlin/Cliente/Pago/PaymentActivity.kt

package com.markwang.tiendavirtualapp_kotlin.Cliente.Pago

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Cliente.Orden.DetalleOrdenCActivity
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var precioTotal: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Obtener el precio total del intent
        precioTotal = intent.getStringExtra("precioTotal") ?: "0"
        precioTotal = precioTotal.replace("Total: ", "").trim()

        // Mostrar el total a pagar
        binding.tvTotal.text = "Total a pagar: ${precioTotal}"

        // Configurar botón de volver
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar botón de simular pago con PayPal
        binding.btnPayWithPaypal.setOnClickListener {
            // Simular procesamiento de pago
            simulatePapalPayment()
        }
    }

    private fun simulatePapalPayment() {
        // Mostrar un mensaje de procesamiento
        Toast.makeText(this, "Procesando pago con PayPal...", Toast.LENGTH_SHORT).show()

        // Simular un pequeño retraso para el procesamiento
        binding.btnPayWithPaypal.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        // Simular un retraso de procesamiento de 2 segundos
        android.os.Handler(mainLooper).postDelayed({
            // Crear la orden después del "pago exitoso"
            crearOrden()
        }, 2000)
    }

    private fun crearOrden() {
        val tiempo = Constantes().obtenerTiempoD()
        val costo = precioTotal
        val uid = firebaseAuth.uid

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        val keyId = ref.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["idOrden"] = keyId ?: ""
        hashMap["tiempoOrden"] = tiempo.toString()
        hashMap["estadoOrden"] = "Solicitud recibida"
        hashMap["costo"] = costo
        hashMap["ordenadoPor"] = uid ?: ""
        hashMap["pagado"] = true
        hashMap["metodoPago"] = "PayPal (Simulado)"

        ref.child(keyId!!).setValue(hashMap)
            .addOnSuccessListener {
                cargarProductosDelCarrito(keyId)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnPayWithPaypal.isEnabled = true
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarProductosDelCarrito(keyId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("CarritoCompras")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val idProducto = ds.child("idProducto").value.toString()
                        val nombre = ds.child("nombre").value.toString()
                        val precio = ds.child("precio").value.toString()
                        val precioFinal = ds.child("precioFinal").value.toString()
                        val precioDesc = ds.child("precioDesc").value.toString()
                        val cantidad = ds.child("cantidad").value.toString().toInt()

                        val hashMap2 = HashMap<String, Any>()
                        hashMap2["idProducto"] = idProducto
                        hashMap2["nombre"] = nombre
                        hashMap2["precio"] = precio
                        hashMap2["precioFinal"] = precioFinal
                        hashMap2["precioDesc"] = precioDesc
                        hashMap2["cantidad"] = cantidad

                        val refOrden = FirebaseDatabase.getInstance().getReference("Ordenes")
                        refOrden.child(keyId).child("Productos").child(idProducto).setValue(hashMap2)
                    }

                    // Limpiar el carrito
                    eliminarProductosCarrito()

                    // Mostrar mensaje de éxito
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@PaymentActivity, "Pago completado con éxito", Toast.LENGTH_SHORT).show()

                    // Ir a la pantalla de detalle de la orden
                    val intent = Intent(this@PaymentActivity, DetalleOrdenCActivity::class.java)
                    intent.putExtra("idOrden", keyId)
                    startActivity(intent)
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPayWithPaypal.isEnabled = true
                    Toast.makeText(this@PaymentActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun eliminarProductosCarrito() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
            .child(uid!!).child("CarritoCompras")

        ref.removeValue().addOnSuccessListener {
            // Carrito limpiado con éxito
        }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}