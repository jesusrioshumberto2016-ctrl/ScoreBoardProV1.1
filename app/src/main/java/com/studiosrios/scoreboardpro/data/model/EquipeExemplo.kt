package com.studiosrios.scoreboardpro.data.model

/**
 * Representa uma equipe no app.
 * @param id Identificador único da equipe.
 * @param nome Nome completo da equipe.
 * @param city Cidade de origem da equipe.
 * @param identificacao Sigla ou código de identificação (ex: 'FLA').
 */
data class EquipeExemplo(
    val id: Int,
    val nome: String,
    val city: String,
    val identificacao: String? = null
)
