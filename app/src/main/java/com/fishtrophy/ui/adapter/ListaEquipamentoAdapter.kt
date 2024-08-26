package com.fishtrophy.ui.adapter


import android.annotation.SuppressLint
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ListaEquipamentoAdapter(
    private val context: Context,
    equipamento: List<Equipamento> = emptyList(),
    var quandoClicaNoItemListener: (equipamento: Equipamento) -> Unit = {},
) : RecyclerView.Adapter<ListaEquipamentoAdapter.ViewHolder>() {

    private var equipamentos = equipamento.toMutableList()
    private var equipamentosFiltrados = equipamento.toMutableList()

    private val equipamentoDao by lazy {
        AppDatabase.instancia(this.context).equipamentoDao()
    }
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.context)
        db.peixeDao()
    }

    inner class ViewHolder(private val binding: EquipamentoItemBinding) :
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

                    mostraPopup(itemView, equipamento)
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
        val equipamento = equipamentosFiltrados[position]
        holder.vincula(equipamento)
    }

    override fun getItemCount(): Int = equipamentosFiltrados.size

    fun atualiza(equipamentos: List<Equipamento>) {
        this.equipamentos.clear()
        this.equipamentos.addAll(equipamentos)
        this.equipamentosFiltrados.clear()
        this.equipamentosFiltrados.addAll(equipamentos)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filtra(query: String) {

        this.equipamentosFiltrados = this.equipamentos.filter {
            it.descricao.contains(
                query, ignoreCase = true
            ) || it.tipo.contains(query, ignoreCase = true)
        }.toMutableList()

        notifyDataSetChanged()
    }

    fun mostraPopup(v: View, equipamento: Equipamento) {
        val popup = PopupMenu(v.context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_detalhes_peixe, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.menu_detalhes_lista_peixe_editar -> {
                    val direction =
                        EquipamentoFragmentDirections.actionNavigationEquipamentoToEquipamentoCadastroFragment(
                            equipamento.id.toInt()
                        )


                    findNavController(v).navigate(direction)
                }

                R.id.menu_detalhes_lista_peixe_excluir -> {


                    deletar(equipamento)


                }
            }

            true
        }
        popup.show()
    }

    private fun deletar(equipamento: Equipamento) {

        equipamento.let {
            validaEquipamentoNaoExisteNoPeixe(it)
        }

    }

    private fun validaEquipamentoNaoExisteNoPeixe(it: Equipamento) {

        CoroutineScope(Dispatchers.IO).launch {

            try {
                peixeDao.buscaPeixePorEquipamento(it.id).collect { peixes ->
                    if (peixes.isEmpty()) {
                        equipamentoDao.remove(it)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context, "Registro excluído com sucesso!", Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                "Este equipamento se encontra vinculado a um peixe.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle deletion error
            }
        }
      }
}


