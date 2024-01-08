package com.fishtrophy.ui.peixe

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
import com.fishtrophy.databinding.FragmentPeixeDetalheBinding
import com.fishtrophy.extentions.formataDuasCasas
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Peixe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PeixeDetalhesFragment : Fragment() {


    private var idPeixe: Long = 0L
    private var animal: Peixe? = null
    private val args : PeixeDetalhesFragmentArgs by navArgs()
    private var _binding: FragmentPeixeDetalheBinding? = null
    private val binding: FragmentPeixeDetalheBinding get() = _binding!!
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }

    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeixeDetalheBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        val mapFragment = childFragmentManager.findFragmentById(binding.fragmentDetalhesPeixeMapa.id) as SupportMapFragment
        tentaCarregarProduto()

        mapFragment.getMapAsync { googleMap ->
            lifecycleScope.launch {
                buscaBuscaPosicaoPeixe(googleMap)
            }
        }

        binding.fragmentDetalhesPeixeImagem.setOnClickListener(){
            val direction = PeixeDetalhesFragmentDirections.actionPeixeDetalhesFragmentToPeixeImagemFragment(
                idPeixe.toInt()
            )
            findNavController().navigate(direction)
        }

        navView.visibility = View.GONE
        return root
    }

    override fun onResume() {
        super.onResume()
        buscaProduto()
    }

    private fun buscaProduto() {
        lifecycleScope.launch {
            peixeDao.buscaPorId(idPeixe).collect{ animal->
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

    private fun tentaCarregarProduto() {
        idPeixe= args.idPeixe.toLong()//intent.getLongExtra(CHAVE_PRODUTO_ID,0L)
    }

    private fun preencheCampos(peixeCarregado: Peixe) {

        var sexo = ""

        with(binding) {
            if(peixeCarregado.sexo=="I") {
                sexo ="Indeterminado"
            }else if (peixeCarregado.sexo=="M"){
                sexo ="Macho"
            }else if (peixeCarregado.sexo=="F"){
                sexo ="Fêmea"
            }

            binding.fragmentDetalhesPeixeImagem.tentaCarregarImagem(peixeCarregado.diretorioImagem)
            binding.fragmentDetalhesPeixeSexo.text = "Sexo: "+sexo
            binding.fragmentDetalhesPeixeId.text = "ID: "+peixeCarregado.id.toString()
            binding.fragmentDetalhesPeixeTamanho.text = "Tamanho (CM): "+peixeCarregado.tamanho.formataDuasCasas()
            binding.fragmentDetalhesPeixePeso.text = "Peso (KG): "+peixeCarregado.peso.formataDuasCasas()
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

    private suspend fun buscaBuscaPosicaoPeixe(googleMap: GoogleMap)  {

        peixeDao.buscaPorId(idPeixe).collect { peixe ->

            if (peixe != null) {
                addMarker(googleMap,peixe)
                loadCameraOnMap(googleMap,peixe)
            }
        }

    }

    private fun addMarker(googleMap: GoogleMap, peixe:Peixe){

        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(peixe.localizacao)
        )

        marker?.tag = peixe
        if (marker != null) {
            marker.showInfoWindow()
        }


    }

    private fun loadCameraOnMap(googleMap: GoogleMap, peixe:Peixe){

        googleMap.setOnMapLoadedCallback {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(peixe.localizacao))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 2000, null);
        }
    }
}