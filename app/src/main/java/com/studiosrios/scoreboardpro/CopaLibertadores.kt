package com.studiosrios.scoreboardpro

class CopaLibertadores : FormatoCampeonato {
    override val nomeTipo: String = "Estilo Libertadores (Grupos)"

    override fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo>
    ): List<Partida> {
        val partidas = mutableListOf<Partida>()
        var idContador = 1
        val timesParaDistribuir = equipes.toMutableList()

        // 1. GERAÇÃO DA FASE DE GRUPOS
        configsGrupos.forEach { config ->
            val timesDesteGrupo = mutableListOf<Int>()

            for (i in 0 until config.qtdTimes) {
                if (timesParaDistribuir.isNotEmpty()) {
                    timesDesteGrupo.add(timesParaDistribuir.removeAt(0).id)
                }
            }

            for (i in timesDesteGrupo.indices) {
                for (j in i + 1 until timesDesteGrupo.size) {
                    partidas.add(
                        Partida(id = idContador++, mandanteId = timesDesteGrupo[i], visitanteId = timesDesteGrupo[j])
                    )
                    if (turnoEReturno) {
                        partidas.add(
                            Partida(id = idContador++, mandanteId = timesDesteGrupo[j], visitanteId = timesDesteGrupo[i])
                        )
                    }
                }
            }
        }

        // 2. GERAÇÃO DOS SLOTS DO MATA-MATA (TBD)
        // O mata-mata na Libertadores começa após os grupos.
        // Vamos gerar os confrontos de "Ida e Volta" se a config de mata-mata estiver ativa.
        // Nota: Como os times ainda não foram classificados, geramos partidas com IDs negativos ou marcadores TBD.
        // No entanto, para o ScoreBoard funcionar, as partidas de mata-mata precisam ser criadas aqui.
        
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
