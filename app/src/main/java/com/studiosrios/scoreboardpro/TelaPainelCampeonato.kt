package com.studiosrios.scoreboardpro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.studiosrios.scoreboardpro.data.repository.DataRepository

@Composable
fun TelaPainelCampeonato(
    idCamp: Int,
    nomeCamp: String,
    fotoCamp: String,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    isOrganizador: Boolean = true,
    repository: DataRepository? = null, // Adicionado parâmetro
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    // Roteador de Painéis
    when {
        modelo.contains("Libertadores", ignoreCase = true) -> {
            TelaPainelLibertadores(
                idCamp = idCamp,
                nomeCamp = nomeCamp,
                fotoCamp = fotoCamp,
                equipes = equipes,
                partidas = partidas,
                listaGlobalJogadores = listaGlobalJogadores,
                configsIniciais = configsIniciais,
                listaGruposConfig = listaGruposConfig,
                isOrganizador = isOrganizador,
                repository = repository, // Passando para frente
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
                isOrganizador = isOrganizador,
                repository = repository, // Passando para frente
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
                isOrganizador = isOrganizador,
                repository = repository, // Passando para frente
                onSalvarGeral = onSalvarGeral,
                onVoltar = onVoltar
            )
        }
    }
}
