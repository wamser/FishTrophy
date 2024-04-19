package com.fishtrophy.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.fishtrophy.R
import com.fishtrophy.databinding.FragmentEquipamentoPesquisaItemBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento

class ListaEquipamentoPesquisaAdapter(
    private val context: Context,
    equipamento: List<Equipamento> = emptyList(),
    var quandoClicaNoItemListener: (equipamento: Equipamento) -> Unit = {}
) : RecyclerView.Adapter<ListaEquipamentoPesquisaAdapter.ViewHolder>() {

    private var equipamentosPesquisa = equipamento.toMutableList()
    private var equipamentosPesquisaFiltrados = equipamento.toMutableList()


    inner class ViewHolder(private val binding: FragmentEquipamentoPesquisaItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var equipamento: Equipamento

        init {
            itemView.setOnClickListener {
                if (::equipamento.isInitialized) {
                    quandoClicaNoItemListener(equipamento)
                }
            }

            itemView.setOnLongClickListener {
                if (::equipamento.isInitialized) {
                    mostraPopUp(itemView)
                }
                return@setOnLongClickListener true
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun vincula(equipamento: Equipamento) {
            this.equipamento = equipamento

            val descricao = binding.equipamentoPesquisaItemDescricao
            descricao.text = equipamento.descricao

            val tipo = binding.equipamentoPesquisaItemTipo
            when (equipamento.tipo) {
                "1" -> {
                    tipo.text = "Isca"
                }
                "2" -> {
                    tipo.text = "Vara"
                }
                "3" -> {
                    tipo.text = "Carretilha"
                }
                "4" -> {
                    tipo.text = "Molinete"
                }
            }

            val visibilidade = if (equipamento.diretorioImagem != "") {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.equipamentoPesquisaItemImagem.visibility = visibilidade
            binding.equipamentoPesquisaItemImagem.tentaCarregarImagem(equipamento.diretorioImagem)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = FragmentEquipamentoPesquisaItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val equipamento = equipamentosPesquisaFiltrados[position]
        holder.vincula(equipamento)
    }

    override fun getItemCount(): Int = equipamentosPesquisaFiltrados.size

    fun atualiza(equipamentos: List<Equipamento>) {
        this.equipamentosPesquisa.clear()
        this.equipamentosPesquisa.addAll(equipamentos)
        this.equipamentosPesquisaFiltrados.clear()
        this.equipamentosPesquisaFiltrados.addAll(equipamentos)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filtra(query: String) {

        this.equipamentosPesquisaFiltrados =
            this.equipamentosPesquisa.filter { it.descricao.contains(query, ignoreCase = true) || it.tipo.contains(query, ignoreCase = true) }
                .toMutableList()

        notifyDataSetChanged()
    }

    fun mostraPopUp(v: View) {
        val popup = PopupMenu(v.context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_detalhes_peixe, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.menu_detalhes_lista_peixe_editar -> {

                }

                R.id.menu_detalhes_lista_peixe_excluir -> {

                }
            }

            true
        }
        popup.show()
    }


}