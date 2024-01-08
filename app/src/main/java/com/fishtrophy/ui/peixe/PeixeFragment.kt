package com.fishtrophy.ui.peixe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentPeixeBinding
import com.fishtrophy.ui.adapter.ListaPeixeAdapter
import com.fishtrophy.ui.equipamento.EquipamentoFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class PeixeFragment : Fragment() {

    private var _binding: FragmentPeixeBinding? = null
    private val binding: FragmentPeixeBinding get() = _binding!!

    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity())
        db.peixeDao()
    }
    private var layoutManager: RecyclerView.LayoutManager? = null
    private val adapter by lazy { ListaPeixeAdapter(context = this.requireContext().applicationContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentPeixeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        configuraRecyclerView()

        lifecycleScope.launch {

            val launch = launch {
                //usuario.filterNotNull().collect {usuario->
                buscaPeixesUsuario(/*usuario.id*/)
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
                menuInflater.inflate(R.menu.bottom_options_menu_fragment_peixe, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_detalhes_fragment_peixe_adicionar -> {
                        val direction = PeixeFragmentDirections.actionNavigationPeixeToPeixeCadastroFragment(0)
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

    private suspend fun buscaPeixesUsuario(/*usuarioId: String*/) {
        // produtoDao.buscaTodos(usuarioId).collect { peixes ->
        peixeDao.buscaTodos().collect { peixes ->
            adapter.atualiza(peixes)
        }
    }

    private fun configuraRecyclerView() {
        val recyclerView = binding.fragmentListaAnimaisRecyclerView
        recyclerView.setAdapter(adapter)
        adapter.quandoClicaNoItemListener = {
            val direction = PeixeFragmentDirections.actionNavigationPeixeToPeixeDetalhesFragment(it.id.toInt())
            findNavController().navigate(direction)
        }
    }

}