package com.markwang.tiendavirtualapp_kotlin.Adaptadores

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloImgSlider
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ItemImagenSliderBinding

class AdapdadorImgSlider : RecyclerView.Adapter<AdapdadorImgSlider.HolderImagenSlider> {

    private lateinit var binding: ItemImagenSliderBinding
    private var context: Context
    private var imagenArrayList: ArrayList<ModeloImgSlider>

    constructor(context: Context, imagenArrayList: ArrayList<ModeloImgSlider>) {
        this.context = context
        this.imagenArrayList = imagenArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSlider {
        binding = ItemImagenSliderBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderImagenSlider(binding.root)
    }

    override fun getItemCount(): Int {
        return imagenArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSlider, position: Int) {
        val modeloImagenSlider = imagenArrayList[position]

        val imagenUrl = modeloImagenSlider.imagenUrl
        val imagenContador = "${position + 1}/${imagenArrayList.size}"
        holder.imagenContadorTv.text = imagenContador

        try {
            Glide.with(context)
                .load(imagenUrl)
                .placeholder(R.drawable.item_img_producto)
                .into(holder.imagenSIV)
        } catch (e: Exception) {

        }

        holder.itemView.setOnClickListener {
            zoomImg(imagenUrl)
        }
    }

    inner class HolderImagenSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenSIV: ShapeableImageView = binding.imagenSIV
        val imagenContadorTv: TextView = binding.imagenContadorTv
    }

    private fun zoomImg(imagen : String){
        val pv : PhotoView
        val btnCerrar : MaterialButton

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.zoom_imagen)

        pv = dialog.findViewById(R.id.zoomImg)
        btnCerrar = dialog.findViewById(R.id.cerrarZoom)

        try {
            Glide.with(context)
                .load(imagen)
                .placeholder(R.drawable.item_img_producto)
                .into(pv)
        }catch (e:Exception){

        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
    }




}