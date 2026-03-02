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
    var gols: Int = 0 // Mantido para a Artilharia
)

data class EquipeExemplo(
    val id: Int,
    val identificacao: String,
    val nome: String,
    val city: String,
    val jogadores: List<JogadorExemplo> = emptyList() // Mantido para a Artilharia
)

data class EventoSumula(
    val id: String = UUID.randomUUID().toString(),
    val jogadorNome: String,
    val equipeNome: String,
    val tipo: String,
    val minuto: String = ""
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
    val data: String = "",
    val horario: String = "",
    val local: String = "A DEFINIR",

    // Editáveis para o placar funcionar
    var golsMandante: Int? = null,
    var golsVisitante: Int? = null,
    var finalizada: Boolean = false,

    val melhorJogador: String = "",
    val cartoesAmarelosMandante: Int = 0,
    val cartoesVermelhosMandante: Int = 0,
    val cartoesAmarelosVisitante: Int = 0,
    val cartoesVermelhosVisitante: Int = 0,
    val eventos: List<EventoPartida> = emptyList(),

    // Escalações
    val titularesMandante: List<Int> = emptyList(),
    val reservasMandante: List<Int> = emptyList(),
    val titularesVisitante: List<Int> = emptyList(),
    val reservasVisitante: List<Int> = emptyList(),

    // Arbitragem e Comissão
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
    val criteriosDesempate: List<String> = listOf("Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar"),
    val exibirCartoesNaTabela: Boolean = false
)

data class ConfigGrupo(
    val nome: String = "",
    val qtdTimes: Int = 0,
    val qtdClassificados: Int = 0
)

// --- AQUI ESTÁ A CLASSE QUE FALTAVA ---
data class CampeonatoSalvo(
    val id: Int,
    val nomeExibicao: String,
    val modelo: String,
    val equipes: List<EquipeExemplo>,
    val partidas: List<Partida>,
    val configs: ConfiguracoesCampeonato = ConfiguracoesCampeonato(),
    val gruposConfig: List<ConfigGrupo> = emptyList() // Adicionado para a Libertadores funcionar certinho
)
