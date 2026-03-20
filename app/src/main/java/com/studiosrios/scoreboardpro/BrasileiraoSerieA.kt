package com.studiosrios.scoreboardpro

class BrasileiraoSerieA : FormatoCampeonato {
    override val nomeTipo: String = "Brasileirão (Pontos Corridos)"

    override fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo>,
        confrontosMataMata: List<Pair<String, String>>,
        idaEVoltaMataMata: Boolean,
        idaEVoltaFinal: Boolean
    ): List<Partida> {
        val partidasGeradas = mutableListOf<Partida>()
        var idContador = 1
        
        if (equipes.size < 2) return emptyList()

        val numEquipes = equipes.size
        val numRodadas = if (numEquipes % 2 == 0) numEquipes - 1 else numEquipes
        val jogosPorRodada = numEquipes / 2

        val listaEquipes = equipes.toMutableList()
        if (numEquipes % 2 != 0) {
            listaEquipes.add(EquipeExemplo(-1, "BYE", "BYE", "BYE")) // Equipe fantasma para número ímpar
        }

        val totalEquipes = listaEquipes.size

        for (rodada in 1..numRodadas) {
            for (jogo in 0 until totalEquipes / 2) {
                val mandante = listaEquipes[jogo]
                val visitante = listaEquipes[totalEquipes - 1 - jogo]

                if (mandante.id != -1 && visitante.id != -1) {
                    partidasGeradas.add(
                        Partida(
                            id = idContador++,
                            mandanteId = mandante.id,
                            visitanteId = visitante.id,
                            fase = "Rodada $rodada"
                        )
                    )
                }
            }
            // Rotacionar equipes (mantém a primeira fixa)
            val ultima = listaEquipes.removeAt(listaEquipes.size - 1)
            listaEquipes.add(1, ultima)
        }

        if (turnoEReturno) {
            val returno = partidasGeradas.toList().mapIndexed { index, p ->
                val rodadaReturno = numRodadas + (index / (numEquipes / 2)) + 1
                Partida(
                    id = idContador++, 
                    mandanteId = p.visitanteId, 
                    visitanteId = p.mandanteId, 
                    fase = "Rodada $rodadaReturno"
                )
            }
            partidasGeradas.addAll(returno)
        }

        return partidasGeradas
    }

    override fun calcularRanking(equipes: List<EquipeExemplo>, partidas: List<Partida>, configs: ConfiguracoesCampeonato): List<LinhaTabela> {
        val tabela = equipes.map { equipe ->
            var pts = 0; var v = 0; var e = 0; var d = 0; var gm = 0; var gs = 0; var am = 0; var vm = 0

            partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }.forEach { p ->
                val gM = p.golsMandante ?: 0
                val gV = p.golsVisitante ?: 0

                if (p.mandanteId == equipe.id) {
                    gm += gM; gs += gV
                    am += p.cartoesAmarelosMandante; vm += p.cartoesVermelhosMandante
                    if (gM > gV) { pts += 3; v++ } else if (gM == gV) { pts += 1; e++ } else d++
                } else {
                    gm += gV; gs += gM
                    am += p.cartoesAmarelosVisitante; vm += p.cartoesVermelhosVisitante
                    if (gV > gM) { pts += 3; v++ } else if (gV == gM) { pts += 1; e++ } else d++
                }
            }
            LinhaTabela(equipe.id, equipe.nome, pts, v+e+d, v, e, d, gm, gs, gm-gs, am, vm)
        }

        // --- APLICAÇÃO DOS CRITÉRIOS DE DESEMPATE ---
        var comparador = compareByDescending<LinhaTabela> { it.pontos }

        configs.criteriosDesempate.forEach { criterio ->
            comparador = when (criterio) {
                "Vitórias" -> comparador.thenByDescending { it.vitorias }
                "Saldo de Gols" -> comparador.thenByDescending { it.sg }
                "Gols Marcados" -> comparador.thenByDescending { it.gm }
                "Cartões Amarelos" -> comparador.thenBy { it.amarelos }
                "Cartões Vermelhos" -> comparador.thenBy { it.vermelhos }
                "Confronto Direto" -> {
                    comparador.thenComparator { a, b ->
                        val jogosEntre = partidas.filter { 
                            it.finalizada && 
                            ((it.mandanteId == a.equipeId && it.visitanteId == b.equipeId) || (it.mandanteId == b.equipeId && it.visitanteId == a.equipeId))
                        }
                        var ptsA = 0
                        var ptsB = 0
                        jogosEntre.forEach { j ->
                            val gM = j.golsMandante ?: 0
                            val gV = j.golsVisitante ?: 0
                            if (j.mandanteId == a.equipeId) {
                                if (gM > gV) ptsA += 3 else if (gM == gV) ptsA += 1 else ptsB += 3
                            } else {
                                if (gV > gM) ptsA += 3 else if (gV == gM) ptsA += 1 else ptsB += 3
                            }
                        }
                        ptsB.compareTo(ptsA) // Descending
                    }
                }
                else -> comparador
            }
        }
        return tabela.sortedWith(comparador)
    }
}
