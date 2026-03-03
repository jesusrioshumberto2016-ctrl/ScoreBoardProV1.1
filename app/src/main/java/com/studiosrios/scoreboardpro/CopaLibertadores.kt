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
        // Alteração: Removido o .shuffled() para que a ordem de seleção dos times seja respeitada na formação dos grupos
        val timesParaDistribuir = equipes.toMutableList()

        // Loop por cada grupo personalizado
        configsGrupos.forEach { config ->
            val timesDesteGrupo = mutableListOf<Int>()

            // Distribui os times conforme a quantidade definida para o grupo
            for (i in 0 until config.qtdTimes) {
                if (timesParaDistribuir.isNotEmpty()) {
                    timesDesteGrupo.add(timesParaDistribuir.removeAt(0).id)
                }
            }

            // Gera os jogos (todos contra todos dentro do grupo)
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
        return partidas
    }

    override fun calcularRanking(
        equipes: List<EquipeExemplo>,
        partidas: List<Partida>,
        configs: ConfiguracoesCampeonato
    ): List<LinhaTabela> {
        // Reutiliza a lógica de pontos do Brasileirão que já corrigimos
        return BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
    }
}
