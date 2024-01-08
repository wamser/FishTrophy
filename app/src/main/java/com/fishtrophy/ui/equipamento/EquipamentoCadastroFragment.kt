package com.fishtrophy.ui.equipamento

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fishtrophy.database.AppDatabase
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.fishtrophy.R
import com.fishtrophy.databinding.FragmentEquipamentoCadastroBinding
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Equipamento
import com.fishtrophy.ui.peixe.PeixeCadastroFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.Time
import java.util.UUID


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2

@Suppress("DEPRECATION")
class EquipamentoCadastroFragment : Fragment() {
    private val args : EquipamentoCadastroFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoCadastroBinding? = null
    private val binding: FragmentEquipamentoCadastroBinding get() = _binding!!
    private var url: String? = null
    private var idEquipamento= 0L
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.

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
        val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        _binding = FragmentEquipamentoCadastroBinding.inflate(inflater, container, false)
        val root: View = binding.root

        navView.visibility = View.GONE

        tentaCarregarEquipamento()
        if(idEquipamento.toInt()!=0) {
            buscaPeixe()

        }

        binding.fragmentEquipamentoCadastroImagem.setOnClickListener(){
            val direction = EquipamentoCadastroFragmentDirections.actionEquipamentoCadastroFragmentToEquipamentoImagemFragment(
                idEquipamento.toInt()
            )
            findNavController().navigate(direction)
        }

        configuraBotaoGaleria()
        configuraBotaoFoto()
        configuraDropDownTipo()

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buscaPeixe() {
        lifecycleScope.launch {
            equipamentoDao.buscaPorId(idEquipamento).collect{ it->
                //withContext(Dispatchers.Main) {
                it?.let {

                    preencheCampos(it)
                }
                //val navController = findNavController()
                //navController.navigate(R.id.nav_animal)
                // }
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

        if(binding.fragmentEquipamentoCadastroDescricao.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Descrição", Toast.LENGTH_SHORT).show()
            binding.fragmentEquipamentoCadastroDescricao.editText?.requestFocus()
        }else{
            lifecycleScope.launch {
                //usuario.value?.let {
                tentaSalvarEquipamento()
                //}
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun tentaSalvarEquipamento() {
        //usuario.value?.let { usuario ->
        val navController = findNavController()
        try {
            //val usuarioId = defineUsuarioId(usuario)
            val equipamento = criaEquipamento()

            equipamentoDao.salva(equipamento)
            Toast.makeText(context, "Registro salvo com sucesso! ", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } catch (e: RuntimeException) {
            Log.e("EquipamentoCadastro", "tentaSalvarPeixe: ", e)
        }

        //}
    }

    private fun configuraDropDownTipo() {
        val spinner: Spinner = binding.fragmentEquipamentoCadastroTipo
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            com.fishtrophy.R.array.fragment_equipamento_cadastro_tipo_array,
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
        val botaoFoto = binding.fragmentEquipamentoCadastroBotaoFoto
        botaoFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }
    private fun configuraBotaoGaleria() {
        val botaoGaleria = binding.fragmentCadastroEquipamentoBotaoGaleria
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
            binding.fragmentEquipamentoCadastroImagem.setImageBitmap(imageBitmap)
        }

        if(requestCode==REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == RESULT_OK){
            binding.fragmentEquipamentoCadastroImagem.setImageURI(data?.data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private fun criaEquipamento(): Equipamento {
        var imgDiretorio:String=""
        val bitmap:Bitmap
        val campoTipo =
            binding.fragmentEquipamentoCadastroTipo
        var tipo = campoTipo.selectedItem.toString() //getEditText()?.text.toString()

        if(tipo=="Isca"){
            tipo ="1"
        }else if(tipo=="Vara"){
            tipo ="2"
        }else if(tipo=="Carretilha"){
            tipo ="3"
        }else if(tipo=="Molinete"){
            tipo ="4"
        }

        val campoDescricao =
            binding.fragmentEquipamentoCadastroDescricao
        val descricao = campoDescricao.editText?.text.toString()

        val imgDrawable=binding.fragmentEquipamentoCadastroImagem.getDrawable()
        if(imgDrawable!=null) {
            bitmap = (imgDrawable as BitmapDrawable).bitmap
            val saida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
            val img = saida.toByteArray()
            imgDiretorio = gravaImagemDiretorio(this.requireActivity().baseContext,idEquipamento,img)
        }


         val equipamento=Equipamento(
            id = idEquipamento,
            descricao=descricao,
            tipo = tipo,
            diretorioImagem=imgDiretorio
            //usuarioId = usuarioId?.toString()
        )
        return equipamento
    }

    private fun tentaCarregarEquipamento() {
        idEquipamento= args.idEquipamento.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(EquipamentoCarregado: Equipamento) {


        val spinner= binding.fragmentEquipamentoCadastroTipo
        val adapter = ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            com.fishtrophy.R.array.fragment_equipamento_cadastro_tipo_array,
            android.R.layout.simple_spinner_item)
        var tipo = EquipamentoCarregado.tipo
        if(EquipamentoCarregado.tipo=="1"){
            tipo ="Isca"
        }else if(EquipamentoCarregado.tipo=="2"){
            tipo ="Vara"
        }else if(EquipamentoCarregado.tipo=="3"){
            tipo ="Carretilha"
        }else if(EquipamentoCarregado.tipo=="4"){
            tipo ="Molinete"
        }
        val spinnerPosition: Int = adapter.getPosition(tipo)

        binding.fragmentEquipamentoCadastroTipo.setSelection(spinnerPosition)
        binding.fragmentEquipamentoCadastroImagem.tentaCarregarImagem(EquipamentoCarregado.diretorioImagem)
        binding.fragmentEquipamentoCadastroDescricao.editText?.setText(EquipamentoCarregado.descricao)

    }

   private fun gravaImagemDiretorio(context:Context, id:Long, binario:ByteArray):String {

        //var idMax=0L

        //if(id.toInt()==0){
            //lifecycleScope.launch {
            //CoroutineScope(Dispatchers.IO).launch {
                //idMax=equipamentoDao.buscaMaxId()
            //}

        //}

        val directory = createFolderInAppDirectory(this.requireActivity().baseContext, "imagens/equipamento/")
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