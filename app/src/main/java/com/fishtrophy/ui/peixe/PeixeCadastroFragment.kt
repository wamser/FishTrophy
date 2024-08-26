package com.fishtrophy.ui.peixe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.database.dao.PeixeDao
import com.fishtrophy.databinding.FragmentPeixeCadastroBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento
import com.fishtrophy.model.Peixe
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.UUID

private const val REQUEST_IMAGE_CAPTURE = 3
private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2

@Suppress("DEPRECATION", "UNREACHABLE_CODE")
class PeixeCadastroFragment : Fragment()/*, OnMapReadyCallback*/ {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val args: PeixeCadastroFragmentArgs by navArgs()
    private var _binding: FragmentPeixeCadastroBinding? = null
    private var preencheuAlteracao = 0L
    private var fotoAlterada = false
    private val binding: FragmentPeixeCadastroBinding get() = _binding!!
    private var idPeixe = 0L
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }

    private var idVaraSelecionada: Int = 0
    private var descVaraSelecionada: String? = null

    private var idIscaSelecionada: Int = 0
    private var descIscaSelecionada: String? = null

    private var idRecolhimentoSelecionada: Int = 0
    private var descRecolhimentoSelecionada: String? = null

    private var idSexoSelecionado: String = "I"

    private var imageUrl: String? = null
    private var imageBitmap: Bitmap? = null

    private var marker: Marker? = null

    private var scrollViewPositionY: Int? = 0

    private var latitude = 0.0
    private var longitude = 0.0

    private lateinit var imagemFile: File

    private var imagemCaminho: String = ""

    private var acessouGaleria = false
    private var acessouCamera = false

    private lateinit var mapView: SupportMapFragment

    private var acessouLocalizacaoInserir = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    private fun createFolderInAppDirectory(context: Context, folderName: String): File? {
        val appDirectory = context.filesDir
        val folder = File(appDirectory, folderName)

        return if (!folder.exists()) {
            val isDirectoryCreated = folder.mkdirs()
            if (isDirectoryCreated) {
                folder
            } else {

                null
            }
        } else {

            folder
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentPeixeCadastroBinding.inflate(inflater, container, false)
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        val root: View = binding.root

        tentaCarregarPeixe()

        if (imageUrl != null) {
            binding.fragmentPeixeCadastroImagem.tentaCarregarImagem(imageUrl)
        } else if (imageBitmap != null) {
            binding.fragmentPeixeCadastroImagem.setImageBitmap(imageBitmap)
        }

        if (idPeixe.toInt() != 0) {
            if (preencheuAlteracao == 0L) {
                buscaPeixe()
            }

        } else {
            valorPadraoDataCaptura()
            valorPadraoHoraCaptura()
        }

        binding.fragmentPeixeCadastroImagem.setOnClickListener {
            val direction =
                PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToPeixeImagemFragment(
                    idPeixe.toInt()
                )
            findNavController().navigate(direction)
        }

        binding.fragmentCadastroPeixeBotaoData.setOnClickListener {
            criarDataPicker()
        }
        binding.fragmentCadastroPeixeBotaoHora.setOnClickListener {
            criarTimePicker()
        }

        binding.fragmentPeixeCadastroRadioSexo.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val radioButton = group.findViewById<RadioButton>(checkedId)
                radioButton.text.toString()
                if (binding.fragmentPeixeCadastroRadioSexoIndefinido.isChecked) {
                    idSexoSelecionado = "I"
                } else if (binding.fragmentPeixeCadastroRadioSexoMacho.isChecked) {
                    idSexoSelecionado = "M"
                } else if (binding.fragmentPeixeCadastroRadioSexoFemea.isChecked) {
                    idSexoSelecionado = "F"
                }
            }
        }

        navView.visibility = View.GONE
        configuraBotaoGaleria()
        configuraBotaoFoto()
        configuraBotaoIsca()
        configuraBotaoRecolhimento()
        configuraBotaoVara()


        mapView =
            childFragmentManager.findFragmentById(binding.fragmentPeixeTextinputlayoutMapa.id) as SupportMapFragment
        verificaPermissao()
        recebeDados()
        mapView.getMapAsync { googleMap ->

            if (marker == null) {

                buscaPosicaoPeixe(googleMap)
            }


            googleMap.setOnMapClickListener {
                val direction =
                    PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToPeixeCadastroMapaFragment(
                        idPeixe.toInt()
                    )
                findNavController().navigate(direction)
            }
        }

        return root
    }

    private fun verificaPermissao() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        if (idPeixe.toInt() == 0) {
            if (!acessouLocalizacaoInserir) {
                if (ActivityCompat.checkSelfPermission(
                        this.requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    requisitaAtualizacaoPosicao()
                } else {
                    ActivityCompat.requestPermissions(
                        this.requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                    requisitaAtualizacaoPosicao()

                }
                acessouLocalizacaoInserir=true
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        scrollViewPositionY = binding.fragmentPeixeCadastroScrollview.scrollY
    }

    private fun recebeDados() {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>("position")
            ?.observe(viewLifecycleOwner) { result ->
                //val mapFragment = childFragmentManager.findFragmentById(binding.fragmentPeixeTextinputlayoutMapa.id) as SupportMapFragment
                mapView.getMapAsync { googleMap ->

                    lifecycleScope.launch {
                        adicionaMarker(googleMap, result.latitude, result.longitude)
                        posicionaMarkerNoMapa(googleMap, result.latitude, result.longitude)

                    }
                }

            }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Equipamento>("varaEscolhido")
            ?.observe(viewLifecycleOwner) { result ->
                idVaraSelecionada = result.id.toInt()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Equipamento>("varaEscolhido")
                descVaraSelecionada = result.descricao
                binding.fragmentPeixeCadastroVara.editText?.setText(descVaraSelecionada)

            }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Equipamento>("iscaEscolhido")
            ?.observe(viewLifecycleOwner) { result ->
                idIscaSelecionada = result.id.toInt()
                descIscaSelecionada = result.descricao
                binding.fragmentPeixeCadastroIsca.editText?.setText(descIscaSelecionada)

                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Equipamento>("iscaEscolhido")
            }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Equipamento>("molineteCarretilhaEscolhido")
            ?.observe(viewLifecycleOwner) { result ->
                idRecolhimentoSelecionada = result.id.toInt()
                descRecolhimentoSelecionada = result.descricao
                binding.fragmentPeixeCadastroRecolhimento.editText?.setText(
                    descRecolhimentoSelecionada
                )
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Equipamento>("molineteCarretilhaEscolhido")
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun criarDataPicker() {

        // Set the current date as the default date
        val currentDate = Calendar.getInstance()
        var year = currentDate.get(Calendar.YEAR)
        var month = currentDate.get(Calendar.MONTH)
        var day = currentDate.get(Calendar.DAY_OF_MONTH)

        if (idPeixe.toInt() != 0) {
            val dataRegistro = binding.fragmentPeixeCadastroDataCaptura.editText?.text.toString()

            day = dataRegistro.subSequence(0, 2).toString().toInt()
            month = dataRegistro.subSequence(3, 5).toString().toInt() - 1
            year = dataRegistro.subSequence(6, 10).toString().toInt()
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)


                binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formatDate(selectedDate))

            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = currentDate.timeInMillis

        datePickerDialog.show()

    }

    private fun valorPadraoDataCaptura() {

        val selectedDate = Calendar.getInstance()
        binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formatDate(selectedDate))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun criarTimePicker() {

        val currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)

        if (idPeixe.toInt() != 0) {
            val horaMinuto = binding.fragmentPeixeCadastroHoraCaptura.editText?.text.toString()

            hour = horaMinuto.subSequence(0, 2).toString().toInt()
            minute = horaMinuto.subSequence(4, 5).toString().toInt()
        }


        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->

                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)

                binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formatTime(selectedTime) + ":00")

            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()

    }

    private fun valorPadraoHoraCaptura() {

        val selectedTime = Calendar.getInstance()
        binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formatTime(selectedTime) + ":00")

    }

    private fun formatDate(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
        val year = calendar.get(Calendar.YEAR)
        //return "$day/$month/$year"
        return String.format("%02d/%02d/%04d", day, month, year)

    }

    private fun formatTime(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private fun buscaPosicaoPeixe(googleMap: GoogleMap) {

        lifecycleScope.launch {
            peixeDao.buscaPorId(idPeixe).collect { peixe ->

                peixe?.let {

                    adicionaMarker(
                        googleMap,
                        peixe.localizacao.latitude,
                        peixe.localizacao.longitude
                    )
                    posicionaMarkerNoMapa(
                        googleMap,
                        peixe.localizacao.latitude,
                        peixe.localizacao.longitude
                    )
                }

            }
        }

    }

    private fun adicionaMarker(googleMap: GoogleMap, latitude: Double, longitude: Double) {

        if (marker != null) {
            //marker?.remove()
            //googleMap.clear()
            marker?.position = LatLng(latitude, longitude)
        } else {

            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(latitude, longitude))
            )

            marker?.showInfoWindow()


        }

    }

    private fun posicionaMarkerNoMapa(googleMap: GoogleMap, latitude: Double, longitude: Double) {

        googleMap.setOnMapLoadedCallback {

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 750, null)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscaPeixe() {

        val peixeRetornado: LiveData<List<PeixeDao.PeixeWithEquipamento>> =
            peixeDao.buscaPorIdCompleto(idPeixe)
        peixeRetornado.observe(viewLifecycleOwner) {
            preencheCampos(it!![0])
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.bottom_options_menu_fragment_peixe_cadastro, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        return when (item.itemId) {
            R.id.menu_detalhes_peixe_salvar -> {

                validaCampos()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validaCampos() {

        if (binding.fragmentPeixeCadastroDataCaptura.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Data da captura", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroDataCaptura.editText?.requestFocus()

        } else if (binding.fragmentPeixeCadastroHoraCaptura.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Hora da captura", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroHoraCaptura.editText?.requestFocus()

        } else if (binding.fragmentPeixeCadastroEspecie.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Espécie", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroEspecie.editText?.requestFocus()

        } else if (binding.fragmentPeixeCadastroPeso.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Peso", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroPeso.editText?.requestFocus()

        } else if (binding.fragmentPeixeCadastroTamanho.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Tamanho", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroTamanho.editText?.requestFocus()

        } else if (idVaraSelecionada == 0) {
            Toast.makeText(context, "Preencha o campo Vara", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroVara.editText?.requestFocus()

        } else if (idRecolhimentoSelecionada == 0) {
            Toast.makeText(context, "Preencha o campo Recolhimento", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroRecolhimento.editText?.requestFocus()

        } else if (idIscaSelecionada == 0) {
            Toast.makeText(context, "Preencha o campo Isca", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroIsca.editText?.requestFocus()

        } else {
            lifecycleScope.launch {
                tentaSalvarPeixe()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun tentaSalvarPeixe() {
        val navController = findNavController()
        try {
            val peixe = criaPeixe()
            peixeDao.salva(peixe)
            Toast.makeText(context, "Registro salvo com sucesso! ", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } catch (e: RuntimeException) {
            Log.e("AnimalCadastro", "tentaSalvarPeixe: ", e)
        }
    }


    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File =
            criaPastaDiretorioApp(this.requireActivity().baseContext, "imagens/equipamento/")
        //getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun criaPastaDiretorioApp(context: Context, folderName: String): File {
        val appDirectory = context.filesDir
        val folder = File(appDirectory, folderName)

        return if (!folder.exists()) {
            val isDirectoryCreated = folder.mkdirs()
            if (isDirectoryCreated) {
                folder
            } else {

                folder
            }
        } else {

            folder
        }
    }

    private fun tiraFotoIntent() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // if (imageFile != null) {

        val imageUri =
            FileProvider.getUriForFile(
                this.requireActivity().baseContext,
                "com.fishtrophy.android.fileprovider",
                imagemFile
            )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, com.fishtrophy.ui.peixe.REQUEST_IMAGE_CAPTURE)
        // }
    }

    fun lidaComImagemCapturada(imagePath: String): Bitmap {
        val exifInterface = ExifInterface(imagePath)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotatedBitmap = rotacionaImagem(BitmapFactory.decodeFile(imagePath), orientation)
        return rotatedBitmap
    }

    fun rotacionaImagem(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun configuraBotaoFoto() {
        val botaoFoto = binding.fragmentPeixeCadastroBotaoFoto
        botaoFoto.setOnClickListener {
            acessouGaleria = false
            acessouCamera = true
            imagemFile = createImageFile()
            tiraFotoIntent()
        }
    }

    private fun configuraBotaoGaleria() {
        val botaoGaleria = binding.fragmentCadastroPeixeBotaoGaleria
        botaoGaleria.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            acessouGaleria = true
            acessouCamera = false
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    private fun configuraBotaoIsca() {
        val botaoIsca = binding.fragmentCadastroPeixeBotaoIsca
        botaoIsca.setOnClickListener {
            val direction =
                PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToEquipamentoPesquisaFragment(
                    1
                )
            findNavController().navigate(direction)
        }
    }

    private fun configuraBotaoRecolhimento() {
        val botaoIsca = binding.fragmentCadastroPeixeBotaoRecolhimento
        botaoIsca.setOnClickListener {
            val direction =
                PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToEquipamentoPesquisaFragment(
                    3
                )
            findNavController().navigate(direction)
        }
    }

    private fun configuraBotaoVara() {
        val botaoIsca = binding.fragmentCadastroPeixeBotaoVara
        botaoIsca.setOnClickListener {
            val direction =
                PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToEquipamentoPesquisaFragment(
                    2
                )
            findNavController().navigate(direction)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            fotoAlterada = true
            val capturedImageFileBitMap = lidaComImagemCapturada(imagemFile.toString())
            this.imageBitmap = capturedImageFileBitMap
            binding.fragmentPeixeCadastroImagem.setImageBitmap(capturedImageFileBitMap)

        }

        if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == RESULT_OK) {
            fotoAlterada = true
            binding.fragmentPeixeCadastroImagem.setImageURI(data?.data)
            imageUrl = data?.data.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat", "SuspiciousIndentation")
    private fun criaPeixe(): Peixe {
        var imgDiretorio = ""
        val campoData =
            binding.fragmentPeixeCadastroDataCaptura
        val dataCaptura = campoData.editText?.text.toString()
        val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dataCapturaFormatada = LocalDate.parse(dataCaptura, formatoData)

        val campoHora =
            binding.fragmentPeixeCadastroHoraCaptura
        val horaCaptura = campoHora.editText?.text.toString()
        val formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss")
        val horaCapturaFormatada = LocalTime.parse(horaCaptura, formatoHora)

        val campoPeso =
            binding.fragmentPeixeCadastroPeso
        val pesoTexto = campoPeso.editText?.text.toString()
        val peso = if (pesoTexto.isBlank()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(pesoTexto)
        }


        val campoTamanho =
            binding.fragmentPeixeCadastroTamanho
        val tamanhoTexto = campoTamanho.editText?.text.toString()
        val tamanho = if (tamanhoTexto.isBlank()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(tamanhoTexto)
        }

        val campoEspecie =
            binding.fragmentPeixeCadastroEspecie
        val especie = campoEspecie.editText?.text.toString()

        if (fotoAlterada) {

            if (imagemCaminho != "") {
                deletaArquivo(imagemCaminho)
            }
            if (acessouGaleria) {

                val imgDrawable = binding.fragmentPeixeCadastroImagem.getDrawable()
                if (imgDrawable != null) {
                    val bitmap =
                        (imgDrawable as BitmapDrawable).bitmap
                    val saida = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
                    val img: ByteArray = saida.toByteArray()
                    imgDiretorio = gravaImagemDiretorio(img)
                }
            } else if (acessouCamera) {
                imgDiretorio = imagemFile.toString()
            }

            return Peixe(
                id = idPeixe,
                dataCaptura = dataCapturaFormatada,
                horaCaptura = horaCapturaFormatada,
                peso = peso,
                tamanho = tamanho,
                sexo = idSexoSelecionado,
                diretorioImagem = imgDiretorio,
                localizacao = LatLng(
                    marker?.position!!.latitude,
                    marker?.position!!.longitude
                ),
                idEquipamentoVara = idVaraSelecionada.toLong(),
                idEquipamentoIsca = idIscaSelecionada.toLong(),
                idEquipamentoRecolhimento = idRecolhimentoSelecionada.toLong(),
                especie = especie

            )
        } else {

            return Peixe(
                id = idPeixe,
                dataCaptura = dataCapturaFormatada,
                horaCaptura = horaCapturaFormatada,
                peso = peso,
                tamanho = tamanho,
                sexo = idSexoSelecionado,
                diretorioImagem = imagemCaminho,
                localizacao = LatLng(
                    marker?.position!!.latitude,
                    marker?.position!!.longitude
                ),
                idEquipamentoVara = idVaraSelecionada.toLong(),
                idEquipamentoIsca = idIscaSelecionada.toLong(),
                idEquipamentoRecolhimento = idRecolhimentoSelecionada.toLong(),
                especie = especie

            )

        }
    }

    private fun deletaArquivo(arquivoCaminho: String) {

        val arquivo = File(arquivoCaminho)

        if (arquivo.exists()) {
            val deleted = arquivo.delete()
            if (deleted) {

                Log.d("FishTrophy", "Arquivo deletado")
            } else {

                Log.w("FishTrophy", "Falha ao deletar o arquivo")
            }
        } else {

            Log.w("FishTrophy", "Arquivo não encontrado")
        }

    }

    private fun tentaCarregarPeixe() {
        idPeixe = args.idPeixe.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(peixeCarregado: PeixeDao.PeixeWithEquipamento) {

        idSexoSelecionado = peixeCarregado.sexo

        when (peixeCarregado.sexo) {
            "I" -> {
                binding.fragmentPeixeCadastroRadioSexoIndefinido.isChecked = true
            }

            "M" -> {
                binding.fragmentPeixeCadastroRadioSexoMacho.isChecked = true
            }

            "F" -> {
                binding.fragmentPeixeCadastroRadioSexoFemea.isChecked = true
            }
        }

        imagemCaminho = peixeCarregado.diretorioImagem

        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = peixeCarregado.dataCaptura.format(formatterDate)
        binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formattedDate)

        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedTime = peixeCarregado.horaCaptura.format(formatterTime)
        binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formattedTime)

        binding.fragmentPeixeCadastroImagem.tentaCarregarImagem(peixeCarregado.diretorioImagem)
        imageUrl = peixeCarregado.diretorioImagem

        binding.fragmentPeixeCadastroPeso.editText?.setText(peixeCarregado.peso.toString())
        binding.fragmentPeixeCadastroTamanho.editText?.setText(peixeCarregado.tamanho.toString())

        binding.fragmentPeixeCadastroEspecie.editText?.setText(peixeCarregado.especie)

        idVaraSelecionada = peixeCarregado.idEquipamentoVara.toInt()
        descVaraSelecionada = peixeCarregado.varaDescricao
        binding.fragmentPeixeCadastroVara.editText?.setText(peixeCarregado.varaDescricao)

        idRecolhimentoSelecionada = peixeCarregado.idEquipamentoRecolhimento.toInt()
        descRecolhimentoSelecionada = peixeCarregado.recolhimentoDescricao
        binding.fragmentPeixeCadastroRecolhimento.editText?.setText(peixeCarregado.recolhimentoDescricao)

        idIscaSelecionada = peixeCarregado.idEquipamentoIsca.toInt()
        descIscaSelecionada = peixeCarregado.iscaDescricao
        binding.fragmentPeixeCadastroIsca.editText?.setText(peixeCarregado.iscaDescricao)

        preencheuAlteracao = 1
    }

    private fun gravaImagemDiretorio(binario: ByteArray): String {
        val directory = createFolderInAppDirectory(
            this.requireActivity().baseContext,
            "imagens/peixe/"
        )
        val fileName = UUID.randomUUID().toString()
        val file = File(directory, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            outputStream.write(binario)
            outputStream.close()
            directory.toString() + "/" + fileName
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }

    }

    private fun requisitaAtualizacaoPosicao() {

        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->

            if (location != null) {

                latitude = location.latitude
                longitude = location.longitude

                mapView.getMapAsync { googleMap ->

                    adicionaMarker(googleMap, latitude, longitude)
                    posicionaMarkerNoMapa(googleMap, latitude, longitude)
                }

            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão autorizada
                requisitaAtualizacaoPosicao()
            } else {
                // Permissão negada
            }
        }
    }

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    /*override fun onMapReady(googleMap: GoogleMap) {
        if (idPeixe.toInt() != 0) {
            lifecycleScope.launch {

                buscaPosicaoPeixe(googleMap)

            }
        }
        googleMap.setOnMapClickListener {
            val direction =
                PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToPeixeCadastroMapaFragment(
                    idPeixe.toInt()
                )
            findNavController().navigate(direction)
        }

    }*/

}