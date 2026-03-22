package com.studiosrios.scoreboardpro.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studiosrios.scoreboardpro.*

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromJogadorList(value: List<JogadorExemplo>?): String = gson.toJson(value)
    @TypeConverter
    fun toJogadorList(value: String): List<JogadorExemplo>? = 
        gson.fromJson(value, object : TypeToken<List<JogadorExemplo>>() {}.type)

    @TypeConverter
    fun fromEquipeList(value: List<EquipeExemplo>?): String = gson.toJson(value)
    @TypeConverter
    fun toEquipeList(value: String): List<EquipeExemplo>? = 
        gson.fromJson(value, object : TypeToken<List<EquipeExemplo>>() {}.type)

    @TypeConverter
    fun fromPartidaList(value: List<Partida>?): String = gson.toJson(value)
    @TypeConverter
    fun toPartidaList(value: String): List<Partida>? = 
        gson.fromJson(value, object : TypeToken<List<Partida>>() {}.type)

    @TypeConverter
    fun fromConfiguracoes(value: ConfiguracoesCampeonato?): String = gson.toJson(value)
    @TypeConverter
    fun toConfiguracoes(value: String): ConfiguracoesCampeonato? = 
        gson.fromJson(value, ConfiguracoesCampeonato::class.java)

    @TypeConverter
    fun fromConfigGrupoList(value: List<ConfigGrupo>?): String = gson.toJson(value)
    @TypeConverter
    fun toConfigGrupoList(value: String): List<ConfigGrupo>? = 
        gson.fromJson(value, object : TypeToken<List<ConfigGrupo>>() {}.type)

    @TypeConverter
    fun fromPatrocinadorList(value: List<Patrocinador>?): String = gson.toJson(value)
    @TypeConverter
    fun toPatrocinadorList(value: String): List<Patrocinador>? = 
        gson.fromJson(value, object : TypeToken<List<Patrocinador>>() {}.type)

    @TypeConverter
    fun fromEventoPartidaList(value: List<EventoPartida>?): String = gson.toJson(value)
    @TypeConverter
    fun toEventoPartidaList(value: String): List<EventoPartida>? = 
        gson.fromJson(value, object : TypeToken<List<EventoPartida>>() {}.type)

    @TypeConverter
    fun fromIntList(value: List<Int>?): String = gson.toJson(value)
    @TypeConverter
    fun toIntList(value: String): List<Int>? = 
        gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)

    @TypeConverter
    fun fromIntStringMap(value: Map<Int, String>?): String = gson.toJson(value)
    @TypeConverter
    fun toIntStringMap(value: String): Map<Int, String>? = 
        gson.fromJson(value, object : TypeToken<Map<Int, String>>() {}.type)

    @TypeConverter
    fun fromIntDoubleMap(value: Map<Int, Double>?): String = gson.toJson(value)
    @TypeConverter
    fun toIntDoubleMap(value: String): Map<Int, Double>? = 
        gson.fromJson(value, object : TypeToken<Map<Int, Double>>() {}.type)
}
