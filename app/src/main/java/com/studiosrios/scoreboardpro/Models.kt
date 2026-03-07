package com.studiosrios.scoreboardpro

import java.util.*

// --- MODELOS DE DADOS ---

data class JogadorExemplo(
    val id: Int,
    val nome: String,
    var posicao: String = "",
    val altura: String = "",
    val idade: String = "",
    var equipeId: Int = -1,
    var gols: Int = 0 
)

data class EquipeExemplo(
    val id: Int,
    val identificacao: String,
    val nome: String,
    val city: String,
    val jogadores: List<JogadorExemplo> = emptyList()
)

data class EventoPartida(
    val id: String = UUID.randomUUID().toString(),
    val jogadorNome: String = "",
    val equipeNome: String = "",
    val tipo: String = "",
    val minuto: String = ""
)

data class Partida(
    val id: Int,
    val mandanteId: Int,
    val visitanteId: Int,
    val fase: String = "", // Nome da Fase (ex: OITAVAS DE FINAL)
    val nomeConfronto: String = "", // Identificador único do confronto (ex: Oitavas 1)
    val labelMandante: String = "", 
    val labelVisitante: String = "",
    val data: String = "",
    val horario: String = "",
    val local: String = "A DEFINIR",

    var golsMandante: Int? = null,
    var golsVisitante: Int? = null,
    var finalizada: Boolean = false,

    val melhorJogador: String = "",
    val cartoesAmarelosMandante: Int = 0,
    val cartoesVermelhosMandante: Int = 0,
    val cartoesAmarelosVisitante: Int = 0,
    val cartoesVermelhosVisitante: Int = 0,
    val eventos: List<EventoPartida> = emptyList(),

    val titularesMandante: List<Int> = emptyList(),
    val reservasMandante: List<Int> = emptyList(),
    val titularesVisitante: List<Int> = emptyList(),
    val reservasVisitante: List<Int> = emptyList(),

    val arbitroPrincipal: String = "",
    val assistente1: String = "",
    val assistente2: String = "",
    val tecnicoMandante: String = "",
    val auxiliar1Mandante: String = "",
    val auxiliar2Mandante: String = "",
    val massagistaMandante: String = "",
    val tecnicoVisitante: String = "",
    val auxiliar1Visitante: String = "",
    val auxiliar2Visitante: String = "",
    val massagistaVisitante: String = ""
)

data class LinhaTabela(
    val equipeId: Int,
    val nome: String,
    val pontos: Int,
    val jogos: Int,
    val vitorias: Int,
    val empates: Int,
    val derrotas: Int,
    val gm: Int,
    val gs: Int,
    val sg: Int,
    val amarelos: Int = 0,
    val vermelhos: Int = 0
)

data class ConfiguracoesCampeonato(
    val modoReturno: Boolean = false, 
    val modoIdaEVoltaMataMata: Boolean = true,
    val modoIdaEVoltaFinal: Boolean = false, // Adicionado para a final
    val criteriosDesempate: List<String> = listOf("Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar"),
    val exibirCartoesNaTabela: Boolean = false
)

data class ConfigGrupo(
    val nome: String = "",
    val qtdTimes: Int = 0,
    val qtdClassificados: Int = 0
)

data class CampeonatoSalvo(
    val id: Int,
    val nomeExibicao: String,
    val modelo: String,
    val equipes: List<EquipeExemplo>,
    val partidas: List<Partida>,
    val configs: ConfiguracoesCampeonato = ConfiguracoesCampeonato(),
    val gruposConfig: List<ConfigGrupo> = emptyList()
)
