package com.studiosrios.scoreboardpro.data.local

import androidx.room.*
import com.studiosrios.scoreboardpro.CampeonatoSalvo
import com.studiosrios.scoreboardpro.EquipeExemplo
import com.studiosrios.scoreboardpro.JogadorExemplo
import kotlinx.coroutines.flow.Flow

@Dao
interface JogadorDao {
    @Query("SELECT * FROM jogadores")
    fun getAll(): Flow<List<JogadorExemplo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jogadores: List<JogadorExemplo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jogador: JogadorExemplo)

    @Delete
    suspend fun delete(jogador: JogadorExemplo)

    @Query("DELETE FROM jogadores")
    suspend fun deleteAll()
}

@Dao
interface EquipeDao {
    @Query("SELECT * FROM equipes")
    fun getAll(): Flow<List<EquipeExemplo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipes: List<EquipeExemplo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipe: EquipeExemplo)

    @Delete
    suspend fun delete(equipe: EquipeExemplo)

    @Query("DELETE FROM equipes")
    suspend fun deleteAll()
}

@Dao
interface CampeonatoDao {
    @Query("SELECT * FROM campeonatos")
    fun getAll(): Flow<List<CampeonatoSalvo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(campeonatos: List<CampeonatoSalvo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(campeonato: CampeonatoSalvo)

    @Delete
    suspend fun delete(campeonato: CampeonatoSalvo)

    @Query("DELETE FROM campeonatos")
    suspend fun deleteAll()
}
