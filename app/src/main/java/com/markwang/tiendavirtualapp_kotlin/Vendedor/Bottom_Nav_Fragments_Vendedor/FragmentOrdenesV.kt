package com.markwang.tiendavirtualapp_kotlin.Vendedor.Bottom_Nav_Fragments_Vendedor

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.markwang.tiendavirtualapp_kotlin.Adaptadores.AdapdadorOrdenCompraV
import com.markwang.tiendavirtualapp_kotlin.Modelos.ModeloOrdenCompra
import com.markwang.tiendavirtualapp_kotlin.R
import com.markwang.tiendavirtualapp_kotlin.databinding.FragmentOrdenesVBinding


class FragmentOrdenesV : Fragment() {

    private lateinit var binding : FragmentOrdenesVBinding
    private lateinit var mContext : Context

    private lateinit var ordenesArrayList : ArrayList<ModeloOrdenCompra>
    private lateinit var ordenAdapdador : AdapdadorOrdenCompraV

    private var handler = Handler(Looper.getMainLooper())
    private var searchRunnable : Runnable? = null

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentOrdenesVBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verOrdenes()

        binding.IbFiltroEstado.setOnClickListener {
            filtrarOrdenMenu()
        }

        binding.etBuscarOrdenId.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(id : CharSequence?, start: Int, before: Int, count: Int) {
                val idOrden = id.toString()

                searchRunnable?.let { handler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    if (idOrden.isNotEmpty()){
                        /*Comprobamos que no sea campo vacio*/
                        buscarOrdenPorId(idOrden)
                    }else{
                        /*Si el campo está vacio que muestre todos los pedidos*/
                        verOrdenes()
                    }
                }

                handler.postDelayed(searchRunnable!!, 1000)



            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun buscarOrdenPorId(idOrden: String) {
        ordenesArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
            .orderByChild("idOrden").startAt(idOrden).endAt(idOrden + "\uf8ff")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Corregido: Usar if para comprobar si existe
                if (snapshot.exists()) {
                    /*Si el id del pedido existe*/
                    ordenesArrayList.clear()
                    for (ds in snapshot.children){
                        val modelo = ds.getValue(ModeloOrdenCompra::class.java)
                        ordenesArrayList.add(modelo!!)
                    }

                    ordenAdapdador = AdapdadorOrdenCompraV(mContext, ordenesArrayList)
                    binding.ordenesRv.adapter = ordenAdapdador
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun filtrarOrdenMenu() {
        val popupMenu = PopupMenu(mContext, binding.IbFiltroEstado)

        popupMenu.menu.add(Menu.NONE, 0,0,"Todos")
        popupMenu.menu.add(Menu.NONE, 1,1,"Solicitud recibida")
        popupMenu.menu.add(Menu.NONE, 2,2,"En preparación")
        popupMenu.menu.add(Menu.NONE, 3,3,"Entregado")
        popupMenu.menu.add(Menu.NONE, 4,4,"Cancelado")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val itemId = item.itemId

            when(itemId){
                0-> verOrdenes()
                1-> filtrarOrden("Solicitud recibida")
                2-> filtrarOrden("En preparación")
                3-> filtrarOrden("Entregado")
                4-> filtrarOrden("Cancelado")
            }


            return@setOnMenuItemClickListener true
        }




    }

    private fun filtrarOrden(estado: String) {
        ordenesArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()

                for (ds in snapshot.children){

                    val modeloOrden = ds.getValue(ModeloOrdenCompra::class.java)
                    when(modeloOrden?.estadoOrden){
                        //Filtrar pedido según el estado
                        estado  -> ordenesArrayList.add(modeloOrden)
                    }
                }

                ordenAdapdador = AdapdadorOrdenCompraV(mContext, ordenesArrayList)
                binding.ordenesRv.adapter = ordenAdapdador
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun verOrdenes() {
        ordenesArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Ordenes")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                ordenesArrayList.clear()
                for (ds in snapshot.children){
                    val modelo = ds.getValue(ModeloOrdenCompra::class.java)
                    ordenesArrayList.add(modelo!!)
                }

                ordenAdapdador = AdapdadorOrdenCompraV(mContext, ordenesArrayList)
                binding.ordenesRv.adapter = ordenAdapdador
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}