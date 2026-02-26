package com.studiosrios.scoreboardpro.data.model

/**
 * Representa um jogador no app.
 * @param id Identificador único do jogador.
 * @param nome Nome completo do jogador.
 * @param posicao Posição em que o jogador atua.
 * @param altura Altura do jogador (ex: "1.85").
 * @param idade Idade do jogador.
 * @param equipeId ID da equipe à qual o jogador pertence (-1 se não tiver equipe).
 */
data class JogadorExemplo(
    val id: Int,
    val nome: String,
    val posicao: String,
    val altura: String,
    val idade: String,
    var equipeId: Int = -1
)
