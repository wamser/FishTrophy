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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentEquipamentoBinding
import com.fishtrophy.ui.adapter.ListaEquipamentoAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class EquipamentoFragment : Fragment() {

    private var _binding: FragmentEquipamentoBinding? = null
    private val binding: FragmentEquipamentoBinding get() = _binding!!

    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity())
        db.equipamentoDao()
    }

    private val adapter by lazy { ListaEquipamentoAdapter(context = this.requireContext().applicationContext) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentEquipamentoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        configuraRecyclerView()

        lifecycleScope.launch {

            val launch = launch {
                //usuario.filterNotNull().collect {usuario->
                buscaEquipamentosUsuario(/*usuario.id*/)
                //}

            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.bottom_options_menu_fragment_equipamento, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_detalhes_fragment_equipamento_adicionar -> {
                        val direction =EquipamentoFragmentDirections.actionNavigationEquipamentoToEquipamentoCadastroFragment(0)
                        Navigation.findNavController(view).navigate(direction)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        if(!navView.isVisible){
            navView.visibility = View.VISIBLE
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun buscaEquipamentosUsuario(/*usuarioId: String*/) {
        // produtoDao.buscaTodos(usuarioId).collect { peixes ->
        equipamentoDao.buscaTodos().collect { peixes ->
            adapter.atualiza(peixes)
        }
    }

   private fun configuraRecyclerView() {
        val recyclerView = binding.fragmentListaEquipamentosRecyclerView
        recyclerView.setAdapter(adapter)
        adapter.quandoClicaNoItemListener = {
            val direction = EquipamentoFragmentDirections.actionNavigationEquipamentoToEquipamentoDetalhesFragment(it.id.toInt())
            findNavController().navigate(direction)
        }
    }

}