package com.fishtrophy.ui.equipamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentEquipamentoDetalheBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento
import com.fishtrophy.model.Peixe
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EquipamentoDetalhesFragment : Fragment() {


    private var idEquipamento: Long = 0L
    private var animal: Peixe? = null
    private val args : EquipamentoDetalhesFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoDetalheBinding? = null
    private val binding: FragmentEquipamentoDetalheBinding get() = _binding!!
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipamentoDetalheBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        navView.visibility = View.GONE
        tentaCarregarEquipamento()

        binding.fragmentDetalhesEquipamentoImagem.setOnClickListener(){
            val direction = EquipamentoDetalhesFragmentDirections.actionEquipamentoDetalhesFragmentToEquipamentoImagemFragment(
                idEquipamento.toInt()
            )
            findNavController().navigate(direction)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        buscaEquipamento()
    }

    private fun buscaEquipamento() {
        lifecycleScope.launch {
            equipamentoDao.buscaPorId(idEquipamento).collect{ animal->
                withContext(Main) {
                    animal?.let {
                        preencheCampos(it)
                    }
                    //val navController = findNavController()
                    //navController.navigate(R.id.nav_animal)
                }
            }
        }

    }

    private fun tentaCarregarEquipamento() {
        idEquipamento= args.idEquipamento.toLong()//intent.getLongExtra(CHAVE_PRODUTO_ID,0L)
    }

    private fun preencheCampos(equipamentoCarregado: Equipamento) {

        var tipo = ""

        with(binding) {

            if(equipamentoCarregado.tipo=="1"){
                tipo ="Isca"
            }else if(equipamentoCarregado.tipo=="2"){
                tipo ="Vara"
            }else if(equipamentoCarregado.tipo=="3"){
                tipo ="Carretilha"
            }else if(equipamentoCarregado.tipo=="4"){
                tipo ="Molinete"
            }

            binding.fragmentDetalhesEquipamentoImagem.tentaCarregarImagem(equipamentoCarregado.diretorioImagem)
            binding.fragmentDetalhesEquipamentoTipo.text = "Tipo: "+tipo
            binding.fragmentDetalhesEquipamentoId.text = "ID: "+equipamentoCarregado.id.toString()
            binding.fragmentDetalhesEquipamentoDescricao.text = "Descrição: "+equipamentoCarregado.descricao.toString()
        }
    }


    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalhes_produto, menu)
        return super.onCreateOptionsMenu(menu)
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.menu_detalhes_produto_editar -> {
                Intent(this, FormularioProdutoActivity::class.java).apply {
                    putExtra(CHAVE_PRODUTO_ID,idProduto)
                    startActivity(this)
                }
            }
            R.id.menu_detalhes_produto_excluir -> {
                lifecycleScope.launch {
                    produto?.let { produtoDao.remove(it) }
                    finish()
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }*/


    override fun onDestroy() {
        super.onDestroy()
        //Log.i(TAG, "onDestroy: ")
    }

}