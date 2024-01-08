package com.fishtrophy.ui.peixe

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
import com.fishtrophy.databinding.FragmentPeixeImagemBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Peixe
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class PeixeImagemFragment : Fragment() {
    private val args : PeixeImagemFragmentArgs by navArgs()
    private var _binding: FragmentPeixeImagemBinding? = null
    private val binding: FragmentPeixeImagemBinding get() = _binding!!
    private var idPeixe= 0L
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tentaCarregarPeixe()
        //val toolBar = requireActivity().findViewById<Top>(R.id.toolbar)
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        _binding = FragmentPeixeImagemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if(idPeixe.toInt()!=0) {
            buscaPeixe()
        }
        //toolBar.visibility = View.GONE
        navView.visibility = View.GONE

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscaPeixe() {
        lifecycleScope.launch {
            peixeDao.buscaPorId(idPeixe).collect{it->
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

    private fun tentaCarregarPeixe() {
        idPeixe= args.idPeixe.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(PeixeCarregado: Peixe) {

        binding.fragmentPeixeImagemImageview.tentaCarregarImagem(PeixeCarregado.diretorioImagem)

    }

}