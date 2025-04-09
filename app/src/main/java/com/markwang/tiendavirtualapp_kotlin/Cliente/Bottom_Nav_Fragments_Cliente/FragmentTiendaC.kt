package com.markwang.tiendavirtualapp_kotlin.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorCategoriaC
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoAleatorio
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoC
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloCategoria
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentTiendaCBinding

class FragmentTiendaC : Fragment() {

    private lateinit var binding : FragmentTiendaCBinding
    private lateinit var mContext : Context
    private lateinit var firebaseAuth : FirebaseAuth

    private lateinit var categoriaArrayList : ArrayList<ModeloCategoria>
    private lateinit var adapdadorCategoria : AdapdadorCategoriaC

    private lateinit var productoArrayList: ArrayList<ModeloProducto>
    private lateinit var filteredProductArrayList: ArrayList<ModeloProducto>
    private lateinit var adapdadorProducto : AdapdadorProductoAleatorio
    private lateinit var adapdadorProductoBusqueda : AdapdadorProductoC

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTiendaCBinding.inflate(LayoutInflater.from(mContext), container ,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        // Inicializar listas
        productoArrayList = ArrayList()
        filteredProductArrayList = ArrayList()

        // Configurar RecyclerView de productos aleatorios
        val productosLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.productosAleatRV.layoutManager = productosLayoutManager

        // Cargar datos
        leerInfoCliente()
        listarCategorias()
        obtenerProductoAlea()

        // Configurar búsqueda
        binding.etBuscarProductoTienda?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtrar productos solo si el campo existe
                s?.toString()?.let { filtrarProductos(it) }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarProductos(query: String) {
        filteredProductArrayList.clear()

        if (query.isEmpty()) {
            // Si la búsqueda está vacía, mostrar productos aleatorios
            val listaAleatoria = productoArrayList.shuffled().take(5)
            adapdadorProducto = AdapdadorProductoAleatorio(mContext, listaAleatoria)
            binding.productosAleatRV.adapter = adapdadorProducto
        } else {
            // Filtrar productos por nombre
            for (producto in productoArrayList) {
                if (producto.nombre.lowercase().contains(query.lowercase())) {
                    filteredProductArrayList.add(producto)
                }
            }

            // Mostrar resultados de búsqueda
            adapdadorProductoBusqueda = AdapdadorProductoC(mContext, filteredProductArrayList)
            binding.productosAleatRV.adapter = adapdadorProductoBusqueda
        }
    }

    private fun leerInfoCliente(){
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = "${snapshot.child("nombres").value}"
                    var direccion = "${snapshot.child("direccion").value}"

                    // Mejorar formato de texto
                    val nombreFormateado = if (nombres != "null" && nombres.isNotEmpty()) {
                        "Bienvenido(a): $nombres"
                    } else {
                        "Bienvenido(a) a la tienda"
                    }

                    val direccionFormateada = if (direccion != "null" && direccion.isNotEmpty()) {
                        direccion
                    } else {
                        "Dirección no registrada"
                    }

                    binding.bienvenidaTXT.text = nombreFormateado
                    binding.direccionTXT.text = direccionFormateada
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al cargar información: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun obtenerProductoAlea() {
        productoArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloProducto = ds.getValue(ModeloProducto::class.java)
                    if (modeloProducto != null) {
                        productoArrayList.add(modeloProducto)
                    }
                }

                // Mezclar y tomar 5 productos aleatorios
                val listaAleatoria = productoArrayList.shuffled().take(10)

                // Mostrar productos aleatorios
                adapdadorProducto = AdapdadorProductoAleatorio(mContext, listaAleatoria)
                binding.productosAleatRV.adapter = adapdadorProducto
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(mContext, "Error al cargar productos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listarCategorias() {
        categoriaArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Categorias")
            .orderByChild("categoria")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoriaArrayList.clear()
                for (ds in snapshot.children){
                    val modeloCat = ds.getValue(ModeloCategoria::class.java)
                    if (modeloCat != null) {
                        categoriaArrayList.add(modeloCat)
                    }
                }

                adapdadorCategoria = AdapdadorCategoriaC(mContext, categoriaArrayList)
                binding.categoriaRV.adapter = adapdadorCategoria
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(mContext, "Error al cargar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}