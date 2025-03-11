package com.markwang.tiendavirtualapp_kotlin.Cliente.Nav_Fragments_cliente

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.markwang.tiendavirtualapp_kotlin.Cliente.ActualizarPasswordActivity
import com.markwang.tiendavirtualapp_kotlin.Constantes
import com.markwang.tiendavirtualapp_kotlin.Mapas.SeleccionarUbicacionActivity
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentMiPerfilCBinding
import android.util.Log

class FragmentMiPerfilC : Fragment() {

    private lateinit var binding: FragmentMiPerfilCBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private var imagenUri : Uri?=null

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMiPerfilCBinding.inflate(layoutInflater, container, false)

        binding.imgPerfil.setOnClickListener{
            seleccionarImg()
        }

        binding.ubicacion.setOnClickListener {
            val intent = Intent(mContext, SeleccionarUbicacionActivity::class.java)
            obtenerUbicacion_ARL.launch(intent)
        }

        binding.btnGuardarInfo.setOnClickListener {
            actualizarInfo()
        }

        binding.btnActualizarContraseA.setOnClickListener {
            startActivity(Intent(mContext, ActualizarPasswordActivity::class.java))
        }

        return binding.root
    }

    private var nombres = ""
    private var email = ""
    private var dni = ""
    private var telefono = ""
    private fun actualizarInfo() {
        nombres = binding.nombresCperfil.text.toString().trim()
        email = binding.emailCPerfil.text.toString().trim()
        dni = binding.dniCPerfil.text.toString().trim()
        telefono = binding.telefonoCPerfil.text.toString().trim()
        direccion = binding.ubicacion.text.toString().trim()

        // Validar DNI (8 números y 1 letra)
        val dniRegex = Regex("^[0-9]{8}[A-Za-z]\$")
        if (!dni.matches(dniRegex)) {
            Toast.makeText(mContext, "El DNI debe tener 8 números seguidos de 1 letra", Toast.LENGTH_SHORT).show()
            return
        }

        val hashMap: HashMap<String, Any> = HashMap()

        hashMap["nombres"] = "${nombres}"
        hashMap["email"] = "${email}"
        hashMap["dni"] = "${dni}"
        hashMap["telefono"] = "${telefono}"
        hashMap["direccion"] = "${direccion}"
        hashMap["latitud"] = "${latitud}"
        hashMap["longitud"] = "${longitud}"


        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Se actualizó la información", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        leerInformacion()
    }

    private fun leerInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Obtener los datos del usuario
                    val nombres = "${snapshot.child("nombres").value}"
                    val email = "${snapshot.child("email").value}"
                    val dni = "${snapshot.child("dni").value}"
                    val imagen = "${snapshot.child("imagen").value}"
                    val telefono = "${snapshot.child("telefono").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"
                    val fechaRegistro = "${snapshot.child("tRegistro").value}"
                    val direccion = "${snapshot.child("direccion").value}"

                    // Configurar los campos
                    binding.nombresCperfil.setText(nombres)
                    binding.emailCPerfil.setText(email)
                    binding.dniCPerfil.setText(dni)
                    binding.telefonoCPerfil.setText(telefono)
                    binding.ubicacion.setText(direccion)

                    try {
                        Glide.with(mContext)
                            .load(imagen)
                            .placeholder(R.drawable.img_perfil)
                            .into(binding.imgPerfil)
                    } catch (e: Exception) {
                        // Manejo de errores
                    }

                    // Usar proveedores de autenticación directamente desde Firebase
                    val user = firebaseAuth.currentUser
                    val proveedores = user?.providerData?.map { it.providerId } ?: listOf()

                    Log.d("PerfilDebug", "Proveedores: $proveedores, Campo proveedor: $proveedor")

                    when {
                        proveedores.contains("google.com") -> {
                            binding.proveedorCPerfil.text = "La cuenta fue creada a través de una cuenta de Google"
                            binding.emailCPerfil.isEnabled = false
                            binding.btnActualizarContraseA.visibility = View.GONE
                        }
                        proveedores.contains("phone") -> {
                            binding.proveedorCPerfil.text = "La cuenta fue creada a través de su número de teléfono"
                            binding.telefonoCPerfil.isEnabled = false
                            binding.btnActualizarContraseA.visibility = View.GONE
                        }
                        proveedores.contains("password") -> {
                            binding.proveedorCPerfil.text = "La cuenta fue creada a través de su email"
                            binding.emailCPerfil.isEnabled = false
                            binding.btnActualizarContraseA.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.proveedorCPerfil.text = "Método de registro: $proveedor"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(mContext, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun seleccionarImg() {
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                resultadoImg.launch(intent)
            }
    }

    private var resultadoImg =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                imagenUri = data!!.data
                subirImagenStorage(imagenUri)
            }else{
                Toast.makeText(mContext, "Acción cancelada", Toast.LENGTH_SHORT).show()
            }
        }

    private fun subirImagenStorage(imagenUri: Uri?) {
        val rutaImagen = "imagenesPerfil/"+firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.putFile(imagenUri!!)
            .addOnSuccessListener { taskSnapShot ->
                val uriTask = taskSnapShot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlImagenCargada = uriTask.result.toString()
                if (uriTask.isSuccessful){
                    actualizarImagenBD(urlImagenCargada)
                }
            }
            .addOnFailureListener {e->
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private fun actualizarImagenBD(urlImagenCargada: String) {
        val hashMap : HashMap<String, Any> = HashMap()
        if (imagenUri!=null){
            hashMap["imagen"] = urlImagenCargada
        }

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Toast.makeText(mContext, "Su imagen de perfil se ha actualizado", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e->
                Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()

            }

    }

    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""
    private val obtenerUbicacion_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){resultado->
            if (resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                if (data!=null){
                    latitud = data.getDoubleExtra("latitud", 0.0)
                    longitud = data.getDoubleExtra("longitud", 0.0)
                    direccion = data.getStringExtra("direccion") ?: ""

                    binding.ubicacion.setText(direccion)
                }
            }
        }


}