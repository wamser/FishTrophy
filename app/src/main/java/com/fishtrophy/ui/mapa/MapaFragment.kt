package com.fishtrophy.ui.mapa

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentMapaBinding
import com.fishtrophy.extentions.CustomBottomSheetFragment
import com.fishtrophy.model.Peixe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MapaFragment : Fragment() {

    private var _binding: FragmentMapaBinding? = null
    private val binding: FragmentMapaBinding get() = _binding!!
    private val peixeDao by lazy {
        AppDatabase.instancia(this.requireContext()).peixeDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onResume() {
        super.onResume()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        if (!navView.isVisible) {
            navView.visibility = View.VISIBLE
        }

        val mapFragment =
            childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment

        mapFragment.getMapAsync { googleMap ->
            lifecycleScope.launch {
                googleMap.clear()
                buscaPeixesEMapeia(googleMap)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapaBinding.inflate(inflater, container, false)
        val progressBar= binding.loadingProgressBar as ProgressBar //binding.loadingProgressBar

        val mapFragment =
            childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment
        val root: View = binding.root

        mapFragment.getMapAsync { googleMap ->
            progressBar.visibility = View.GONE
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            lifecycleScope.launch {
                buscaPeixesEMapeia(googleMap)

            }
            /*googleMap.setOnMapLoadedCallback {
                 progressBar.visibility = View.GONE
                Toast.makeText(context, "Entrou no setOnMapLoadedCallback", Toast.LENGTH_SHORT).show()
            }*/
            googleMap.setOnMarkerClickListener { marker ->
                val peixeID = marker.title?.toLong()
                val bottomSheetFragment = CustomBottomSheetFragment()
                val bundle = Bundle()
                if (peixeID != null) {
                    bundle.putLong("peixeID", peixeID)
                }
                bottomSheetFragment.arguments = bundle
                bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                true  // Consume the marker click event

            }
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(com.fishtrophy.R.menu.bottom_options_menu_fragment_mapa, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_detalhes_fragment_mapa_add -> {

                val direction =
                    MapaFragmentDirections.actionNavigationMapaToPeixeCadastroFragment(0)
                    Navigation.findNavController(this.requireView()).navigate(direction)
                true
            }
            R.id.menu_detalhes_fragment_mapa_hybrid -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_HYBRID
                }

                true
            }
            R.id.menu_detalhes_fragment_mapa_normal -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment


                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
                }

                true
            }
            R.id.menu_detalhes_fragment_mapa_terrain -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
                }

                true
            }
            R.id.menu_detalhes_fragment_mapa_satellite -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentMap.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun buscaPeixesEMapeia(googleMap:GoogleMap)  {

        peixeDao.buscaTodos().collect { peixes ->
            addMarkers(googleMap,peixes)
            loadCameraOnMap(googleMap,peixes)
        }

    }

   private fun addMarkers(googleMap:GoogleMap,peixes:List<Peixe>){

        peixes.forEach { local ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(local.id.toString())
                    .position(local.localizacao)
            )

            marker?.tag = local
            if (marker != null) {
                marker.showInfoWindow()
            }

        }

    }

    private fun loadCameraOnMap(googleMap:GoogleMap,peixes:List<Peixe>){

        googleMap.setOnMapLoadedCallback {
            val bounds = LatLngBounds.builder()
            peixes.forEach { local ->
                bounds.include(local.localizacao)
            }
        if(peixes.isNotEmpty()){
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),500))
        }
        }
    }
}