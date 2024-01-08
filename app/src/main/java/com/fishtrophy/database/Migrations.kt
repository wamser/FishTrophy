package com.fishtrophy.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `Peixe`
             (`id` INTEGER NOT NULL,
              `dataHoraCaptura` TEXT NOT NULL,              
              `peso` REAL NOT NULL,
              `tamanho` REAL NOT NULL,
              `sexo` TEXT NOT NULL,
              `imagem` BLOB NOT NULL,
              `dataCadastro` TEXT NOT NULL,
              `horaCadastro` TEXT NOT NULL,
|            PRIMARY KEY(`id`))""".trimMargin()
        );
    }
}

val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Peixe` ADD COLUMN `urlImagem` TEXT DEFAULT 'teste' NOT NULL");
    }
}

val MIGRATION_3_4 = object : Migration(3,4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE Peixe SET urlImagem='TESTE' ");
    }
}

val MIGRATION_4_5 = object : Migration(4,5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE Peixe");
    }
}

val MIGRATION_5_6 = object : Migration(5,6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `Peixe`
             (`id` INTEGER NOT NULL,
              `dataCaptura` TEXT NOT NULL,
              `horaCaptura` TEXT NOT NULL,
              `peso` REAL NOT NULL,
              `tamanho` REAL NOT NULL,
              `sexo` TEXT NOT NULL,
              `diretorioImagem` TEXT DEFAULT '' NOT NULL,
|            PRIMARY KEY(`id`))""".trimMargin()
        );
    }
}

val MIGRATION_6_7 = object : Migration(6,7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `Equipamento`
             (`id` INTEGER NOT NULL,
              `descricao` TEXT NOT NULL,
              `tipo` TEXT NOT NULL,
              `diretorioImagem` TEXT DEFAULT '' NOT NULL,
|            PRIMARY KEY(`id`))""".trimMargin()
        );
    }
}

val MIGRATION_7_8 = object : Migration(7,8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Peixe` ADD COLUMN `localizacao` TEXT DEFAULT '0.0' NOT NULL");
    }
}


val MIGRATION_8_9 = object : Migration(8,9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Peixe` ADD COLUMN `idEquipamentoVara` INTEGER");
    }
}

val MIGRATION_9_10 = object : Migration(9,10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Peixe` ADD COLUMN `idEquipamentoIsca` INTEGER");
    }
}

val MIGRATION_10_11 = object : Migration(10,11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `Peixe` ADD COLUMN `idEquipamentoRecolhimento` INTEGER");
    }
}