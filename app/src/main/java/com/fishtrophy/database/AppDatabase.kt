package com.fishtrophy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fishtrophy.database.converter.Converters
import com.fishtrophy.database.dao.EquipamentoDao
import com.fishtrophy.database.dao.PeixeDao
import com.fishtrophy.model.Equipamento
import com.fishtrophy.model.Peixe

@Database(entities = [
    Peixe::class,
    Equipamento::class
    ], version = 12, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun peixeDao(): PeixeDao
    abstract fun equipamentoDao(): EquipamentoDao

    companion object {
        @Volatile private var db: AppDatabase?=null
        fun instancia(context: Context) :AppDatabase{

            return db?:Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "fishtrophy.db"
            ).addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
              ).build().also{
                db=it
            }
        }
    }

}