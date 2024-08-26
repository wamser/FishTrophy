package com.fishtrophy.ui.peixe

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.database.dao.PeixeDao
import com.fishtrophy.databinding.FragmentPeixeDetalheBinding
import com.fishtrophy.extentions.formataDuasCasas
import com.fishtrophy.extentions.tentaCarregarImagem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.format.DateTimeFormatter

class PeixeDetalhesFragment : Fragment() {


    private var idPeixe: Long = 0L
    private val args: PeixeDetalhesFragmentArgs by navArgs()
    private var _binding: FragmentPeixeDetalheBinding? = null
    private val binding: FragmentPeixeDetalheBinding get() = _binding!!
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeixeDetalheBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        val mapFragment =
            childFragmentManager.findFragmentById(binding.fragmentPeixeDetalhesMapa.id) as SupportMapFragment
        tentaCarregarProduto()

        mapFragment.getMapAsync { googleMap ->
            buscaBuscaPosicaoPeixe(googleMap)
        }

        binding.fragmentPeixeDetalhesImagem.setOnClickListener {
            val direction =
                PeixeDetalhesFragmentDirections.actionPeixeDetalhesFragmentToPeixeImagemFragment(
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
        var peixe: PeixeDao.PeixeWithEquipamento

        peixeDao.buscaPorIdCompleto(idPeixe).let { peixeComEquipamento ->
            peixeComEquipamento.observe(viewLifecycleOwner) {
                peixe = it!![0]
                preencheCampos(peixe)
            }
        }
    }


    private fun tentaCarregarProduto() {
        idPeixe = args.idPeixe.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(peixeCarregado: PeixeDao.PeixeWithEquipamento) {

        var sexo = ""

        when (peixeCarregado.sexo) {
            "I" -> {
                sexo = "Indeterminado"
            }

            "M" -> {
                sexo = "Macho"
            }

            "F" -> {
                sexo = "Fêmea"
            }
        }

        binding.fragmentPeixeDetalhesImagem.tentaCarregarImagem(peixeCarregado.diretorioImagem)

        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = peixeCarregado.dataCaptura.format(formatterDate)
        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedTime = peixeCarregado.horaCaptura.format(formatterTime)
        binding.fragmentPeixeDetalhesDataHoraCaptura.text = formattedDate + " | " + formattedTime

        binding.fragmentPeixeDetalhesEspecie.text =
            peixeCarregado.especie

        binding.fragmentPeixeDetalhesPeso.text =
            peixeCarregado.peso.toString()

        binding.fragmentPeixeDetalhesSexo.text = sexo

        binding.fragmentPeixeDetalhesTamanho.text =
            peixeCarregado.tamanho.formataDuasCasas() + " CM"
        binding.fragmentPeixeDetalhesPeso.text =
            peixeCarregado.peso.formataDuasCasas() + " KG"
        binding.fragmentPeixeDetalhesVara.text = peixeCarregado.varaDescricao
        binding.fragmentPeixeDetalhesRecolhimento.text = peixeCarregado.recolhimentoDescricao
        binding.fragmentPeixeDetalhesIsca.text = peixeCarregado.iscaDescricao
    }

    private fun buscaBuscaPosicaoPeixe(googleMap: GoogleMap) {
        var peixe: PeixeDao.PeixeWithEquipamento
        peixeDao.buscaPorIdCompleto(idPeixe).let { peixeComEquipamento ->

            peixeComEquipamento.observe(viewLifecycleOwner) {
                peixe = it!![0]
                adicionaMarker(googleMap, peixe)
                loadCameraOnMap(googleMap, peixe)
            }
        }

    }

    private fun adicionaMarker(googleMap: GoogleMap, peixe: PeixeDao.PeixeWithEquipamento) {

        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(peixe.localizacao)
        )

        marker?.tag = peixe
        marker?.showInfoWindow()


    }

    private fun loadCameraOnMap(googleMap: GoogleMap, peixe: PeixeDao.PeixeWithEquipamento) {

        googleMap.setOnMapLoadedCallback {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(peixe.localizacao))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 750, null)
        }
    }
}