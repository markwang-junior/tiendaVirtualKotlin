package com.markwang.tiendavirtualapp_kotlin.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloProductoOrden
import com.markwang.tiendavirtualapp_kotlin.databinding.ItemProductoOrdenBinding

class AdapdadorProductoOrden : RecyclerView.Adapter<AdapdadorProductoOrden.HolderProductoOrden> {

    private lateinit var binding : ItemProductoOrdenBinding
    private var mContext : Context
    private var productosArrayList : ArrayList<ModeloProductoOrden>

    constructor(mContext: Context, productosArrayList: ArrayList<ModeloProductoOrden>) {
        this.mContext = mContext
        this.productosArrayList = productosArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderProductoOrden {
        binding = ItemProductoOrdenBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderProductoOrden(binding.root)
    }

    override fun getItemCount(): Int {
        return productosArrayList.size
    }

    override fun onBindViewHolder(holder: HolderProductoOrden, position: Int) {
        val modeloProductoOrden = productosArrayList[position]

        val nombre = modeloProductoOrden.nombre
        val precio = modeloProductoOrden.precio

        holder.itemNombreP.text = nombre
        holder.itemPrecioP.text = precio
    }


    inner class HolderProductoOrden (itemView : View) : RecyclerView.ViewHolder(itemView){
        var itemNombreP = binding.itemNombreP
        var itemPrecioP = binding.itemPrecioP
    }


}