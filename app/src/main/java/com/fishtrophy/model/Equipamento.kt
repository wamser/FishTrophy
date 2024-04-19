package com.fishtrophy.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
class Equipamento(
    @PrimaryKey(autoGenerate = true) val id:Long = 0L,
    val descricao: String,
    val tipo: String,
    val diretorioImagem: String
): Parcelable