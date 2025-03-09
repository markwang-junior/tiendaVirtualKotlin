package com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentOrdenesV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Productos.AgregarProductoActivity
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentInicioVBinding

class FragmentInicioV : Fragment() {

    private lateinit var binding: FragmentInicioVBinding
    private lateinit var mContext : Context

    override fun onAttach(context: Context){
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Usa el método correcto "inflate"
        binding = FragmentInicioVBinding.inflate(inflater, container, false)

        // Configura el listener para el BottomNavigationView
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.op_mis_productos_v -> {
                    replaceFragment(FragmentMisProductosV())
                    true
                }
                R.id.op_mis_ordenes_v -> {
                    replaceFragment(FragmentOrdenesV())
                    true
                }
                else -> false
            }
        }

        // Establece por defecto el fragmento y el ítem seleccionado
        replaceFragment(FragmentMisProductosV())
        binding.bottomNavigation.selectedItemId = R.id.op_mis_productos_v

        binding.addFab.setOnClickListener{
            val intent = Intent(mContext, AgregarProductoActivity::class.java)
            intent.putExtra("Edicion", false)
            mContext.startActivity(intent)
        }

        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.bottomFragment, fragment)
            .commit()
    }
}
