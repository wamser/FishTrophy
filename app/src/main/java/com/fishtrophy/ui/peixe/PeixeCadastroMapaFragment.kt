package com.fishtrophy.ui.peixe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentPeixeCadastroMapaBinding
import com.fishtrophy.model.Peixe
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class PeixeCadastroMapaFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var peixes: List<Peixe> = emptyList()
    private var _binding: FragmentPeixeCadastroMapaBinding? = null
    private val binding: FragmentPeixeCadastroMapaBinding get() = _binding!!
    private val peixeDao by lazy {
        AppDatabase.instancia(this.requireContext()).peixeDao()
    }
    private val args : PeixeCadastroMapaFragmentArgs by navArgs()
    private var idPeixe= 0L

    private var latitude=0.0
    private var longitude=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        // Check for runtime permissions
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationUpdates()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

        @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(com.fishtrophy.R.menu.bottom_options_menu_fragment_peixe_cadastro_mapa,menu)
        return super.onCreateOptionsMenu(menu,inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_peixe_cadastro_mapa_peixe_salvar -> {
                val navController = findNavController()
                navController.previousBackStackEntry?.savedStateHandle?.set("position",LatLng(latitude,longitude))
                navController.popBackStack()

                true
            }
            R.id.menu_peixe_cadastro_mapa_peixe_hybrid -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentPeixeCadastroMapa.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_HYBRID
                }

                true
            }
            R.id.menu_peixe_cadastro_mapa_peixe_normal -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentPeixeCadastroMapa.id) as SupportMapFragment


                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_NORMAL
                }

                true
            }
            R.id.menu_peixe_cadastro_mapa_peixe_terrain -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentPeixeCadastroMapa.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
                }

                true
            }
            R.id.menu_peixe_cadastro_mapa_peixe_satellite -> {

                val mapFragment=childFragmentManager.findFragmentById(binding.fragmentPeixeCadastroMapa.id) as SupportMapFragment

                mapFragment. getMapAsync { googleMap ->
                    googleMap.mapType=GoogleMap.MAP_TYPE_SATELLITE
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun tentaCarregarPeixe() {
        idPeixe= args.idPeixe.toLong()
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Got last known location. In some rare situations, this can be null.
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude

                // Now you have the latitude and longitude
                // Do something with the location information
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
                requestLocationUpdates()
            } else {
                // Permission denied
            }
        }
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeixeCadastroMapaBinding.inflate(inflater, container, false)
        val mapFragment = childFragmentManager.findFragmentById(binding.fragmentPeixeCadastroMapa.id) as SupportMapFragment
        val root: View = binding.root
        tentaCarregarPeixe()

        mapFragment.getMapAsync{ googleMap->
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
           lifecycleScope.launch {
              buscaBuscaPosicaoPeixe(googleMap)
           }

            googleMap.setOnMapClickListener{clickedLatLng->

                AdicionaNovoMarker(googleMap,clickedLatLng)
            }
        }

        return root
    }

    private fun AdicionaNovoMarker(googleMap: GoogleMap,clickedLatLng:LatLng){

        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(clickedLatLng))
        // (Optional) You can move the camera to the clicked location
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(clickedLatLng))

        latitude= clickedLatLng.latitude
        longitude=clickedLatLng.longitude

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun buscaBuscaPosicaoPeixe(googleMap: GoogleMap)  {

            peixeDao.buscaPorId(idPeixe).collect { peixe ->

                googleMap.setOnMapLoadedCallback {
                    if(idPeixe.toInt()!=0) {
                        if (peixe != null) {
                            addMarkerPosicaoPeixe(googleMap, peixe)
                            loadCameraOnMapPeixe(googleMap, peixe)
                        }
                    }else{
                        addMarkerPosicaoAtual(googleMap)
                        loadCameraOnMapAtual(googleMap)
                    }
            }
        }
    }

    private  fun addMarkerPosicaoPeixe(googleMap: GoogleMap,peixe:Peixe){

        if(idPeixe.toInt()!=0) {

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(peixe.localizacao)
            )

        }else {

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
            )
        }

    }

    private fun loadCameraOnMapPeixe(googleMap: GoogleMap, peixe:Peixe){

        if(idPeixe.toInt()!=0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(peixe.localizacao))
        }else{

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
        }
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 2000, null);

    }


    private  fun addMarkerPosicaoAtual(googleMap: GoogleMap){

            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
            )

    }

    private fun loadCameraOnMapAtual(googleMap: GoogleMap){

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 2000, null);

    }

}