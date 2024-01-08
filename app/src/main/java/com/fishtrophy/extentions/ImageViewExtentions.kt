package com.fishtrophy.extentions

import android.widget.ImageView
import coil.load

fun ImageView.tentaCarregarImagem(url: String? = null){
    load(url) {
        fallback(com.fishtrophy.R.drawable.erro)
        error(com.fishtrophy.R.drawable.erro)
        placeholder(com.fishtrophy.R.drawable.imagem_padrao)
    }
}


