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
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EquipamentoDetalhesFragment : Fragment() {


    private var idEquipamento: Long = 0L
    private val args : EquipamentoDetalhesFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoDetalheBinding? = null
    private val binding: FragmentEquipamentoDetalheBinding get() = _binding!!
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

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

        binding.fragmentEquipamentoDetalhesImagem.setOnClickListener{
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
                }
            }
        }

    }

    private fun tentaCarregarEquipamento() {
        idEquipamento= args.idEquipamento.toLong()
    }

    private fun preencheCampos(equipamentoCarregado: Equipamento) {

        var tipo = ""

        when (equipamentoCarregado.tipo) {
            "1" -> {
                tipo ="Isca"
            }
            "2" -> {
                tipo ="Vara"
            }
            "3" -> {
                tipo ="Carretilha"
            }
            "4" -> {
                tipo ="Molinete"
            }
        }

        binding.fragmentEquipamentoDetalhesImagem.tentaCarregarImagem(equipamentoCarregado.diretorioImagem)
        binding.fragmentEquipamentoDetalhesTipo.text = tipo
        binding.fragmentEquipamentoDetalhesDescricao.text = equipamentoCarregado.descricao
    }
}