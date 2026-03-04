package com.studiosrios.scoreboardpro

class CopaLibertadores : FormatoCampeonato {
    override val nomeTipo: String = "Estilo Libertadores (Grupos)"

    override fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo>,
        confrontosMataMata: List<Pair<String, String>>,
        idaEVoltaMataMata: Boolean
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

            // Gerador de rodadas simples (Todos contra todos no grupo)
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

        // 2. GERAÇÃO DOS CONFRONTOS DE MATA-MATA
        // Aqui usamos os nomes das fases para o filtro da PartidasTab funcionar
        var indexConfronto = 0
        val totalVagas = configsGrupos.sumOf { it.qtdClassificados }
        
        confrontosMataMata.forEach { _ ->
            val faseInfo = obterNomeFaseEConfronto(indexConfronto, totalVagas)
            
            partidas.add(Partida(
                id = idContador++, 
                mandanteId = -1, 
                visitanteId = -1, 
                fase = faseInfo.first,
                local = "A DEFINIR (IDA)"
            ))
            
            if (idaEVoltaMataMata) {
                partidas.add(Partida(
                    id = idContador++, 
                    mandanteId = -1, 
                    visitanteId = -1, 
                    fase = faseInfo.first,
                    local = "A DEFINIR (VOLTA)"
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
