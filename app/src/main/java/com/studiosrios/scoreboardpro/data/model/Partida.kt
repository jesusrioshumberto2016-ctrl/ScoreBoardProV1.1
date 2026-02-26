package com.studiosrios.scoreboardpro.data.model

/**
 * Representa uma partida no campeonato.
 * @param id Identificador único da partida.
 * @param mandanteId ID da equipe mandante.
 * @param visitanteId ID da equipe visitante.
 * @param golsMandante Gols da equipe mandante.
 * @param golsVisitante Gols da equipe visitante.
 * @param data Data da partida (ex: "25/12/2024").
 * @param hora Hora da partida (ex: "16:00").
 * @param local Estádio ou local da partida.
 * @param finalizada Indica se a partida já terminou.
 */
data class Partida(
    val id: Int,
    val mandanteId: Int,
    val visitanteId: Int,
    var golsMandante: Int = 0,
    var golsVisitante: Int = 0,
    val data: String,
    val hora: String,
    val local: String,
    var finalizada: Boolean = false
)
