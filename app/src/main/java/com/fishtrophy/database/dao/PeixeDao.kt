package com.fishtrophy.database.dao

import androidx.room.*
import com.fishtrophy.model.EquipamentosComPeixe
import com.fishtrophy.model.Peixe
import kotlinx.coroutines.flow.Flow

@Dao
interface PeixeDao {

    @Query("SELECT * FROM Peixe")
    fun buscaTodos(): Flow<List<Peixe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(vararg peixe: Peixe)

   @Delete
   suspend fun remove(vararg peixe: Peixe)

    @Query("SELECT * FROM Peixe WHERE id = :id")
    fun buscaPorId(id: Long) : Flow<Peixe?>

   /* @Transaction
    @Query("SELECT * FROM Peixe")
    fun getUsersWithPlaylists(): List<EquipamentosComPeixe>*/

}