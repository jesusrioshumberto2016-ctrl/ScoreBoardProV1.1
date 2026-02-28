package com.studiosrios.scoreboardpro

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.*

// --- MODELOS DE DADOS ---
data class JogadorExemplo(val id: Int, val nome: String, var posicao: String, val altura: String, val idade: String, var equipeId: Int = -1)
data class EquipeExemplo(val id: Int, val identificacao: String, val nome: String, val city: String)

data class EventoSumula(
    val id: String = UUID.randomUUID().toString(),
    val jogadorNome: String,
    val equipeNome: String,
    val tipo: String,
    val minuto: String = ""
)

data class Partida(
    val id: Int,
    val mandanteId: Int,
    val visitanteId: Int,
    var golsMandante: String = "",
    var golsVisitante: String = "",
    var finalizada: Boolean = false,
    var data: String = "00/00",
    var horario: String = "00:00",
    var local: String = "A definir",
    var melhorJogador: String = "Não definido",
    val eventos: MutableList<EventoSumula> = mutableStateListOf()
)

data class LinhaTabela(val nome: String, val pontos: Int = 0, val jogos: Int = 0, val vitorias: Int = 0, val empates: Int = 0, val derrotas: Int = 0, val gm: Int = 0, val gs: Int = 0, val sg: Int = 0)

data class ConfiguracoesCampeonato(
    var criteriosDesempate: List<String> = listOf("Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar"),
    var modoReturno: Boolean = false
)

data class CampeonatoSalvo(
    val id: Int,
    val nomeExibicao: String,
    val modelo: String,
    val equipes: List<EquipeExemplo>,
    val partidas: List<Partida>,
    val configs: ConfiguracoesCampeonato = ConfiguracoesCampeonato()
)