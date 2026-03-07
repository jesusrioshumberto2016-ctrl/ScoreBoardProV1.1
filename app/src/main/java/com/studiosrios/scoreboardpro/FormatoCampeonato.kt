package com.studiosrios.scoreboardpro

interface FormatoCampeonato {
    val nomeTipo: String

    fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo> = emptyList(),
        confrontosMataMata: List<Pair<String, String>> = emptyList(),
        idaEVoltaMataMata: Boolean = false,
        idaEVoltaFinal: Boolean = false
    ): List<Partida>

    fun calcularRanking(equipes: List<EquipeExemplo>, partidas: List<Partida>, configs: ConfiguracoesCampeonato): List<LinhaTabela>
}
