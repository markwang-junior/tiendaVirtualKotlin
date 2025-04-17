package com.markwang.tiendavirtualapp_kotlin.Cliente

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
    private var firebaseAuth : FirebaseAuth? = null
    private lateinit var prefs: SharedPreferences
    private val PREFS_NAME = "TiendaVirtualPrefs"
    private val USER_TYPE_KEY = "user_type"
    private val USER_ID_KEY = "user_id"
    private val USER_LOGGED_IN = "user_logged_in"
    private val AUTH_PROVIDER = "auth_provider"

    private var dobleClick = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Cambiado a androidx.appcompat.widget.Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        firebaseAuth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        comprobarSesion()

        binding.navigationView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (dobleClick){
                    /*Salimos de la app*/
                    finish()
                    return
                }

                dobleClick = true
                Toast.makeText(this@MainActivityCliente, "Presione nuevamente para salir", Toast.LENGTH_SHORT).show()

                handler.postDelayed({dobleClick = false}, 2000)
            }
        })

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Verificar si viene con la bandera ABRIR_PERFIL
        if (intent.getBooleanExtra("ABRIR_PERFIL", false)) {
            // Mostrar el fragmento de perfil
            replaceFragment(FragmentMiPerfilC())
            binding.navigationView.setCheckedItem(R.id.op_mi_perfil_c)

            // Mostrar mensaje informativo
            Toast.makeText(this,
                "Por favor completa tu información de perfil para una mejor experiencia",
                Toast.LENGTH_LONG).show()
        } else {
            // Comportamiento normal - inicializar con fragmento de inicio
            replaceFragment(FragmentInicioC()) // ✅ Se inicializa con el fragmento de inicio
        }
    }

    private fun comprobarSesion(){
        if (firebaseAuth!!.currentUser == null){
            // No hay sesión, limpiar preferencias y redirigir
            limpiarPreferencias()
            startActivity(Intent(this@MainActivityCliente, SeleccionarTipoActivity::class.java))
            finishAffinity()
        } else {
            // Hay sesión, actualizar estado de preferencias
            val editor = prefs.edit()
            editor.putBoolean(USER_LOGGED_IN, true)
            editor.putString(USER_ID_KEY, firebaseAuth!!.currentUser!!.uid)
            editor.apply()

            Toast.makeText(this,"Usuario en linea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion(){
        // Limpiar las preferencias al cerrar sesión
        limpiarPreferencias()

        // Cerrar sesión en Firebase
        firebaseAuth!!.signOut()

        // Redirigir a pantalla de selección de tipo
        startActivity(Intent(this@MainActivityCliente, SeleccionarTipoActivity::class.java))
        finishAffinity()
        Toast.makeText(this,"Cerraste sesión", Toast.LENGTH_SHORT).show()
    }

    private fun limpiarPreferencias() {
        val editor = prefs.edit()
        editor.remove(USER_TYPE_KEY)
        editor.remove(USER_ID_KEY)
        editor.remove(USER_LOGGED_IN)
        editor.remove(AUTH_PROVIDER)
        editor.apply()
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