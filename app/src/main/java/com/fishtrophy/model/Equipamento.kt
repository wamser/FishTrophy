package com.fishtrophy.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Equipamento(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var descricao: String,
    var tipo: String,
    val diretorioImagem: String,

    ): Parcelable{

   constructor(
        id: Long,  descricao: String, tipo: String
    ) : this(id, descricao, tipo,"" ){
        this.id = id
        this.descricao = descricao
        this.tipo = tipo
    }


}


