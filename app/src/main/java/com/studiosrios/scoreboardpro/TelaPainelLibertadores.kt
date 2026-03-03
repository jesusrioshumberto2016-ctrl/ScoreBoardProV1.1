package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelLibertadores(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    // Abas atualizadas conforme pedido: "Jogos" -> "Resultados" + novas abas "Partidas", "Súmula", "Pré-Jogo"
    val titulosAbas = listOf("Grupos", "Resultados", "Partidas", "Súmula", "Pré-Jogo", "Artilharia", "Configs")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Painel Libertadores", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        TextButton(onClick = onVoltar) { Text("Sair") }
                    }
                )
                // Usamos ScrollableTabRow pois agora temos muitas abas
                ScrollableTabRow(
                    selectedTabIndex = abaSelecionada,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    titulosAbas.forEachIndexed { index, titulo ->
                        Tab(
                            selected = abaSelecionada == index,
                            onClick = { abaSelecionada = index },
                            text = { Text(titulo, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (abaSelecionada) {
                0 -> PainelGruposLibertadores(equipes, partidas, configsIniciais, listaGruposConfig)
                1 -> ResultadosTab(partidas, equipes)
                2 -> PartidasTab(partidas, equipes, onPartidaClick = { /* Navegação opcional */ })
                3 -> SumulaTab(partidas, equipes, listaGlobalJogadores)
                4 -> PreJogoTab(equipes, partidas, listaGlobalJogadores)
                5 -> TelaArtilharia(equipes, listaGlobalJogadores)
                6 -> {
                    ConfigLibertadores(
                        configs = configsIniciais,
                        onSalvar = { novasConfigs ->
                            onSalvarGeral(idCamp, novasConfigs)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PainelGruposLibertadores(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    configs: ConfiguracoesCampeonato,
    listaGrupos: List<ConfigGrupo>
) {
    var indiceInicio = 0
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (listaGrupos.isEmpty()) {
            Text("Nenhum grupo configurado.", Modifier.padding(16.dp))
        } else {
            listaGrupos.forEach { grupo ->
                // Proteção para não tentar acessar índice fora da lista de equipes
                if (indiceInicio < equipes.size) {
                    Text(
                        text = grupo.nome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                    val fim = (indiceInicio + grupo.qtdTimes).coerceAtMost(equipes.size)
                    val equipesDesteGrupo = equipes.subList(indiceInicio, fim)
                    TelaTabelaRanking(equipes = equipesDesteGrupo, partidas = partidas, configs = configs)
                    indiceInicio += grupo.qtdTimes
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
