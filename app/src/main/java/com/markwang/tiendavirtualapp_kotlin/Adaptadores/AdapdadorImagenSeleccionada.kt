package com.markwang.tiendavirtualapp_kotlin.Adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloImagenSeleccionada
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.ItemIamgenesSeleccionadasBinding
import java.security.PrivateKey


class AdapdadorImagenSeleccionada (
    private val context : Context,
    private val imagenesSelecArrayList: ArrayList<ModeloImagenSeleccionada>,
    private val idProducto : String
): Adapter<AdapdadorImagenSeleccionada.HolderImagenSeleccionada>(){
    private lateinit var binding: ItemIamgenesSeleccionadasBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagenSeleccionada {
        binding = ItemIamgenesSeleccionadasBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderImagenSeleccionada(binding.root)
    }

    override fun getItemCount(): Int {
        return imagenesSelecArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImagenSeleccionada, position: Int) {
        val modelo = imagenesSelecArrayList[position]

        if (modelo.deInternet){
            try {
                val imagenUrl = modelo.imagenUrl
                Glide.with(context)
                    .load(imagenUrl)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.imagenItem)
            }catch (e:Exception){

            }
        }else{
            //Leyendo la imagen
            val imagenUri = modelo.imagenUri
            try {
                Glide.with(context)
                    .load(imagenUri)
                    .placeholder(R.drawable.item_imagen)
                    .into(holder.imagenItem)
            }catch (e:Exception){

            }
        }



        //Evento para eliminar una imgane de la lista
        holder.btn_borrar.setOnClickListener {
            if (modelo.deInternet){
                eliminarImagenFirebase(modelo, position)
            }
            imagenesSelecArrayList.remove(modelo)
            notifyDataSetChanged()
        }
    }

    private fun eliminarImagenFirebase(
        modelo: ModeloImagenSeleccionada,
        position: Int) {

        val idImagen = modelo.id

        val ref = FirebaseDatabase.getInstance().getReference("Productos")
        ref.child(idProducto).child("Imagenes").child(idImagen)
            .removeValue()
            .addOnSuccessListener {
                try {
                    imagenesSelecArrayList.remove(modelo)
                    notifyItemRemoved(position)
                    eliminarImagenStorage(modelo)
                }catch (e:Exception){
                    Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun eliminarImagenStorage(modelo: ModeloImagenSeleccionada) {
        val rutaImagen = "Productos/"+modelo.id

        val ref = FirebaseStorage.getInstance().getReference(rutaImagen)
        ref.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Imagen eliminada", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    inner class HolderImagenSeleccionada(itemView : View) : ViewHolder(itemView){
        var imagenItem = binding.imagenItem
        var btn_borrar = binding.borrarItem
    }


}