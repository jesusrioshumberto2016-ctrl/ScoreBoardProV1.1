package com.studiosrios.scoreboardpro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
fun TelaPainelCampeonato(
    idCamp: Int,
    nomeCamp: String,
    fotoCamp: String, // Adicionado parâmetro para a foto
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    isOrganizador: Boolean = true,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    // Roteador de Painéis: Identifica o modelo e abre a tela correspondente com o perfil correto
    when {
        modelo.contains("Libertadores", ignoreCase = true) -> {
            TelaPainelLibertadores(
                idCamp = idCamp,
                nomeCamp = nomeCamp,
                fotoCamp = fotoCamp, // Repassa a foto
                equipes = equipes,
                partidas = partidas,
                listaGlobalJogadores = listaGlobalJogadores,
                configsIniciais = configsIniciais,
                listaGruposConfig = listaGruposConfig,
                isOrganizador = isOrganizador,
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
