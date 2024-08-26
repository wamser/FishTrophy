package com.fishtrophy.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.fishtrophy.model.Peixe
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface PeixeDao {

    @Query("SELECT * FROM Peixe")
    fun buscaTodos(): Flow<List<Peixe>>

    @Query("SELECT * FROM Peixe Where idEquipamentoVara=:id or idEquipamentoRecolhimento=:id or idEquipamentoIsca=:id")
    fun buscaPeixePorEquipamento(id: Long): Flow<List<Peixe>>

    @Query("SELECT * FROM Peixe Where idEquipamentoRecolhimento=:id")
    fun buscaPeixePorRecolhimento(id: Long): Flow<List<Peixe>>

    @Query("SELECT * FROM Peixe Where idEquipamentoIsca=:id")
    fun buscaPeixePorIsca(id: Long): Flow<List<Peixe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(vararg peixe: Peixe)

    @Delete
    suspend fun remove(vararg peixe: Peixe)

    @Query("""
        SELECT 
            *
        FROM
            Peixe 
        WHERE
            id = :id""")
    fun buscaPorId(id: Long) :  Flow<Peixe?>

    @Query(
        """
        SELECT Peixe.*,
            EquipamentoIsca.descricao as iscaDescricao,
            EquipamentoVara.descricao as varaDescricao,
            EquipamentoRecolhimento.descricao as recolhimentoDescricao
        FROM Peixe 
            LEFT JOIN Equipamento EquipamentoIsca ON EquipamentoIsca.id=Peixe.idEquipamentoIsca 
            LEFT JOIN Equipamento EquipamentoRecolhimento ON EquipamentoRecolhimento.id=Peixe.idEquipamentoRecolhimento 
            LEFT JOIN Equipamento EquipamentoVara ON EquipamentoVara.id=Peixe.idEquipamentoVara 
        WHERE
            Peixe.id = :id"""
    )
    fun buscaPorIdCompleto(id: Long): LiveData<List<PeixeWithEquipamento>>

    data class PeixeWithEquipamento(
        val id: Long,
        val dataCaptura: LocalDate,
        val horaCaptura: LocalTime,
        val peso: BigDecimal,
        val tamanho: BigDecimal,
        val sexo: String,
        val diretorioImagem: String,
        val localizacao: LatLng,
        val idEquipamentoVara: Long,
        val idEquipamentoIsca: Long,
        val idEquipamentoRecolhimento: Long,
        val especie: String,
        val iscaDescricao: String,
        val varaDescricao: String,
        val recolhimentoDescricao: String
    )
}