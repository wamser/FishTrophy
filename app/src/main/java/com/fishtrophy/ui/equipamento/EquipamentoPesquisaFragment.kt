package com.fishtrophy.ui.equipamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.navArgs
import com.fishtrophy.databinding.FragmentEquipamentoPesquisaBinding
import com.fishtrophy.ui.adapter.ListaEquipamentoPesquisaAdapter

class EquipamentoPesquisaFragment : Fragment() {

    private var _binding: FragmentEquipamentoPesquisaBinding? = null
    private val binding: FragmentEquipamentoPesquisaBinding get() = _binding!!

    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity())
        db.equipamentoDao()
    }
    private val args: EquipamentoPesquisaFragmentArgs by navArgs()

    private val adapter by lazy { ListaEquipamentoPesquisaAdapter(context = this.requireContext().applicationContext) }

    private var equipamentoTipo = 0L
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navView = this.requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)

        _binding = FragmentEquipamentoPesquisaBinding.inflate(inflater, container, false)
        val root: View = binding.root
        navView.visibility = View.GONE
        tentaCarregarTipoEquipamento()


        configuraRecyclerView(equipamentoTipo)
        if (equipamentoTipo.toInt() != 0) {
            lifecycleScope.launch {

                launch {

                    buscaEquipamentosUsuario(/*usuario.id*/equipamentoTipo)

                }
            }
        }
        return root
    }

    private fun tentaCarregarTipoEquipamento() {
        equipamentoTipo = args.tipoEquipamento.toLong()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.bottom_options_menu_fragment_equipamento, menu)
                val search = menu.findItem(R.id.menu_detalhes_fragment_equipamento_pesquisar)
                val searchView = search?.actionView as? SearchView
                searchView?.isSubmitButtonEnabled = true

                searchView?.apply {
                    isSubmitButtonEnabled = true



                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                        override fun onQueryTextSubmit(query: String?): Boolean {

                            if (query != null) {

                                adapter.filtra(query)

                            }


                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {

                            if (newText != null) {

                                adapter.filtra(newText)

                            }

                            return true
                        }
                    })
                }

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_detalhes_fragment_equipamento_adicionar -> {
                        val direction =
                            EquipamentoFragmentDirections.actionNavigationEquipamentoToEquipamentoCadastroFragment(
                                0
                            )
                        Navigation.findNavController(view).navigate(direction)
                        true
                    }

                    else -> return false
                }
            }


        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun buscaEquipamentosUsuario(equipamentoTipo: Long) {

        when {
            equipamentoTipo.toInt() == 1 -> {//Isca
                equipamentoDao.buscaEquipamentosIsca().collect { equipamentos ->
                    adapter.atualiza(equipamentos)
                }
            }
            equipamentoTipo.toInt() == 2 -> { //Vara
                equipamentoDao.buscaEquipamentosVara().collect { equipamentos ->
                    adapter.atualiza(equipamentos)
                }
            }
            equipamentoTipo.toInt() == 3 -> {//Molinete e Carretilha
                equipamentoDao.buscaEquipamentosRecolhimento().collect { equipamentos ->
                    adapter.atualiza(equipamentos)
                }
            }
        }
    }

    private fun configuraRecyclerView(equipamentoTipo:Long) {
        val recyclerView = binding.fragmentListaEquipamentosPesquisaRecyclerView
        recyclerView.setAdapter(adapter)

        adapter.quandoClicaNoItemListener = {

            val navController = findNavController()
            if (equipamentoTipo.toInt() == 1) {//Isca
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "iscaEscolhido",
                    it
                )

            } else if (equipamentoTipo.toInt() == 2) { //Vara
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "varaEscolhido",
                    it
                )
            } else if (equipamentoTipo.toInt() == 3) {//Molinete e Carretilha
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "molineteCarretilhaEscolhido",
                    it
                )
            }

            navController.popBackStack()
        }
    }
}




