package com.fishtrophy.database.dao

import androidx.room.*
import com.fishtrophy.model.Equipamento
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipamentoDao {

    @Query("SELECT * FROM Equipamento")
    fun buscaTodos(): Flow<List<Equipamento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(vararg equipamento: Equipamento)

    @Update
    suspend fun altera(vararg equipamento: Equipamento)

    @Delete
    suspend fun remove(vararg equipamento: Equipamento)

    @Query("SELECT * FROM Equipamento WHERE id = :id")
    fun buscaPorId(id: Long) : Flow<Equipamento?>

    @Query("SELECT id FROM Equipamento ORDER BY id DESC ")
    fun buscaMaxId() :Long


    @Query("SELECT * FROM Equipamento WHERE tipo=2 ORDER BY descricao DESC ")
    fun buscaEquipamentosVara():Flow<List<Equipamento>>

    @Query("SELECT * FROM Equipamento WHERE tipo=1 ORDER BY descricao DESC ")
    fun buscaEquipamentosIsca():Flow<List<Equipamento>>

    @Query("SELECT * FROM Equipamento WHERE tipo=3 ORDER BY descricao DESC ")
    fun buscaEquipamentosRecolhimento():Flow<List<Equipamento>>

}