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

        // 1. GERAÇÃO DA FASE DE GRUPOS (Organizada por Rodadas)
        configsGrupos.forEach { config ->
            val timesDesteGrupo = mutableListOf<Int>()
            for (i in 0 until config.qtdTimes) {
                if (copiaEquipes.isNotEmpty()) {
                    timesDesteGrupo.add(copiaEquipes.removeAt(0).id)
                }
            }

            for (i in timesDesteGrupo.indices) {
                for (j in i + 1 until timesDesteGrupo.size) {
                    val rodadaIda = if (turnoEReturno) "${i + j}ª Rodada" else "Rodada Única"
                    partidas.add(Partida(
                        id = idContador++, 
                        mandanteId = timesDesteGrupo[i], 
                        visitanteId = timesDesteGrupo[j],
                        fase = rodadaIda
                    ))
                    
                    if (turnoEReturno) {
                        partidas.add(Partida(
                            id = idContador++, 
                            mandanteId = timesDesteGrupo[j], 
                            visitanteId = timesDesteGrupo[i],
                            fase = "${i + j + 1}ª Rodada"
                        ))
                    }
                }
            }
        }

        // 2. GERAÇÃO DOS CONFRONTOS DE MATA-MATA (Conforme definição do usuário)
        var indexConfronto = 0
        val totalVagas = configsGrupos.sumOf { it.qtdClassificados }
        
        confrontosMataMata.forEach { par ->
            val faseInfo = obterNomeFaseEConfronto(indexConfronto, totalVagas)
            val ehFinal = faseInfo.first.contains("FINAL", ignoreCase = true) && !faseInfo.first.contains("OITAVAS", ignoreCase = true) && !faseInfo.first.contains("QUARTAS", ignoreCase = true) && !faseInfo.first.contains("SEMI", ignoreCase = true)
            
            // Determina se deve ter ida e volta para este confronto específico
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
