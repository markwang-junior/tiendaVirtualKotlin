// app/src/main/java/com/markwang/tiendavirtualapp_kotlin/Cliente/Bottom_Nav_Fragments_Cliente/FragmentFavoritosC.kt
// Este archivo mantiene la misma lógica pero con mejoras visuales

package com.markwang.tiendavirtualapp_kotlin.Cliente.Bottom_Nav_Fragments_Cliente

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorProductoC
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProducto
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentFavoritosCBinding

class FragmentFavoritosC : Fragment() {

    private lateinit var binding: FragmentFavoritosCBinding
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var productoArrayList: ArrayList<ModeloProducto>
    private lateinit var productoAdapdador: AdapdadorProductoC

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFavoritosCBinding.inflate(inflater, container, false)

        // Configurar botón para explorar tienda (cuando favoritos está vacío)
        binding.btnExplorarTienda.setOnClickListener {
            // Navegar al fragmento de tienda
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)?.selectedItemId = R.id.op_tienda_c
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        cargarProdFav()
    }

    private fun cargarProdFav() {
        productoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    productoArrayList.clear()

                    // Comprobar si hay favoritos
                    if (!snapshot.exists() || snapshot.childrenCount.toInt() == 0) {
                        // Mostrar vista de favoritos vacíos
                        binding.favoritosVacioLayout.visibility = View.VISIBLE
                        binding.favoritosRv.visibility = View.GONE
                        return
                    } else {
                        // Mostrar la lista de favoritos normal
                        binding.favoritosVacioLayout.visibility = View.GONE
                        binding.favoritosRv.visibility = View.VISIBLE
                    }

                    for (ds in snapshot.children){
                        val idProducto = "${ds.child("idProducto").value}"

                        val refProd = FirebaseDatabase.getInstance().getReference("Productos")
                        refProd.child(idProducto)
                            .addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val modeloProducto = snapshot.getValue(ModeloProducto::class.java)
                                        productoArrayList.add(modeloProducto!!)
                                    }catch (e:Exception){
                                        // Manejar error
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(mContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        productoArrayList.sortBy { it.nombre }
                        productoAdapdador = AdapdadorProductoC(mContext, productoArrayList)
                        binding.favoritosRv.adapter = productoAdapdador

                        // Verificar después de cargar si realmente hay productos
                        if (productoArrayList.isEmpty()) {
                            binding.favoritosVacioLayout.visibility = View.VISIBLE
                            binding.favoritosRv.visibility = View.GONE
                        }
                    }, 500)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}