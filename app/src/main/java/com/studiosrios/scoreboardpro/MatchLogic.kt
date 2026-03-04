package com.studiosrios.scoreboardpro

/**
 * MatchLogic: O arquivo que processa os números para que as telas
 * fiquem focadas apenas em mostrar os dados.
 */

// 1. ORDENAÇÃO DE PARTIDAS
fun obterPartidasOrdenadas(partidas: List<Partida>): List<Partida> {
    return partidas.sortedWith(
        compareBy(
            // Converte "DD/MM" para "MMDD" para uma ordenação correta por data
            { it.data.split("/").reversed().joinToString("") },
            { it.horario }
        )
    )
}

// 2. CÁLCULO DA CLASSIFICAÇÃO
fun calcularClassificacao(equipes: List<EquipeExemplo>, partidas: List<Partida>): List<LinhaTabela> {
    return equipes.map { equipe ->
        var p = 0; var j = 0; var v = 0; var e = 0; var d = 0; var gm = 0; var gs = 0
        var ca = 0; var cv = 0

        // Filtra partidas finalizadas onde a equipe participou
        partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }
            .forEach { part ->
                j++
                val gM = part.golsMandante ?: 0
                val gV = part.golsVisitante ?: 0

                if (part.mandanteId == equipe.id) {
                    gm += gM; gs += gV
                    ca += part.cartoesAmarelosMandante
                    cv += part.cartoesVermelhosMandante
                    
                    when {
                        gM > gV -> { p += 3; v++ }
                        gM == gV -> { p += 1; e++ }
                        else -> d++
                    }
                } else {
                    gm += gV; gs += gM
                    ca += part.cartoesAmarelosVisitante
                    cv += part.cartoesVermelhosVisitante

                    when {
                        gV > gM -> { p += 3; v++ }
                        gV == gM -> { p += 1; e++ }
                        else -> d++
                    }
                }
            }

        LinhaTabela(
            equipeId = equipe.id,
            nome = equipe.nome,
            pontos = p,
            jogos = j,
            vitorias = v,
            empates = e,
            derrotas = d,
            gm = gm,
            gs = gs,
            sg = gm - gs,
            amarelos = ca,
            vermelhos = cv
        )
    }.sortedWith(
        // Critérios de desempate padrão: Pontos > Vitórias > Saldo de Gols
        compareByDescending<LinhaTabela> { it.pontos }
            .thenByDescending { it.vitorias }
            .thenByDescending { it.sg }
    )
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
