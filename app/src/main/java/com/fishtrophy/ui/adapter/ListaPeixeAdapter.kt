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
import com.fishtrophy.databinding.PeixeItemBinding
import com.fishtrophy.extentions.formataDuasCasas
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Peixe
import com.fishtrophy.ui.peixe.PeixeFragmentDirections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


class ListaPeixeAdapter(
    private val context: Context,
    peixes: List<Peixe> = emptyList(),
    var quandoClicaNoItemListener: (animal: Peixe) -> Unit = {}
) : RecyclerView.Adapter<ListaPeixeAdapter.ViewHolder>() {

    private var peixes = peixes.toMutableList()
    private var peixesFiltrados = peixes.toMutableList()

    private val peixeDao by lazy {
        AppDatabase.instancia(this.context).peixeDao()
    }

    inner class ViewHolder(private val binding: PeixeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var peixe: Peixe

        init {
            itemView.setOnClickListener {
                if(::peixe.isInitialized) {
                    quandoClicaNoItemListener(peixe)
                }
            }

            itemView.setOnLongClickListener {
                if(::peixe.isInitialized) {
                    mostraPopUp(itemView,peixe)
                }
                return@setOnLongClickListener true
            }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun vincula(peixe: Peixe) {
            this.peixe = peixe

            val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val formattedDate = peixe.dataCaptura.format(formatterDate)

            val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
            val formattedTime = peixe.horaCaptura.format(formatterTime)

            binding.peixeItemDataHora.setText("$formattedDate | $formattedTime")

            val tamanho = binding.peixeItemTamanho
            tamanho.text = peixe.tamanho.formataDuasCasas()+" CM"

            val peso = binding.peixeItemPeso
            peso.text = peixe.peso.formataDuasCasas()+" KG"

            val visibilidade = View.VISIBLE

            binding.peixeItemImagem.visibility = visibilidade
            binding.peixeItemImagem.tentaCarregarImagem(peixe.diretorioImagem)
            binding.peixeItemEspecie.setText(peixe.especie)
       }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = PeixeItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val peixe = peixesFiltrados[position]
        holder.vincula(peixe)
    }

    override fun getItemCount(): Int = peixesFiltrados.size

    fun atualiza(peixes: List<Peixe>) {
        this.peixes.clear()
        this.peixes.addAll(peixes)
        this.peixesFiltrados.clear()
        this.peixesFiltrados.addAll(peixes)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filtra(query: String) {

        this.peixesFiltrados =
            this.peixes.filter { it.especie!!.contains(query, ignoreCase = true) }
                .toMutableList()

        notifyDataSetChanged()
    }

    fun mostraPopUp(v: View, peixe: Peixe) {
        val popup = PopupMenu(v.context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_detalhes_peixe, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.menu_detalhes_lista_peixe_editar -> {
                    val direction =
                        PeixeFragmentDirections.actionNavigationPeixeToPeixeCadastroFragment(
                            peixe.id.toInt()
                        )
                    findNavController(v).navigate(direction)

                }

                R.id.menu_detalhes_lista_peixe_excluir -> {


                    CoroutineScope(Dispatchers.Main).launch {

                        peixe.let { peixeDao.remove(it) }

                    }
                    Toast.makeText(context, "Registro excluído com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            true
        }
        popup.show()
    }

}