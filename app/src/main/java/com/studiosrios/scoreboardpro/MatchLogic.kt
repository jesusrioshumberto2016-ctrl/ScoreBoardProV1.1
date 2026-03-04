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

// 4. PROMOÇÃO DE EQUIPES PARA O MATA-MATA
fun promoverClassificadosMataMata(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    configsGrupos: List<ConfigGrupo>,
    configs: ConfiguracoesCampeonato
) {
    val mapaVagas = mutableMapOf<String, Int>()
    var indiceInicio = 0

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
    
    while (faseVagas > 1) {
        val jogosNaFase = faseVagas / 2
        if (index < count + jogosNaFase) {
            val numNoFase = index - count + 1
            return when (faseVagas) {
                32 -> Pair("PRIMEIRA FASE", "PF $numNoFase")
                16 -> Pair("OITAVAS DE FINAL", "Oitavas $numNoFase")
                8 -> Pair("QUARTAS DE FINAL", "QF $numNoFase")
                4 -> Pair("SEMIFINAIS", "Semi $numNoFase")
                2 -> Pair("GRANDE FINAL", "Grande Final")
                else -> Pair("FASE ELIMINATÓRIA", "Jogo $numNoFase")
            }
        }
        count += jogosNaFase
        faseVagas /= 2
    }
    return Pair("FINAL", "Grande Final")
}
