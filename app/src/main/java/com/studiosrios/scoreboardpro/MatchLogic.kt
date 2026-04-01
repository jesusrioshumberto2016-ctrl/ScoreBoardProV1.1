package com.studiosrios.scoreboardpro

import java.text.SimpleDateFormat
import java.util.Locale

fun obterFormato(modelo: String): FormatoCampeonato {
    return when {
        modelo.contains("Brasileirão", ignoreCase = true) || modelo.contains("Pontos Corridos", ignoreCase = true) -> BrasileiraoSerieA()
        modelo.contains("Libertadores", ignoreCase = true) -> CopaLibertadores()
        modelo.contains("Mata-Mata", ignoreCase = true) || modelo.contains("Copa", ignoreCase = true) -> MataMata()
        else -> BrasileiraoSerieA() // Padrão
    }
}

fun obterNomeFaseEConfronto(indexConfronto: Int, totalVagas: Int): Pair<String, String> {
    val totalConfrontosFase = totalVagas / 2
    
    return when {
        totalVagas <= 2 -> Pair("FINAL", "Final Única")
        totalVagas <= 4 -> {
            val num = (indexConfronto % 2) + 1
            Pair("SEMIFINAL", "Semi $num")
        }
        totalVagas <= 8 -> {
            val num = (indexConfronto % 4) + 1
            Pair("QUARTAS DE FINAL", "Quartas $num")
        }
        totalVagas <= 16 -> {
            val num = (indexConfronto % 8) + 1
            Pair("OITAVAS DE FINAL", "Oitavas $num")
        }
        else -> {
            val num = indexConfronto + 1
            Pair("ELIMINATÓRIA", "Jogo $num")
        }
    }
}

data class PontuacaoJogadorResult(
    val total: Double = 0.0,
    val gols: Int = 0,
    val assistencias: Int = 0,
    val sg: Int = 0,
    val mvp: Int = 0,
    val amarelos: Int = 0,
    val vermelhos: Int = 0,
    val golsContra: Int = 0
)

fun calcularPontuacaoJogador(
    jogador: JogadorExemplo,
    partidas: List<Partida>,
    equipes: List<EquipeExemplo>
): PontuacaoJogadorResult {
    var g = 0; var a = 0; var sg = 0; var mvp = 0; var am = 0; var vm = 0; var gc = 0

    val partidasDoJogador = partidas.filter { p ->
        p.finalizada && (p.mandanteId == jogador.equipeId || p.visitanteId == jogador.equipeId)
    }

    partidasDoJogador.forEach { p ->
        // Gols, Assistências e Cartões via Eventos
        p.eventos.filter { it.jogadorNome == jogador.nome || (jogador.apelido.isNotBlank() && it.jogadorNome == jogador.apelido) }.forEach { ev ->
            when (ev.tipo) {
                "GOL", "GOL (PÊNALTI)" -> g++
                "ASSISTÊNCIA" -> a++
                "YELLOW CARD" -> am++
                "RED CARD" -> vm++
                "GOL CONTRA" -> gc++
            }
        }

        // Melhor da Partida
        if (p.melhorJogador == jogador.nome || (jogador.apelido.isNotBlank() && p.melhorJogador == jogador.apelido)) {
            mvp++
        }

        // Saldo de Gols (SG) para Defensores e Goleiros (Volante removido conforme solicitado)
        val ehDefensor = jogador.posicao in listOf("GOL", "ZAG", "LAT")
        if (ehDefensor) {
            val sofridos = if (p.mandanteId == jogador.equipeId) (p.golsVisitante ?: 0) else (p.golsMandante ?: 0)
            if (sofridos == 0) sg++
        }
    }

    val total = (g * 5.0) + (a * 2.5) + (sg * 3.0) + (mvp * 5.0) + (am * -0.5) + (vm * -3.0) + (gc * -5.0)

    return PontuacaoJogadorResult(
        total = total,
        gols = g,
        assistencias = a,
        sg = sg,
        mvp = mvp,
        amarelos = am,
        vermelhos = vm,
        golsContra = gc
    )
}

fun obterPartidasOrdenadas(lista: List<Partida>): List<Partida> {
    val sdfData = SimpleDateFormat("dd/MM", Locale.getDefault())
    val sdfHora = SimpleDateFormat("HH:mm", Locale.getDefault())

    return lista.sortedWith(
        compareBy<Partida> { p ->
            try {
                if (p.data.isNotBlank()) sdfData.parse(p.data)?.time ?: Long.MAX_VALUE else Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
        }.thenBy { p ->
            try {
                if (p.horario.isNotBlank()) sdfHora.parse(p.horario)?.time ?: Long.MAX_VALUE else Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
        }.thenBy { it.id }
    )
}

fun calcularClassificacao(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    configs: ConfiguracoesCampeonato
): List<LinhaTabela> {
    return BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
}

fun calcularTotalJogos(totalVagas: Int): Int {
    if (totalVagas < 2) return 0
    var soma = 0
    var current = totalVagas
    while (current >= 2) {
        soma += current / 2
        current /= 2
    }
    return soma
}

fun verificarFaseGruposFinalizada(partidas: List<Partida>): Boolean {
    val matchesGrupos = partidas.filter { it.fase.contains("Rodada", ignoreCase = true) }
    if (matchesGrupos.isEmpty()) return false
    return matchesGrupos.all { it.finalizada }
}

fun promoverClassificadosMataMata(
    partidas: MutableList<Partida>,
    equipes: List<EquipeExemplo>,
    configsGrupos: List<ConfigGrupo>,
    configs: ConfiguracoesCampeonato
) {
    // 1. Calcular classificações da fase de grupos
    val classificacoesPorGrupo = mutableMapOf<String, List<LinhaTabela>>()
    var indiceInicio = 0
    configsGrupos.forEach { grupo ->
        val fim = (indiceInicio + grupo.qtdTimes).coerceAtMost(equipes.size)
        val equipesDesteGrupo = if (indiceInicio < equipes.size) equipes.subList(indiceInicio, fim) else emptyList()
        val idsEquipes = equipesDesteGrupo.map { it.id }
        val partidasDesteGrupo = partidas.filter { it.mandanteId in idsEquipes && it.visitanteId in idsEquipes && it.fase.contains("Rodada", ignoreCase = true) }
        
        val ranking = BrasileiraoSerieA().calcularRanking(equipesDesteGrupo, partidasDesteGrupo, configs)
        classificacoesPorGrupo[grupo.nome] = ranking
        indiceInicio += grupo.qtdTimes
    }

    // 2. Coletar vencedores de confrontos de mata-mata já finalizados
    val vencedoresConfrontos = mutableMapOf<String, Int>() 
    val confrontos = partidas.filter { it.nomeConfronto.isNotBlank() }.groupBy { it.nomeConfronto }
    
    confrontos.forEach { (nome, jogos) ->
        if (jogos.all { it.finalizada }) {
            val idA = jogos[0].mandanteId
            val idB = jogos[0].visitanteId
            if (idA != -1 && idB != -1) {
                var golsA = 0
                var golsB = 0
                jogos.forEach { j ->
                    if (j.mandanteId == idA) {
                        golsA += (j.golsMandante ?: 0)
                        golsB += (j.golsVisitante ?: 0)
                    } else {
                        golsB += (j.golsMandante ?: 0)
                        golsA += (j.golsVisitante ?: 0)
                    }
                }
                
                if (golsA > golsB) {
                    vencedoresConfrontos[nome] = idA
                } else if (golsB > golsA) {
                    vencedoresConfrontos[nome] = idB
                } else {
                    val ultimoJogo = jogos.last()
                    val pM = ultimoJogo.penaltisMandante ?: 0
                    val pV = ultimoJogo.penaltisVisitante ?: 0
                    if (pM > pV) {
                        vencedoresConfrontos[nome] = ultimoJogo.mandanteId
                    } else if (pV > pM) {
                        vencedoresConfrontos[nome] = ultimoJogo.visitanteId
                    }
                }
            }
        }
    }

    // 3. Atualizar placeholders (Ex: "1º Grupo A" ou "Vence Oitavas 1")
    for (i in partidas.indices) {
        val p = partidas[i]
        if (!p.fase.contains("Rodada", ignoreCase = true)) {
            var novoMandanteId = p.mandanteId
            var novoVisitanteId = p.visitanteId

            // Tentar resolver mandante
            if (novoMandanteId == -1) {
                val label = p.labelMandante
                if (label.contains("º")) {
                    val regex = """(\d+)º\s+(.+)""".toRegex()
                    regex.find(label)?.let { match ->
                        val pos = match.groupValues[1].toInt()
                        val grupoNome = match.groupValues[2]
                        classificacoesPorGrupo[grupoNome]?.let { ranking ->
                            if (ranking.size >= pos) novoMandanteId = ranking[pos - 1].equipeId
                        }
                    }
                } else if (label.startsWith("Vence ")) {
                    val nomeConf = label.removePrefix("Vence ")
                    vencedoresConfrontos[nomeConf]?.let { novoMandanteId = it }
                }
            }

            // Tentar resolver visitante
            if (novoVisitanteId == -1) {
                val label = p.labelVisitante
                if (label.contains("º")) {
                    val regex = """(\d+)º\s+(.+)""".toRegex()
                    regex.find(label)?.let { match ->
                        val pos = match.groupValues[1].toInt()
                        val grupoNome = match.groupValues[2]
                        classificacoesPorGrupo[grupoNome]?.let { ranking ->
                            if (ranking.size >= pos) novoVisitanteId = ranking[pos - 1].equipeId
                        }
                    }
                } else if (label.startsWith("Vence ")) {
                    val nomeConf = label.removePrefix("Vence ")
                    vencedoresConfrontos[nomeConf]?.let { novoVisitanteId = it }
                }
            }

            if (novoMandanteId != p.mandanteId || novoVisitanteId != p.visitanteId) {
                partidas[i] = p.copy(mandanteId = novoMandanteId, visitanteId = novoVisitanteId)
            }
        }
    }
}
