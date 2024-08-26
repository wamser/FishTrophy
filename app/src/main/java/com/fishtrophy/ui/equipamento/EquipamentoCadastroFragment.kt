package com.fishtrophy.ui.equipamento

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
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
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


private const val REQUEST_IMAGE_CAPTURE = 3
private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2

@Suppress("DEPRECATION")
class EquipamentoCadastroFragment : Fragment() {
    private val args: EquipamentoCadastroFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoCadastroBinding? = null
    private val binding: FragmentEquipamentoCadastroBinding get() = _binding!!
    private var idEquipamento = 0L
    private var fotoAlterada = false
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }
    private lateinit var imagemFile: File
    private var imagemCaminho: String = ""
    private var acessouGaleria = false
    private var acessouCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

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
        if (idEquipamento.toInt() != 0) {
            buscaEquipamento()

        }

        binding.fragmentEquipamentoCadastroImagem.setOnClickListener {
            val direction =
                EquipamentoCadastroFragmentDirections.actionEquipamentoCadastroFragmentToEquipamentoImagemFragment(
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
    private fun buscaEquipamento() {
        lifecycleScope.launch {
            equipamentoDao.buscaPorId(idEquipamento).collect { it ->
                it?.let {

                    preencheCampos(it)
                }
            }
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

        if (binding.fragmentEquipamentoCadastroDescricao.editText?.text.toString().isEmpty()) {
            Toast.makeText(context, "Preencha o campo Descrição", Toast.LENGTH_SHORT).show()
            binding.fragmentEquipamentoCadastroDescricao.editText?.requestFocus()
        } else {
            lifecycleScope.launch {
                tentaSalvarEquipamento()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun tentaSalvarEquipamento() {
        val navController = findNavController()
        try {
            val equipamento = criaEquipamento()

            if (idEquipamento.toInt() != 0) {
                equipamentoDao.altera(equipamento)
            } else {
                equipamentoDao.salva(equipamento)
            }
            Toast.makeText(context, "Registro salvo com sucesso! ", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } catch (e: RuntimeException) {
            Log.e("EquipamentoCadastro", "tentaSalvarPeixe: ", e)
        }
    }

    private fun configuraDropDownTipo() {
        val spinner: Spinner = binding.fragmentEquipamentoCadastroTipo

        ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            R.array.fragment_equipamento_cadastro_tipo_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter
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
        val botaoFoto = binding.fragmentEquipamentoCadastroBotaoFoto

        botaoFoto.setOnClickListener {
            acessouGaleria = false
            acessouCamera = true
            imagemFile = createImageFile()
            tiraFotoIntent()
        }
    }

    private fun tiraFotoIntent() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val imageUri =
            FileProvider.getUriForFile(
                this.requireActivity().baseContext,
                "com.fishtrophy.android.fileprovider",
                imagemFile
            )
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun configuraBotaoGaleria() {
        val botaoGaleria = binding.fragmentCadastroEquipamentoBotaoGaleria

        botaoGaleria.setOnClickListener {
            acessouGaleria = true
            acessouCamera = false
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            fotoAlterada = true
            val capturedImageFileBitMap = lidaComImagemCapturada(imagemFile.toString())
            binding.fragmentEquipamentoCadastroImagem.setImageBitmap(capturedImageFileBitMap)

        }

        if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == RESULT_OK) {
            fotoAlterada = true
            binding.fragmentEquipamentoCadastroImagem.setImageURI(data?.data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private fun criaEquipamento(): Equipamento {
        var imgDiretorio = ""
        val bitmap: Bitmap
        val campoTipo =
            binding.fragmentEquipamentoCadastroTipo
        var tipo = campoTipo.selectedItem.toString()

        when (tipo) {
            "Isca" -> {
                tipo = "1"
            }

            "Vara" -> {
                tipo = "2"
            }

            "Carretilha" -> {
                tipo = "3"
            }

            "Molinete" -> {
                tipo = "4"
            }
        }

        val campoDescricao =
            binding.fragmentEquipamentoCadastroDescricao
        val descricao = campoDescricao.editText?.text.toString()

        if (fotoAlterada) {

            if(imagemCaminho!=""){
                deletaArquivo(imagemCaminho)
            }

            if (acessouGaleria) {
                val imgDrawable = binding.fragmentEquipamentoCadastroImagem.getDrawable()
                if (imgDrawable != null) {
                    bitmap = (imgDrawable as BitmapDrawable).bitmap
                    val saida = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)

                    val img = saida.toByteArray()
                    imgDiretorio = gravaImagemDiretorio(img)
                }
            } else if(acessouCamera) {
                imgDiretorio = imagemFile.toString()
            }
            return Equipamento(
                id = idEquipamento,
                descricao = descricao,
                tipo = tipo,
                diretorioImagem = imgDiretorio
            )
        } else {
            return Equipamento(
                id = idEquipamento,
                descricao = descricao,
                tipo = tipo,
                diretorioImagem =imagemCaminho
            )
        }
    }

    private fun deletaArquivo(arquivoCaminho:String){

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

    private fun tentaCarregarEquipamento() {
        idEquipamento = args.idEquipamento.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(equipamentoCarregado: Equipamento) {

        val adapter = ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            R.array.fragment_equipamento_cadastro_tipo_array,
            android.R.layout.simple_spinner_item
        )
        var tipo = equipamentoCarregado.tipo
        when (equipamentoCarregado.tipo) {
            "1" -> {
                tipo = "Isca"
            }

            "2" -> {
                tipo = "Vara"
            }

            "3" -> {
                tipo = "Carretilha"
            }

            "4" -> {
                tipo = "Molinete"
            }
        }
        val spinnerPosition: Int = adapter.getPosition(tipo)
        imagemCaminho=equipamentoCarregado.diretorioImagem
        binding.fragmentEquipamentoCadastroTipo.setSelection(spinnerPosition)
        binding.fragmentEquipamentoCadastroImagem.tentaCarregarImagem(equipamentoCarregado.diretorioImagem)
        binding.fragmentEquipamentoCadastroDescricao.editText?.setText(equipamentoCarregado.descricao)

    }

    private fun gravaImagemDiretorio(binario: ByteArray): String {

        val directory =
            criaPastaDiretorioApp(this.requireActivity().baseContext, "imagens/equipamento/")
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

}