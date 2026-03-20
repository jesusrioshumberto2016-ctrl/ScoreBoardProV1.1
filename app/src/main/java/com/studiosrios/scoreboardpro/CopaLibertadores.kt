package com.studiosrios.scoreboardpro

class CopaLibertadores : FormatoCampeonato {
    override val nomeTipo: String = "Estilo Libertadores (Grupos)"

    override fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo>,
        confrontosMataMata: List<Pair<String, String>>,
        idaEVoltaMataMata: Boolean,
        idaEVoltaFinal: Boolean
    ): List<Partida> {
        val partidas = mutableListOf<Partida>()
        var idContador = 1
        val copiaEquipes = equipes.toMutableList()

        // 1. GERAÇÃO DA FASE DE GRUPOS (Organizada por Rodadas usando Round Robin)
        configsGrupos.forEach { config ->
            val timesDesteGrupo = mutableListOf<EquipeExemplo>()
            for (i in 0 until config.qtdTimes) {
                if (copiaEquipes.isNotEmpty()) {
                    timesDesteGrupo.add(copiaEquipes.removeAt(0))
                }
            }

            if (timesDesteGrupo.size >= 2) {
                val numEquipes = timesDesteGrupo.size
                val numRodadasTurno = if (numEquipes % 2 == 0) numEquipes - 1 else numEquipes
                val listaRodadas = timesDesteGrupo.toMutableList()
                
                if (numEquipes % 2 != 0) {
                    listaRodadas.add(EquipeExemplo(-1, "BYE", "BYE", "BYE"))
                }
                
                val totalEquipes = listaRodadas.size

                for (rodada in 1..numRodadasTurno) {
                    for (jogo in 0 until totalEquipes / 2) {
                        val mandante = listaRodadas[jogo]
                        val visitante = listaRodadas[totalEquipes - 1 - jogo]

                        if (mandante.id != -1 && visitante.id != -1) {
                            partidas.add(Partida(
                                id = idContador++,
                                mandanteId = mandante.id,
                                visitanteId = visitante.id,
                                fase = "Rodada $rodada"
                            ))
                        }
                    }
                    // Rotação
                    val ultima = listaRodadas.removeAt(listaRodadas.size - 1)
                    listaRodadas.add(1, ultima)
                }

                if (turnoEReturno) {
                    val numRodadasTotal = numRodadasTurno * 2
                    val jogosTurno = partidas.filter { p -> 
                        timesDesteGrupo.any { it.id == p.mandanteId } && 
                        timesDesteGrupo.any { it.id == p.visitanteId } 
                    }.toList()

                    jogosTurno.forEach { p ->
                        val rodadaOriginal = p.fase.replace("Rodada ", "").toInt()
                        val rodadaReturno = rodadaOriginal + numRodadasTurno
                        partidas.add(Partida(
                            id = idContador++,
                            mandanteId = p.visitanteId,
                            visitanteId = p.mandanteId,
                            fase = "Rodada $rodadaReturno"
                        ))
                    }
                }
            }
        }

        // 2. GERAÇÃO DOS CONFRONTOS DE MATA-MATA
        var indexConfronto = 0
        val totalVagas = configsGrupos.sumOf { it.qtdClassificados }
        
        confrontosMataMata.forEach { par ->
            val faseInfo = obterNomeFaseEConfronto(indexConfronto, totalVagas)
            val ehFinal = faseInfo.first.contains("FINAL", ignoreCase = true) && !faseInfo.first.contains("OITAVAS", ignoreCase = true) && !faseInfo.first.contains("QUARTAS", ignoreCase = true) && !faseInfo.first.contains("SEMI", ignoreCase = true)
            
            val deveTerVolta = if (ehFinal) idaEVoltaFinal else idaEVoltaMataMata

            // Jogo de Ida
            partidas.add(Partida(
                id = idContador++, 
                mandanteId = -1, 
                visitanteId = -1, 
                fase = faseInfo.first,
                nomeConfronto = faseInfo.second,
                labelMandante = par.first, 
                labelVisitante = par.second, 
                local = if (ehFinal) "A DEFINIR (FINAL)" else "A DEFINIR (IDA)"
            ))
            
            // Jogo de Volta
            if (deveTerVolta) {
                partidas.add(Partida(
                    id = idContador++, 
                    mandanteId = -1, 
                    visitanteId = -1, 
                    fase = faseInfo.first,
                    nomeConfronto = faseInfo.second,
                    labelMandante = par.second, 
                    labelVisitante = par.first,
                    local = if (ehFinal) "A DEFINIR (FINAL VOLTA)" else "A DEFINIR (VOLTA)"
                ))
            }
            indexConfronto++
        }
        
        return partidas
    }

    override fun calcularRanking(
        equipes: List<EquipeExemplo>,
        partidas: List<Partida>,
        configs: ConfiguracoesCampeonato
    ): List<LinhaTabela> {
        return BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
    }
}
