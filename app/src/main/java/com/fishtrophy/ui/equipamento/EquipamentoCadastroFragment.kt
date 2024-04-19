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
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_SELECT_IMAGE_IN_ALBUM = 2

@Suppress("DEPRECATION")
class EquipamentoCadastroFragment : Fragment() {
    private val args : EquipamentoCadastroFragmentArgs by navArgs()
    private var _binding: FragmentEquipamentoCadastroBinding? = null
    private val binding: FragmentEquipamentoCadastroBinding get() = _binding!!
    private var idEquipamento= 0L
    private val equipamentoDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.equipamentoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    private fun criaPastaDiretorioApp(context: Context, folderName: String): File? {
        val appDirectory = context.filesDir
        val folder = File(appDirectory, folderName)

        return if (!folder.exists()) {
            val isDirectoryCreated = folder.mkdir()
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

        binding.fragmentEquipamentoCadastroImagem.setOnClickListener{
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
                it?.let {

                    preencheCampos(it)
                }
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {


        inflater.inflate(R.menu.bottom_options_menu_fragment_peixe_cadastro,menu)

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

        ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            R.array.fragment_equipamento_cadastro_tipo_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter
        }

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

        val imgDrawable = binding.fragmentEquipamentoCadastroImagem.getDrawable()
        if (imgDrawable != null) {
            bitmap = (imgDrawable as BitmapDrawable).bitmap
            val saida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)
            val img = saida.toByteArray()
            imgDiretorio = gravaImagemDiretorio(img)
        }


        return Equipamento(
           id = idEquipamento,
           descricao = descricao,
           tipo = tipo,
           diretorioImagem = imgDiretorio
       )
    }

    private fun tentaCarregarEquipamento() {
        idEquipamento= args.idEquipamento.toLong()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(equipamentoCarregado: Equipamento) {

        val adapter = ArrayAdapter.createFromResource(
            this.requireActivity().baseContext,
            R.array.fragment_equipamento_cadastro_tipo_array,
            android.R.layout.simple_spinner_item)
        var tipo = equipamentoCarregado.tipo
        when (equipamentoCarregado.tipo) {
            "1" -> {
                tipo ="Isca"
            }
            "2" -> {
                tipo ="Vara"
            }
            "3" -> {
                tipo ="Carretilha"
            }
            "4" -> {
                tipo ="Molinete"
            }
        }
        val spinnerPosition: Int = adapter.getPosition(tipo)

        binding.fragmentEquipamentoCadastroTipo.setSelection(spinnerPosition)
        binding.fragmentEquipamentoCadastroImagem.tentaCarregarImagem(equipamentoCarregado.diretorioImagem)
        binding.fragmentEquipamentoCadastroDescricao.editText?.setText(equipamentoCarregado.descricao)

    }

   private fun gravaImagemDiretorio( binario:ByteArray):String {

       val directory = criaPastaDiretorioApp(this.requireActivity().baseContext, "imagens/equipamento/")
       val fileName = UUID.randomUUID().toString()
       val file = File(directory, fileName)

       return try {
           val outputStream = FileOutputStream(file)
           outputStream.write(binario)
           outputStream.close()
           directory.toString()+"/"+fileName
       } catch (e: IOException) {
           e.printStackTrace()
           ""
       }

    }

}