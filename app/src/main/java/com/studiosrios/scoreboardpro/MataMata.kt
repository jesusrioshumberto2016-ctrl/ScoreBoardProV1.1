package com.studiosrios.scoreboardpro

class MataMata : FormatoCampeonato {

    override val nomeTipo: String = "Mata-Mata (Eliminatórias)"

    override fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo>,
        confrontosMataMata: List<Pair<String, String>>,
        idaEVoltaMataMata: Boolean
    ): List<Partida> {
        val partidas = mutableListOf<Partida>()
        var idContador = 1
        val sorteadas = equipes.shuffled()

        for (i in sorteadas.indices step 2) {
            if (i + 1 < sorteadas.size) {
                partidas.add(
                    Partida(
                        id = idContador++,
                        mandanteId = sorteadas[i].id,
                        visitanteId = sorteadas[i + 1].id
                    )
                )
                if (turnoEReturno) {
                    partidas.add(
                        Partida(
                            id = idContador++,
                            mandanteId = sorteadas[i + 1].id,
                            visitanteId = sorteadas[i].id
                        )
                    )
                }
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
