package com.studiosrios.scoreboardpro

import androidx.compose.runtime.Composable

@Composable
fun TelaPainelCampeonato(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    when {
        modelo.contains("Libertadores", ignoreCase = true) -> {
            TelaPainelLibertadores(
                idCamp = idCamp,
                equipes = equipes,
                partidas = partidas,
                listaGlobalJogadores = listaGlobalJogadores,
                configsIniciais = configsIniciais,
                listaGruposConfig = listaGruposConfig,
                onSalvarGeral = onSalvarGeral,
                onVoltar = onVoltar
            )
        }
        modelo.contains("Mata-Mata", ignoreCase = true) -> {
            TelaPainelMataMata(
                idCamp = idCamp,
                equipes = equipes,
                partidas = partidas,
                listaGlobalJogadores = listaGlobalJogadores,
                configsIniciais = configsIniciais,
                onSalvarGeral = onSalvarGeral,
                onVoltar = onVoltar
            )
        }
        else -> {
            TelaPainelPontosCorridos(
                idCamp = idCamp,
                equipes = equipes,
                partidas = partidas,
                modelo = modelo,
                listaGlobalJogadores = listaGlobalJogadores,
                configsIniciais = configsIniciais,
                onSalvarGeral = onSalvarGeral,
                onVoltar = onVoltar
            )
        }
    }
}
