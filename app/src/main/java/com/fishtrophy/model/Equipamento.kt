package com.fishtrophy.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Equipamento(
    @PrimaryKey(autoGenerate = true) val id:Long = 0L,
    val descricao: String,
    val tipo: String,
    val diretorioImagem: String

    //val usuarioId: String? = null
)