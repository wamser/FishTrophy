package com.fishtrophy.ui.peixe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
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
import com.fishtrophy.databinding.FragmentPeixeBinding
import com.fishtrophy.ui.adapter.ListaPeixeAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class PeixeFragment : Fragment() {

    private var _binding: FragmentPeixeBinding? = null
    private val binding: FragmentPeixeBinding get() = _binding!!

    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity())
        db.peixeDao()
    }
    private val adapter by lazy { ListaPeixeAdapter(context = this.requireContext().applicationContext) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentPeixeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        configuraRecyclerView()

        lifecycleScope.launch {

            launch {

                buscaPeixesUsuario()


            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.bottom_options_menu_fragment_peixe, menu)
                val search = menu.findItem(R.id.menu_detalhes_fragment_peixe_pesquisar)
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

    private suspend fun buscaPeixesUsuario() {

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