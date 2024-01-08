package com.fishtrophy.ui.peixe


import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.databinding.FragmentPeixeCadastroBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Peixe
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2

@Suppress("DEPRECATION")
class PeixeCadastroFragment : Fragment() {
    private val args : PeixeCadastroFragmentArgs by navArgs()
    private var _binding: FragmentPeixeCadastroBinding? = null
    private val binding: FragmentPeixeCadastroBinding get() = _binding!!
    private var idPeixe= 0L
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }

    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

    private var IdVaraSelecionada: Int = 0
    private var DescVaraSelecionada: String? = null

    private var IdIscaSelecionada: Int = 0
    private var DescIscaSelecionada: String? = null

    private var IdRecolhimentoSelecionada: Int = 0
    private var DescRecolhimentoSelecionada: String? = null

    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        }

    // This function creates a folder in the app directory
    private fun createFolderInAppDirectory(context: Context, folderName: String): File? {
        val appDirectory = context.filesDir // This gets the app's internal storage directory
        val folder = File(appDirectory, folderName)

        if (!folder.exists()) {
            val isDirectoryCreated = folder.mkdir() // Create the folder
            if (isDirectoryCreated) {
                return folder
            } else {
                // Handle the case where folder creation failed
                return null
            }
        } else {
            // The folder already exists
            return folder
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        tentaCarregarPeixe()
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        _binding = FragmentPeixeCadastroBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if(idPeixe.toInt()!=0) {
            buscaPeixe()
        }else{
            valorPadraoDataCaptura()
            valorPadraoHoraCaptura()
        }

        val mapFragment = childFragmentManager.findFragmentById(binding.fragmentPeixeTextinputlayoutMapa.id) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            if(marker==null){
            lifecycleScope.launch {
                buscaPosicaoPeixe(googleMap)

            }
            }
            googleMap.setOnMapClickListener() {
                val direction = PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToPeixeCadastroMapaFragment(
                    idPeixe.toInt()
                )
                findNavController().navigate(direction)
            }
        }

        binding.fragmentPeixeCadastroImagem.setOnClickListener(){
            val direction = PeixeCadastroFragmentDirections.actionPeixeCadastroFragmentToPeixeImagemFragment(
                idPeixe.toInt()
            )
            findNavController().navigate(direction)
        }

        binding.fragmentCadastroPeixeBotaoData.setOnClickListener(){
            criarDataPicker()
        }
        binding.fragmentCadastroPeixeBotaoHora.setOnClickListener(){
            criarTimePicker()
        }

        navView.visibility = View.GONE
        configuraBotaoGaleria()
        configuraBotaoFoto()
        configuraSpinnerSexo()
        configuraSpinnerVara()
        configuraSpinnerRecolhimento()
        configuraSpinnerIsca()

        return root
    }



        override fun onResume() {
        super.onResume()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<LatLng>("position")?.observe(viewLifecycleOwner) { result ->
            val mapFragment = childFragmentManager.findFragmentById(binding.fragmentPeixeTextinputlayoutMapa.id) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                marker?.remove()
                lifecycleScope.launch {

                    addMarker(googleMap,result.latitude,result.longitude)
                    loadCameraOnMap(googleMap,result.latitude,result.longitude)

                }
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun criarDataPicker(){

        // Set the current date as the default date
        val currentDate = Calendar.getInstance()
        var year = currentDate.get(Calendar.YEAR)
        var month = currentDate.get(Calendar.MONTH)
        var day = currentDate.get(Calendar.DAY_OF_MONTH)

        if(idPeixe.toInt()!=0){
            val dataRegistro = binding.fragmentPeixeCadastroDataCaptura.editText?.text.toString()

            day = dataRegistro.subSequence(0,2).toString().toInt()
            month = dataRegistro.subSequence(3,5).toString().toInt()-1
             year= dataRegistro.subSequence(6,10).toString().toInt()
        }


        // Create a DatePickerDialog with the current date as the default
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                // Do something with the selected date
                // For example, you can update a TextView with the selected date

                binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formatDate(selectedDate))

            },
            year,
            month,
            day
        )

        // Set the maximum date to the current date
        datePickerDialog.datePicker.maxDate = currentDate.timeInMillis

        // Show the DatePickerDialog
        datePickerDialog.show()

    }

    private fun configuraSpinnerVara(){

        lifecycleScope.launch {
            equipamentoDao.buscaEquipamentosVara().collect { equipamentosVara ->
                val spinnerArray = arrayOfNulls<String>(equipamentosVara.size)
                val spinnerMap = HashMap<Int, String>()
                for (i in 0 until equipamentosVara.size) {
                    spinnerMap[i] = equipamentosVara.get(i).id.toString()
                    spinnerArray[i] = equipamentosVara.get(i).descricao
                }

                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.fragmentPeixeCadastroVara.setAdapter(adapter)

                binding.fragmentPeixeCadastroVara.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                       IdVaraSelecionada = spinnerMap[position]!!.toInt()//spinnerMap.keys.toList()[position]  // Or dataList[position].key for custom data class
                       DescVaraSelecionada = spinnerArray[position]  // Or dataList[position].value for custom data class
                        // Use selectedKey and selectedValue as needed
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            }
        }

    }

    private fun configuraSpinnerRecolhimento(){

        lifecycleScope.launch {
            equipamentoDao.buscaEquipamentosRecolhimento().collect { equipamentosVara ->
                val spinnerArray = arrayOfNulls<String>(equipamentosVara.size)
                val spinnerMap = HashMap<Int, String>()
                for (i in 0 until equipamentosVara.size) {
                    spinnerMap[i] = equipamentosVara.get(i).id.toString()
                    spinnerArray[i] = equipamentosVara.get(i).descricao
                }

                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.fragmentPeixeCadastroRecolhimento.setAdapter(adapter)

                binding.fragmentPeixeCadastroRecolhimento.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        IdRecolhimentoSelecionada = spinnerMap[position]!!.toInt()//spinnerMap.keys.toList()[position]  // Or dataList[position].key for custom data class
                        DescRecolhimentoSelecionada = spinnerArray[position]  // Or dataList[position].value for custom data class
                        // Use selectedKey and selectedValue as needed
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }
    }

    private fun configuraSpinnerIsca(){

        lifecycleScope.launch {
            equipamentoDao.buscaEquipamentosIsca().collect { equipamentosVara ->
                val spinnerArray = arrayOfNulls<String>(equipamentosVara.size)
                val spinnerMap = HashMap<Int, String>()
                for (i in 0 until equipamentosVara.size) {
                    spinnerMap[i] = equipamentosVara.get(i).id.toString()
                    spinnerArray[i] = equipamentosVara.get(i).descricao
                }

                val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, spinnerArray)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.fragmentPeixeCadastroIsca.setAdapter(adapter)

                binding.fragmentPeixeCadastroIsca.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        IdIscaSelecionada = spinnerMap[position]!!.toInt()//spinnerMap.keys.toList()[position]  // Or dataList[position].key for custom data class
                        DescIscaSelecionada = spinnerArray[position]  // Or dataList[position].value for custom data class
                        // Use selectedKey and selectedValue as needed
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }

            }
        }
    }

    private fun valorPadraoDataCaptura(){

        val selectedDate = Calendar.getInstance()
        binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formatDate(selectedDate))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun criarTimePicker(){

        // Set the current time as the default time

        var currentTime = Calendar.getInstance()
        var hour = currentTime.get(Calendar.HOUR_OF_DAY)
        var minute = currentTime.get(Calendar.MINUTE)

        if(idPeixe.toInt()!=0){
            val horaMinuto = binding.fragmentPeixeCadastroHoraCaptura.editText?.text.toString()

            hour = horaMinuto.subSequence(0,2).toString().toInt()
            minute = horaMinuto.subSequence(4,5).toString().toInt()
        }

        // Create a TimePickerDialog with the current time as the default
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                // Handle the selected time
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTime.set(Calendar.MINUTE, selectedMinute)
                // Do something with the selected time
                // For example, you can update a TextView with the selected time

                binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formatTime(selectedTime)+":00")

            },
            hour,
            minute,
            true // set to true for 24-hour time format
        )

        // Set the maximum time to the current time
        //timePickerDialog.timePicker.max = currentTime.timeInMillis

        // Show the TimePickerDialog

        timePickerDialog.show()

    }

    private fun valorPadraoHoraCaptura(){

        val selectedTime = Calendar.getInstance()
        binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formatTime(selectedTime)+":00")

    }

    private fun formatDate(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
        val year = calendar.get(Calendar.YEAR)
        //return "$day/$month/$year"
        return String.format("%02d/%02d/%04d",day,month,year)

    }

    private fun formatTime(calendar: Calendar): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private suspend fun buscaPosicaoPeixe(googleMap: GoogleMap)  {

        peixeDao.buscaPorId(idPeixe).collect { peixe ->

            if (peixe != null) {
                addMarker(googleMap,peixe.localizacao.latitude,peixe.localizacao.longitude)
                loadCameraOnMap(googleMap,peixe.localizacao.latitude,peixe.localizacao.longitude)
            }
        }

    }

    private fun addMarker(googleMap:GoogleMap,latitude:Double,longitude:Double){

        marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(latitude,longitude))
        )

        //marker?.tag = peixe
        marker?.showInfoWindow()


    }

    private fun loadCameraOnMap(googleMap:GoogleMap,latitude:Double,longitude:Double){

        googleMap.setOnMapLoadedCallback {

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude,longitude)))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5F), 2000, null);

        }
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

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(com.fishtrophy.R.menu.bottom_options_menu_fragment_peixe_cadastro,menu)
        return super.onCreateOptionsMenu(menu,inflater)
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
    private fun validaCampos(){

        if(binding.fragmentPeixeCadastroDataCaptura.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Data/Hora captura", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroDataCaptura.editText?.requestFocus()
        }else if(binding.fragmentPeixeCadastroPeso.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Peso", Toast.LENGTH_SHORT).show()
            binding.fragmentPeixeCadastroPeso.editText?.requestFocus()
        }else if(binding.fragmentPeixeCadastroTamanho.editText?.text.toString().isEmpty()){
                Toast.makeText(context, "Preencha o campo Tamanho", Toast.LENGTH_SHORT).show()
                binding.fragmentPeixeCadastroTamanho.editText?.requestFocus()
        }else{
            lifecycleScope.launch {
                //usuario.value?.let {
                tentaSalvarPeixe()
                //}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun tentaSalvarPeixe() {
        //usuario.value?.let { usuario ->
        val navController = findNavController()
        try {
            //val usuarioId = defineUsuarioId(usuario)
            val peixe = criaPeixe()

            peixeDao.salva(peixe)
            Toast.makeText(context, "Registro salvo com sucesso! ", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } catch (e: RuntimeException) {
            Log.e("AnimalCadastro", "tentaSalvarPeixe: ", e)
        }

        //}
    }

    private fun configuraSpinnerSexo(    ) {
        val spinner: Spinner = binding.fragmentPeixeCadastroSexo
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            com.fishtrophy.R.array.sexo_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        //campoUsuarioId.
        /*campoUsuarioId.setOnFocusChangeListener { _, focado ->
            if (!focado) {
                usuarioExistenteValido(usuarios)
            }
        }*/
    }

    private fun configuraBotaoFoto() {
        val botaoFoto = binding.fragmentPeixeCadastroBotaoFoto
        botaoFoto.setOnClickListener {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }
    private fun configuraBotaoGaleria() {
        val botaoGaleria = binding.fragmentCadastroPeixeBotaoGaleria
        botaoGaleria.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.fragmentPeixeCadastroImagem.setImageBitmap(imageBitmap)
        }

        if(requestCode==REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == RESULT_OK){
            binding.fragmentPeixeCadastroImagem.setImageURI(data?.data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat", "SuspiciousIndentation")
    private fun criaPeixe(): Peixe {

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
        val  peso= if (pesoTexto.isBlank()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(pesoTexto)
        }


        val campoTamanho =
            binding.fragmentPeixeCadastroTamanho
        val tamanhoTexto = campoTamanho.editText?.text.toString()
        val  tamanho= if (tamanhoTexto.isBlank()) {
            BigDecimal.ZERO
        } else {
            BigDecimal(tamanhoTexto)
        }

        val campoSexo =
            binding.fragmentPeixeCadastroSexo
        var sexo = campoSexo.selectedItem.toString() //getEditText()?.text.toString()

        if(sexo=="Macho"){
            sexo ="M"
        }else if(sexo=="Femea"){
            sexo ="F"
        }else if(sexo=="Indefinido"){
            sexo ="I"
        }

        val bitmap = (binding.fragmentPeixeCadastroImagem.getDrawable() as BitmapDrawable).bitmap
        val saida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
        val img:ByteArray = saida.toByteArray()

     /*   val campoEquipamentoVara =
            binding.fragmentPeixeCadastroVara.selectedItem.toString()
        spinnerMap.get(spinner.getSelectedItemPosition());*/

        return Peixe(
        id = idPeixe,
        dataCaptura=dataCapturaFormatada,
        horaCaptura=horaCapturaFormatada,
        peso = peso,
        tamanho = tamanho,
        sexo = sexo,
        diretorioImagem=gravaImagemDiretorio(this.requireActivity().baseContext,idPeixe,img),
        localizacao = LatLng(marker?.position!!.latitude, marker?.position!!.longitude),
        idEquipamentoVara = IdVaraSelecionada.toLong(),
        idEquipamentoIsca = IdIscaSelecionada.toLong(),
        idEquipamentoRecolhimento = IdRecolhimentoSelecionada.toLong(),

        //usuarioId = usuarioId?.toString()
        )

    }

    private fun tentaCarregarPeixe() {
        idPeixe= args.idPeixe.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(PeixeCarregado: Peixe) {


        val spinner= binding.fragmentPeixeCadastroSexo
        val adapter = ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            com.fishtrophy.R.array.sexo_array,
            android.R.layout.simple_spinner_item)
        var sexo = PeixeCarregado.sexo
        if(PeixeCarregado.sexo=="M"){
            sexo ="Macho"
        }else if(PeixeCarregado.sexo=="F"){
            sexo ="Fêmea"
        }else if(PeixeCarregado.sexo=="I"){
            sexo ="Indefinido"
        }
        val spinnerPosition: Int = adapter.getPosition(sexo)

        binding.fragmentPeixeCadastroSexo.setSelection(spinnerPosition)

        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = PeixeCarregado.dataCaptura.format(formatterDate)
        binding.fragmentPeixeCadastroDataCaptura.editText?.setText(formattedDate)

        val formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formattedTime = PeixeCarregado.horaCaptura.format(formatterTime)
        binding.fragmentPeixeCadastroHoraCaptura.editText?.setText(formattedTime)

        binding.fragmentPeixeCadastroImagem.tentaCarregarImagem(PeixeCarregado.diretorioImagem)

        binding.fragmentPeixeCadastroPeso.editText?.setText(PeixeCarregado.peso.toString())
        binding.fragmentPeixeCadastroTamanho.editText?.setText(PeixeCarregado.tamanho.toString())

    }

    private fun gravaImagemDiretorio(context:Context,id:Long,binario:ByteArray):String {
        val directory = createFolderInAppDirectory(this.requireActivity().baseContext, "imagens/peixe/")//context.filesDir
        //val fileName = id.toString()+"-"+Time(System.currentTimeMillis()).getHours()+Time(System.currentTimeMillis()).getMinutes()+Time(System.currentTimeMillis()).getSeconds()
        val fileName = UUID.randomUUID().toString()
        val file = File(directory, fileName)

        try {
            val outputStream = FileOutputStream(file)
            outputStream.write(binario)
            outputStream.close()
            return directory.toString()+"/"+fileName
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

    }
}