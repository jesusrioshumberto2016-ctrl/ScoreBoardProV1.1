package com.studiosrios.scoreboardpro.data.model

data class Campeonato(
    var id: String = "",
    var nome: String = "",
    var ownerId: String = "",
    var organizadorId: String = "",
    var data: String = "",
    var status: String = "",
    var isFavorite: Boolean = false
)
