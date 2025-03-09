package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.markwang.tiendavirtualapp_kotlin.Cliente.Bottom_Nav_Fragments_Cliente.FragmentMisOrdenesC
import com.markwang.tiendavirtualapp_kotlin.Cliente.Bottom_Nav_Fragments_Cliente.FragmentTiendaC
import com.markwang.tiendavirtualapp_kotlin.Cliente.Nav_Fragments_cliente.FragmentInicioC
import com.markwang.tiendavirtualapp_kotlin.Cliente.Nav_Fragments_cliente.FragmentMiPerfilC
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ActivityMainClienteBinding
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.markwang.tiendavirtualapp_kotlin.SeleccionarTipoActivity


class MainActivityCliente : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainClienteBinding
    private var firebaseAuth : FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Cambiado a androidx.appcompat.widget.Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()


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

        replaceFragment(FragmentInicioC()) // ✅ Se inicializa con el fragmento de inicio
    }

    private fun comprobarSesion(){
        if (firebaseAuth!!.currentUser==null){
            startActivity(Intent(this@MainActivityCliente, SeleccionarTipoActivity::class.java))
            finishAffinity()
        }else {
            Toast.makeText(this,"Usuario en linea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion(){
        firebaseAuth!!.signOut()
        startActivity(Intent(this@MainActivityCliente, SeleccionarTipoActivity::class.java))
        finishAffinity()
        Toast.makeText(this,"Cerraste sesión", Toast.LENGTH_SHORT).show()

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navFragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.op_inicio_c -> {
                replaceFragment(FragmentInicioC())
            }
            R.id.op_mi_perfil_c -> {
                replaceFragment(FragmentMiPerfilC())
            }
            R.id.op_cerrar_sesion_c -> {
                cerrarSesion()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}