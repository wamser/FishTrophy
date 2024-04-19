package com.fishtrophy.extentions

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fishtrophy.R
import com.fishtrophy.database.AppDatabase
import com.fishtrophy.database.dao.PeixeDao
import com.fishtrophy.databinding.MapaBottomSheetDialogBinding
import com.fishtrophy.ui.mapa.MapaFragmentDirections
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CustomBottomSheetFragment : BottomSheetDialogFragment() {
    private val peixeDao by lazy {
        val db = AppDatabase.instancia(this.requireActivity().baseContext)
        db.peixeDao()
    }
    private var _binding: MapaBottomSheetDialogBinding? = null
    private val binding: MapaBottomSheetDialogBinding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mapa_bottom_sheet_dialog, container, false)
        _binding = MapaBottomSheetDialogBinding.inflate(inflater, container, false)

        val bundle = Bundle()
        val peixeId =
            arguments?.getLong("peixeID")
        lifecycleScope.launch {
            var peixe:PeixeDao.PeixeWithEquipamento
            if (peixeId != null) {
                peixeDao.buscaPorIdCompleto(peixeId).let { peixeComEquipamento ->

                    peixeComEquipamento.observe(viewLifecycleOwner) { it ->
                        peixe =it!![0]
                        preencheCampos(peixe, view)
                    }

                }
            }
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun preencheCampos(PeixeCarregado: PeixeDao.PeixeWithEquipamento, view: View) {


        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = PeixeCarregado.dataCaptura.format(formatterDate)
        val visibilidade = if (PeixeCarregado.diretorioImagem != null) {
            View.VISIBLE
        } else {
            View.GONE
        }

        view.findViewById<TextView>(R.id.mapa_peixe_item_data).text = formattedDate
        view.findViewById<TextView>(R.id.mapa_peixe_item_tamanho).text =
            PeixeCarregado.tamanho.formataDuasCasas() + " CM"
        view.findViewById<TextView>(R.id.mapa_peixe_item_peso).text =
            PeixeCarregado.peso.formataDuasCasas() + " KG"
        view.findViewById<ImageView>(R.id.mapa_peixe_item_imagem).visibility = visibilidade
        view.findViewById<ImageView>(R.id.mapa_peixe_item_imagem)
            .tentaCarregarImagem(PeixeCarregado.diretorioImagem)

        view.findViewById<Button>(R.id.mapa_peixe_item_botao_detalhes).setOnClickListener {
            val direction =
                MapaFragmentDirections.actionNavigationMapaToPeixeDetalhesFragment(PeixeCarregado.id.toInt())
            findNavController().navigate(direction)
            val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            navView.visibility = View.GONE

            this.dismiss()


    }

}
}