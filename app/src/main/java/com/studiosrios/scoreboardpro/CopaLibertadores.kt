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

        // 1. GERAÇÃO DA FASE DE GRUPOS
        configsGrupos.forEach { config ->
            val timesDesteGrupo = mutableListOf<Int>()
            for (i in 0 until config.qtdTimes) {
                if (copiaEquipes.isNotEmpty()) {
                    timesDesteGrupo.add(copiaEquipes.removeAt(0).id)
                }
            }

            for (i in timesDesteGrupo.indices) {
                for (j in i + 1 until timesDesteGrupo.size) {
                    // Jogo de Ida
                    partidas.add(Partida(id = idContador++, mandanteId = timesDesteGrupo[i], visitanteId = timesDesteGrupo[j]))
                    // Jogo de Volta (Fase de Grupos)
                    if (turnoEReturno) {
                        partidas.add(Partida(id = idContador++, mandanteId = timesDesteGrupo[j], visitanteId = timesDesteGrupo[i]))
                    }
                }
            }
        }

        // 2. GERAÇÃO DOS CONFRONTOS DE MATA-MATA (CHAVEAMENTO)
        confrontosMataMata.forEach { confronto ->
            // Para o mata-mata, os times são inicialmente indefinidos (usamos -1 ou lógica de TBD no UI)
            // Aqui geramos os slots que aparecerão no painel
            partidas.add(Partida(id = idContador++, mandanteId = -1, visitanteId = -1, local = "A DEFINIR (IDA)"))
            
            if (idaEVoltaMataMata) {
                partidas.add(Partida(id = idContador++, mandanteId = -1, visitanteId = -1, local = "A DEFINIR (VOLTA)"))
            }
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
