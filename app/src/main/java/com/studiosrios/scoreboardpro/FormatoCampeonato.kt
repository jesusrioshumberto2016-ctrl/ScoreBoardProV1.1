package com.studiosrios.scoreboardpro

interface FormatoCampeonato {
    val nomeTipo: String

    // Adicionamos o 'configsGrupos' como opcional (vazio por padrão)
    fun gerarCalendario(
        equipes: List<EquipeExemplo>,
        turnoEReturno: Boolean,
        configsGrupos: List<ConfigGrupo> = emptyList()
    ): List<Partida>

    fun calcularRanking(equipes: List<EquipeExemplo>, partidas: List<Partida>, configs: ConfiguracoesCampeonato): List<LinhaTabela>
}

