package com.studiosrios.scoreboardpro

import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * MatchLogic: Processamento de regras de negócio, classificação e pontuação.
 */

data class PontuacaoDetalhada(
    val total: Double = 0.0,
    val gols: Int = 0,
    val assistencias: Int = 0,
    val sg: Int = 0,
    val amarelos: Int = 0,
    val vermelhos: Int = 0,
    val golsContra: Int = 0,
    val mvp: Int = 0
)

fun calcularPontuacaoJogador(jogador: JogadorExemplo, partidas: List<Partida>): PontuacaoDetalhada {
    var ptsGols = 0; var ptsAssists = 0; var ptsSG = 0
    var ptsAmarelos = 0; var ptsVermelhos = 0; var ptsContra = 0; var ptsMVP = 0
    
    var countGols = 0; var countAssists = 0; var countSG = 0
    var countAmarelos = 0; var countVermelhos = 0; var countContra = 0; var countMVP = 0

    partidas.filter { it.finalizada }.forEach { p ->
        // 1. Eventos individuais
        p.eventos.filter { it.jogadorNome == jogador.nome }.forEach { ev ->
            when (ev.tipo) {
                "GOL", "GOL (PÊNALTI)" -> countGols++
                "ASSISTÊNCIA" -> countAssists++
                "YELLOW CARD" -> countAmarelos++
                "RED CARD" -> countVermelhos++
                "GOL CONTRA" -> countContra++
            }
        }

        // 2. MVP (Melhor da Partida)
        if (p.melhorJogador == jogador.nome) {
            countMVP++
        }

        // 3. Defesa sem sofrer gol (SG)
        // Regra: Jogador deve ser de defesa (GOL, ZAG, LAT, ALA, VOL) e o time não pode ter sofrido gols
        val posBase = jogador.posicao.take(3).uppercase()
        val ehDefensor = posBase in listOf("GOL", "ZAG", "LAT", "ALA", "VOL")
        
        if (ehDefensor) {
            val foiTitular = p.titularesMandante.contains(jogador.id) || p.titularesVisitante.contains(jogador.id)
            val entrouNoJogo = p.eventos.any { it.tipo.contains("SUB") && it.tipo.contains(jogador.nome) && it.tipo.contains("Entra") }
            
            if (foiTitular || entrouNoJogo) {
                val golsSofridos = if (p.mandanteId == jogador.equipeId) (p.golsVisitante ?: 0) else (p.golsMandante ?: 0)
                if (golsSofridos == 0) {
                    countSG++
                }
            }
        }
    }

    val total = (countGols * 5.0) + (countAssists * 2.5) + (countSG * 3.0) + 
                (countAmarelos * -0.5) + (countContra * -5.0) + (countVermelhos * -3.0) + (countMVP * 5.0)

    return PontuacaoDetalhada(
        total = total,
        gols = countGols,
        assistencias = countAssists,
        sg = countSG,
        amarelos = countAmarelos,
        vermelhos = countVermelhos,
        golsContra = countContra,
        mvp = countMVP
    )
}

fun obterPartidasOrdenadas(partidas: List<Partida>): List<Partida> {
    return partidas.sortedWith(
        compareBy<Partida>(
            { !it.fase.contains("Rodada", ignoreCase = true) },
            { it.data.split("/").reversed().joinToString("") },
            { it.horario }
        )
    )
}

fun calcularClassificacao(equipes: List<EquipeExemplo>, partidas: List<Partida>, configs: ConfiguracoesCampeonato): List<LinhaTabela> {
    return BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
}

fun verificarFaseGruposFinalizada(partidas: List<Partida>): Boolean {
    val partidasGrupos = partidas.filter { 
        it.fase.contains("Rodada", ignoreCase = true) || it.fase.contains("ÚNICA", ignoreCase = true)
    }
    if (partidasGrupos.isEmpty()) return false
    return partidasGrupos.all { it.finalizada }
}

fun promoverClassificadosMataMata(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    configsGrupos: List<ConfigGrupo>,
    configs: ConfiguracoesCampeonato
) {
    val mapaVagas = mutableMapOf<String, Int>()
    var indiceInicio = 0

    // 1. Promoção dos Grupos para a Primeira Fase Eliminatória
    configsGrupos.forEach { grupo ->
        val fim = (indiceInicio + grupo.qtdTimes).coerceAtMost(equipes.size)
        val equipesDoGrupo = equipes.subList(indiceInicio, fim)
        val idsEquipes = equipesDoGrupo.map { it.id }
        val partidasDoGrupo = partidas.filter { it.mandanteId in idsEquipes && it.visitanteId in idsEquipes }
        
        val ranking = calcularClassificacao(equipesDoGrupo, partidasDoGrupo, configs)
        
        ranking.forEachIndexed { index, linha ->
            mapaVagas["${index + 1}º ${grupo.nome}"] = linha.equipeId
        }
        indiceInicio += grupo.qtdTimes
    }

    // 2. Promoção dos Vencedores (Considerando Agregado e Pênaltis)
    val confrontos = partidas.filter { it.finalizada && it.nomeConfronto.isNotBlank() }.groupBy { it.nomeConfronto }

    confrontos.forEach { (nome, jogos) ->
        val jogoIda = jogos.first()
        val jogoVolta = if (jogos.size > 1) jogos.last() else null
        
        val idA = jogoIda.mandanteId
        val idB = jogoIda.visitanteId
        
        val golsA = (jogoIda.golsMandante ?: 0) + (jogoVolta?.golsVisitante ?: 0)
        val golsB = (jogoIda.golsVisitante ?: 0) + (jogoVolta?.golsMandante ?: 0)
        
        val vencedorId = when {
            golsA > golsB -> idA
            golsB > golsA -> idB
            else -> {
                // No jogo de volta, idA é visitante. Então penA é penaltisVisitante do jogo de volta.
                val pA = if (jogoVolta != null) (jogoVolta.penaltisVisitante ?: 0) else (jogoIda.penaltisMandante ?: 0)
                val pB = if (jogoVolta != null) (jogoVolta.penaltisMandante ?: 0) else (jogoIda.penaltisVisitante ?: 0)
                
                if (pA >= pB) idA else idB
            }
        }
        mapaVagas["Vence $nome"] = vencedorId
    }

    // 3. Atualiza os IDs nas partidas futuras
    for (i in partidas.indices) {
        val p = partidas[i]
        val novoM = mapaVagas[p.labelMandante] ?: p.mandanteId
        val novoV = mapaVagas[p.labelVisitante] ?: p.visitanteId
        if (novoM != p.mandanteId || novoV != p.visitanteId) {
            partidas[i] = p.copy(mandanteId = novoM, visitanteId = novoV)
        }
    }
}

fun calcularTotalJogos(vagas: Int): Int {
    var total = 0
    var atual = vagas
    while (atual > 1) {
        atual /= 2
        total += atual
    }
    return total
}

fun obterNomeFaseEConfronto(index: Int, vagas: Int): Pair<String, String> {
    var count = 0
    var fV = vagas
    val nomesFasesShort = listOf("PF", "Oitavas", "QF", "Semi", "Final")
    
    // Determina o índice inicial do nome da fase baseado no total de vagas
    var fI = if (vagas == 32) 0 else if (vagas == 16) 1 else if (vagas == 8) 2 else if (vagas == 4) 3 else 4
    
    while (fV > 1) {
        val jogosNaFase = fV / 2
        if (index < count + jogosNaFase) {
            val num = index - count + 1
            val nomeFaseFull = when(fV) {
                32 -> "PRIMEIRA FASE"
                16 -> "OITAVAS DE FINAL"
                8 -> "QUARTAS DE FINAL"
                4 -> "SEMIFINAIS"
                else -> "GRANDE FINAL"
            }
            return Pair(nomeFaseFull, "${nomesFasesShort[fI]} $num")
        }
        count += jogosNaFase
        fV /= 2
        fI++
    }
    return Pair("FINAL", "Final")
}
