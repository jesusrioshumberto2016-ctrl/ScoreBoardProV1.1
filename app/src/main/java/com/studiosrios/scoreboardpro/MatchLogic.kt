package com.studiosrios.scoreboardpro

import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * MatchLogic: O arquivo que processa os números para que as telas
 * fiquem focadas apenas em mostrar os dados.
 */

// 1. ORDENAÇÃO DE PARTIDAS (Melhorada para considerar rodadas e mata-mata)
fun obterPartidasOrdenadas(partidas: List<Partida>): List<Partida> {
    return partidas.sortedWith(
        compareBy<Partida>(
            // Primeiro separa fase de grupos do mata-mata (simplificado)
            { !it.fase.contains("Rodada", ignoreCase = true) },
            // Depois ordena pela string da data convertida para formato comparável (YYYYMMDD ou MMDD)
            { it.data.split("/").reversed().joinToString("") },
            { it.horario }
        )
    )
}

// 2. CÁLCULO DA CLASSIFICAÇÃO
fun calcularClassificacao(equipes: List<EquipeExemplo>, partidas: List<Partida>, configs: ConfiguracoesCampeonato): List<LinhaTabela> {
    return BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
}

// 3. VERIFICAÇÃO DE CONCLUSÃO DA FASE DE GRUPOS
fun verificarFaseGruposFinalizada(partidas: List<Partida>): Boolean {
    val partidasGrupos = partidas.filter { 
        it.fase.contains("Rodada", ignoreCase = true) || it.fase.contains("Única", ignoreCase = true)
    }
    if (partidasGrupos.isEmpty()) return false
    return partidasGrupos.all { it.finalizada }
}

// 4. PROMOÇÃO DE EQUIPES PARA O MATA-MATA (Incluindo promoção de vencedores)
fun promoverClassificadosMataMata(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    configsGrupos: List<ConfigGrupo>,
    configs: ConfiguracoesCampeonato
) {
    val mapaVagas = mutableMapOf<String, Int>()
    var indiceInicio = 0

    // --- PARTE A: PROMOÇÃO DOS GRUPOS ---
    configsGrupos.forEach { grupo ->
        val fim = (indiceInicio + grupo.qtdTimes).coerceAtMost(equipes.size)
        val equipesDoGrupo = equipes.subList(indiceInicio, fim)
        val idsEquipes = equipesDoGrupo.map { it.id }
        val partidasDoGrupo = partidas.filter { it.mandanteId in idsEquipes && it.visitanteId in idsEquipes }
        
        val ranking = calcularClassificacao(equipesDoGrupo, partidasDoGrupo, configs)
        
        ranking.forEachIndexed { index, linha ->
            val posicao = index + 1
            mapaVagas["${posicao}º ${grupo.nome}"] = linha.equipeId
        }
        indiceInicio += grupo.qtdTimes
    }

    // --- PARTE B: PROMOÇÃO DOS VENCEDORES DE CONFRONTOS ---
    // Agrupa partidas por confronto para calcular o agregado (Ida e Volta)
    val confrontosFinalizados = partidas
        .filter { it.finalizada && it.nomeConfronto.isNotBlank() }
        .groupBy { it.nomeConfronto }

    confrontosFinalizados.forEach { (nome, jogos) ->
        val mandantePrimeiroJogo = jogos.first().mandanteId
        val visitantePrimeiroJogo = jogos.first().visitanteId
        
        var saldoMandante = 0
        var saldoVisitante = 0
        
        jogos.forEach { j ->
            val gM = j.golsMandante ?: 0
            val gV = j.golsVisitante ?: 0
            if (j.mandanteId == mandantePrimeiroJogo) {
                saldoMandante += gM; saldoVisitante += gV
            } else {
                saldoMandante += gV; saldoVisitante += gM
            }
        }
        
        val vencedorId = if (saldoMandante >= saldoVisitante) mandantePrimeiroJogo else visitantePrimeiroJogo
        mapaVagas["Vence $nome"] = vencedorId
    }

    // --- PARTE C: ATUALIZAÇÃO FINAL ---
    for (i in partidas.indices) {
        val p = partidas[i]
        val novoMandanteId = mapaVagas[p.labelMandante] ?: p.mandanteId
        val novoVisitanteId = mapaVagas[p.labelVisitante] ?: p.visitanteId
        
        if (novoMandanteId != p.mandanteId || novoVisitanteId != p.visitanteId) {
            partidas[i] = p.copy(
                mandanteId = novoMandanteId,
                visitanteId = novoVisitanteId
            )
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
    var faseVagas = vagas
    val nomesFases = listOf("PF", "Oitavas", "QF", "Semi", "Final")
    var faseIndex = if (vagas == 32) 0 else if (vagas == 16) 1 else if (vagas == 8) 2 else if (vagas == 4) 3 else 4
    
    var fV = faseVagas
    var fI = faseIndex
    while (fV > 1) {
        val jogosNaFase = fV / 2
        if (index < count + jogosNaFase) {
            val numNoFase = index - count + 1
            val nomeFaseFull = when(fV) {
                32 -> "PRIMEIRA FASE"
                16 -> "OITAVAS DE FINAL"
                8 -> "QUARTAS DE FINAL"
                4 -> "SEMIFINAIS"
                else -> "GRANDE FINAL"
            }
            return Pair(nomeFaseFull, "${nomesFases[fI]} $numNoFase")
        }
        count += jogosNaFase
        fV /= 2
        fI++
    }
    return Pair("FINAL", "Final")
}
