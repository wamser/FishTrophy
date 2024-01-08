package com.fishtrophy.ui.adapter

import android.content.Context
import android.media.Image
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide

import com.fishtrophy.R
import com.fishtrophy.databinding.FragmentPeixeBinding
import com.fishtrophy.databinding.PeixeItemBinding
import com.fishtrophy.extentions.formataDuasCasas
import com.fishtrophy.extentions.tentaCarregarImagem
import com.fishtrophy.model.Peixe
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnSuccessListener
import java.time.format.DateTimeFormatter
import com.google.android.material.bottomsheet.BottomSheetDialog

class ItemMapaAdapter (private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getInfoContents(marker: Marker): View? {

        val lugar = marker.tag as? Peixe ?: return null

        val view = LayoutInflater.from(context).inflate(R.layout.mapa_bottom_sheet_dialog,null)


        val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = lugar.dataCaptura.format(formatterDate)


        //val tamanho = binding.animalItemTamanho
        //tamanho.text = lugar.tamanho.formataDuasCasas()+" CM"

        //val peso = binding.animalItemPeso.text
        //peso.text = lugar.peso.formataDuasCasas()+" KG"

        val visibilidade = if(lugar.diretorioImagem != null){
            View.VISIBLE
        } else {
            View.GONE
        }
        //binding.imageView.visibility = visibilidade
        //binding.imageView.tentaCarregarImagem(lugar.diretorioImagem)

        view.findViewById<TextView>(R.id.mapa_peixe_item_data).text =formattedDate
        view.findViewById<TextView>(R.id.mapa_peixe_item_tamanho).text =lugar.tamanho.formataDuasCasas()+" CM"
        view.findViewById<TextView>(R.id.mapa_peixe_item_peso).text =lugar.peso.formataDuasCasas()+" KG"
        view.findViewById<ImageView>(R.id.mapa_peixe_item_imagem).visibility=visibilidade
        view.findViewById<ImageView>(R.id.mapa_peixe_item_imagem).tentaCarregarImagem(lugar.diretorioImagem)

        /*val imageView= view.findViewById<ImageView>(R.id.mapa_peixe_item_imagem)
        Glide.with(context)
            .load(lugar.diretorioImagem) // Replace with the image URL
            .into(imageView)*/

        //val dialog = BottomSheetDialog(context)

        //dialog.setContentView(view)

        // on below line we are calling
        // a show method to display a dialog.
        //dialog.show()

        return null
    }

}