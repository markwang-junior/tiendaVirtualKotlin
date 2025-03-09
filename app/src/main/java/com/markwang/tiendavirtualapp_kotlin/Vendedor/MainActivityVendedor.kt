package com.markwang.tiendavirtualapp_kotlin.Vendedor

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.SeleccionarTipoActivity
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentMisProductosV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor.FragmentOrdenesV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor.FragmentCategoriasV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor.FragmentInicioV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor.FragmentMiTiendaV
import com.markwang.tiendavirtualapp_kotlin.Vendedor.Nav_Fragments_Vendedor.FragmentReseniasV
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityMainVendedorBinding

class MainActivityVendedor : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainVendedorBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainVendedorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        // Configurar el Navigation Drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Fragment inicial
        replaceFragment(FragmentInicioV())
        binding.navigationView.setCheckedItem(R.id.op_inicio_v)
    }

    private fun cerrarSesion(){
        firebaseAuth!!.signOut()
        startActivity(Intent(applicationContext, SeleccionarTipoActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Has cerrado sesión", Toast.LENGTH_SHORT).show()
    }

    private fun comprobarSesion() {
        if (firebaseAuth.currentUser == null) {
            Toast.makeText(applicationContext, "Vendedor no registrado o no logueado", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext, SeleccionarTipoActivity::class.java))
            finish()
        } else {
            Toast.makeText(applicationContext, "Vendedor en línea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_v -> replaceFragment(FragmentInicioV())
            R.id.op_mi_tienda_v -> replaceFragment(FragmentMiTiendaV())
            R.id.op_categotias_v -> replaceFragment(FragmentCategoriasV())
            R.id.op_resenia_v -> replaceFragment(FragmentReseniasV())
            R.id.op_cerrar_sesion_v -> {
                cerrarSesion()
            }
            R.id.op_mis_productos_v -> replaceFragment(FragmentMisProductosV())
            R.id.op_mis_ordenes_v -> replaceFragment(FragmentOrdenesV())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
