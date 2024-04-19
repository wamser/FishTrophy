package com.fishtrophy.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

@Entity

class Peixe(

    @PrimaryKey(autoGenerate = true) val id:Long = 0L,
    val dataCaptura: LocalDate,
    val horaCaptura: LocalTime,
    val peso: BigDecimal,
    val tamanho: BigDecimal,
    val sexo: String,
    val diretorioImagem: String,
    val localizacao: LatLng,
    val idEquipamentoVara:Long? =0L,
    val idEquipamentoIsca:Long? = 0L,
    val idEquipamentoRecolhimento:Long? = 0L,
    val especie: String?
)