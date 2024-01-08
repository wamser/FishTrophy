package com.fishtrophy.model

import androidx.room.Embedded
import androidx.room.Relation

data class EquipamentosComPeixe (
    @Embedded val equipamento: Equipamento,
    @Relation(
        parentColumn = "id",
        entityColumn = "idEquipamentoVara"
    )
    val playlists: List<Peixe>)