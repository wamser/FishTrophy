package com.fishtrophy.ui.equipamento

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentEquipamentoImagemBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class EquipamentoImagemFragment: Fragment() {
    private val args : EquipamentoImagemFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoImagemBinding? = null
    private val binding: FragmentEquipamentoImagemBinding get() = _binding!!
    private var idEquipamento= 0L
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tentaCarregarEquipamento()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        _binding = FragmentEquipamentoImagemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if(idEquipamento.toInt()!=0) {
            buscaEquipamento()
        }
        //toolBar.visibility = View.GONE
        navView.visibility = View.GONE

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscaEquipamento() {
        lifecycleScope.launch {
            equipamentoDao.buscaPorId(idEquipamento).collect{ it->
                //withContext(Dispatchers.Main) {
                it?.let {

                    preencheCampos(it)
                }

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun tentaCarregarEquipamento() {
        idEquipamento= args.idEquipamento.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(EquipamentoCarregado: Equipamento) {

        binding.fragmentEquipamentoImagemImageview.tentaCarregarImagem(EquipamentoCarregado.diretorioImagem)

    }

}