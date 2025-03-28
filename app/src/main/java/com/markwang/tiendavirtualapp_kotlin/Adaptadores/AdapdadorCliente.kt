package com.markwang.tiendavirtualapp_kotlin.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloUsuario
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ItemClienteBinding

class AdapdadorCliente : RecyclerView.Adapter<AdapdadorCliente.HolderUsuario>{

    private lateinit var binding : ItemClienteBinding

    private var mContext : Context
    private var usuarioArrayList : ArrayList<ModeloUsuario>

    constructor(mContext: Context, usuarioArrayList: ArrayList<ModeloUsuario>) {
        this.mContext = mContext
        this.usuarioArrayList = usuarioArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {
        binding = ItemClienteBinding.inflate(LayoutInflater.from(mContext),parent, false)
        return  HolderUsuario(binding.root)
    }

    override fun getItemCount(): Int {
        return usuarioArrayList.size
    }

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val modeloUsuario = usuarioArrayList[position]

        val imagenU = modeloUsuario.imagen
        val nombresU = modeloUsuario.nombres
        val emailU = modeloUsuario.email
        val dniU = modeloUsuario.dni
        val ubicacionU = modeloUsuario.direccion
        val telefonoU = modeloUsuario.telefono
        val proveedor = modeloUsuario.proveedor

        holder.nombres.text =  "${nombresU}"
        holder.email.text =  "${emailU}"
        holder.dni.text =  "${dniU}"
        holder.ubicacion.text =  "${ubicacionU}"
        holder.telefono.text =  "${telefonoU}"
        holder.proveedor.text =  "${proveedor}"

        try {
            Glide.with(mContext)
                .load(imagenU)
                .placeholder(R.drawable.img_perfil)
                .into(holder.imagen)
        }catch (e: Exception){

        }

    }

    inner class HolderUsuario(itemView : View) : RecyclerView.ViewHolder(itemView){
        var imagen = binding.imagenC
        var nombres = binding.nombresCperfil
        var email = binding.emailCPerfil
        var dni = binding.dniCPerfil
        var ubicacion = binding.ubicacion
        var telefono = binding.telefonoCPerfil
        var proveedor = binding.proveedorCPerfil

    }


}