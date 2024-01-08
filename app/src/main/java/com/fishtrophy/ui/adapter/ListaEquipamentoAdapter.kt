package com.fishtrophy.ui.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation.findNavController

import androidx.recyclerview.widget.RecyclerView
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.EquipamentoItemBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento
import com.fishtrophy.ui.equipamento.EquipamentoFragmentDirections
import com.fishtrophy.ui.peixe.PeixeFragmentDirections


import kotlinx.coroutines.launch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class ListaEquipamentoAdapter(
    private val context: Context,
    equipamento: List<Equipamento> = emptyList(),
    var quandoClicaNoItemListener: (equipamento: Equipamento) -> Unit = {}
) : RecyclerView.Adapter<ListaEquipamentoAdapter.ViewHolder>() {

    private val equipamentos = equipamento.toMutableList()

    private val equipamentoDao by lazy {
        AppDatabase.instancia(this.context).equipamentoDao()
    }



    inner class ViewHolder(private val binding: EquipamentoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var equipamento: Equipamento

        init {
            itemView.setOnClickListener {
                if(::equipamento.isInitialized) {
                    quandoClicaNoItemListener(equipamento)
                }
            }

            itemView.setOnLongClickListener {
                if(::equipamento.isInitialized) {
                    showPopup(itemView,equipamento)
                }
                return@setOnLongClickListener true
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun vincula(equipamento: Equipamento) {
            this.equipamento = equipamento

            val descricao = binding.equipamentoItemDescricao
            descricao.text = equipamento.descricao

            val tipo = binding.equipamentoItemTipo
            if(equipamento.tipo=="1"){
                tipo.text ="Isca"
            }else if(equipamento.tipo=="2"){
                tipo.text ="Vara"
            }else if(equipamento.tipo=="3"){
                tipo.text ="Carretilha"
            }else if(equipamento.tipo=="4"){
                tipo.text ="Molinete"
            }

            val visibilidade = if(equipamento.diretorioImagem != ""){
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.equipamentoItemImagem.visibility = visibilidade
            binding.equipamentoItemImagem.tentaCarregarImagem(equipamento.diretorioImagem)
        }




    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = EquipamentoItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val equipamento = equipamentos[position]
        holder.vincula(equipamento)
    }

    override fun getItemCount(): Int = equipamentos.size

    fun atualiza(equipamentos: List<Equipamento>) {
        this.equipamentos.clear()
        this.equipamentos.addAll(equipamentos)
        notifyDataSetChanged()
    }

    fun showPopup(v: View, equipamento: Equipamento) {
        val popup = PopupMenu(v.context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_detalhes_peixe, popup.menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.menu_detalhes_lista_peixe_editar -> {
                    val direction = EquipamentoFragmentDirections.actionNavigationEquipamentoToEquipamentoCadastroFragment(equipamento.id.toInt())


                    findNavController(v).navigate(direction)
                }
                R.id.menu_detalhes_lista_peixe_excluir -> {


                    CoroutineScope(Dispatchers.Main).launch {

                        equipamento?.let { equipamentoDao.remove(it) }


                    }
                    Toast.makeText(context, "Registro excluído com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            true
        })
        popup.show()
    }



}