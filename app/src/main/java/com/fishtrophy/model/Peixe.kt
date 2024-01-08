package com.fishtrophy.model

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.annotations.Nullable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity/*(foreignKeys = [
    ForeignKey(
        entity = Equipamento::class,
        parentColumns = ["id"],
        childColumns = ["idEquipamentoVara"]
)])*/
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
    val idEquipamentoRecolhimento:Long? = 0L
    //val raca: String,
    //val usuarioId: String? = null
)