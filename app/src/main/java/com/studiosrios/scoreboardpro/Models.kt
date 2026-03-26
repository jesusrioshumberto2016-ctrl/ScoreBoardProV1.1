package com.studiosrios.scoreboardpro

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Keep
@Entity(tableName = "jogadores")
data class JogadorExemplo(
    @PrimaryKey val id: Int = 0,
    val nome: String = "",
    var posicao: String = "",
    val altura: String = "",
    val idade: String = "",
    var equipeId: Int = -1,
    var gols: Int = 0,
    val fotoUri: String = "",
    val apelido: String = ""
)

@Keep
data class Patrocinador(
    val nome: String = "",
    val fotoUri: String = ""
)

@Keep
@Entity(tableName = "equipes")
data class EquipeExemplo(
    @PrimaryKey val id: Int = 0,
    val identificacao: String = "",
    val nome: String = "",
    val city: String = "",
    val jogadores: List<JogadorExemplo> = emptyList(),
    val patrocinadores: List<Patrocinador> = emptyList(),
    val escudoUri: String = "" 
)

@Keep
data class EventoPartida(
    val id: String = UUID.randomUUID().toString(),
    val jogadorNome: String = "",
    val equipeNome: String = "",
    val tipo: String = "",
    val minuto: String = ""
)

@Keep
data class Partida(
    val id: Int = 0,
    val mandanteId: Int = 0,
    val visitanteId: Int = 0,
    val fase: String = "",
    val nomeConfronto: String = "",
    val labelMandante: String = "", 
    val labelVisitante: String = "",
    val data: String = "",
    val horario: String = "",
    val local: String = "A DEFINIR",
    var golsMandante: Int? = null,
    var golsVisitante: Int? = null,
    var finalizada: Boolean = false,
    var penaltisMandante: Int? = null,
    var penaltisVisitante: Int? = null,
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
    val posicoesNoJogo: Map<String, String> = emptyMap(),
    val formacaoMandante: String = "4-4-2",
    val formacaoVisitante: String = "4-4-2",
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
    val massagistaVisitante: String = "",
    val notasJogadores: Map<String, Double> = emptyMap()
)

@Keep
data class LinhaTabela(
    val equipeId: Int = 0,
    val nome: String = "",
    val pontos: Int = 0,
    val jogos: Int = 0,
    val vitorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val gm: Int = 0,
    val gs: Int = 0,
    val sg: Int = 0,
    val amarelos: Int = 0,
    val vermelhos: Int = 0
)

@Keep
data class ConfiguracoesCampeonato(
    val modoReturno: Boolean = false, 
    val modoIdaEVoltaMataMata: Boolean = true,
    val modoIdaEVoltaFinal: Boolean = false,
    val criteriosDesempate: List<String> = listOf("Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar", "Selecionar"),
    val exibirCartoesNaTabela: Boolean = false
)

@Keep
data class ConfigGrupo(
    val nome: String = "",
    val qtdTimes: Int = 0,
    val qtdClassificados: Int = 0
)

@Keep
@Entity(tableName = "campeonatos")
data class CampeonatoSalvo(
    @PrimaryKey val id: Int = 0,
    val nomeExibicao: String = "",
    val nome: String = "", 
    val ownerId: String = "",
    val modelo: String = "",
    val equipes: List<EquipeExemplo> = emptyList(),
    val partidas: List<Partida> = emptyList(),
    val configs: ConfiguracoesCampeonato = ConfiguracoesCampeonato(),
    val gruposConfig: List<ConfigGrupo> = emptyList(),
    val fotoUri: String = ""
)
