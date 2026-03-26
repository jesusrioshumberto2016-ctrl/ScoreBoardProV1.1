package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelPontosCorridos(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulosAbas = listOf("Tabela", "Jogos", "Artilharia", "Configs")
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }

    // Ordenação das partidas para pontos corridos
    val partidasOrdenadas by remember {
        derivedStateOf {
            partidas.sortedWith(
                compareBy<Partida> { it.data.split("/").reversed().joinToString("") }
                .thenBy { it.horario }
            )
        }
    }

    Scaffold(
        topBar = {
            if (equipeSelecionada == null) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Painel: $modelo", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = onVoltar) { Text("Sair") }
                        }
                    )
                    TabRow(selectedTabIndex = abaSelecionada) {
                        titulosAbas.forEachIndexed { index, titulo ->
                            Tab(
                                selected = abaSelecionada == index,
                                onClick = { abaSelecionada = index },
                                text = { Text(titulo, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(if (equipeSelecionada != null) PaddingValues(0.dp) else paddingValues).fillMaxSize()) {
            if (equipeSelecionada != null) {
                TelaDetalhesEquipe(
                    equipe = equipeSelecionada!!,
                    partidas = partidas,
                    onVoltar = { equipeSelecionada = null }
                )
            } else {
                when (abaSelecionada) {
                    0 -> TelaTabelaRanking(
                        equipes = equipes, 
                        partidas = partidas, 
                        configs = configsIniciais,
                        onEquipeClick = { e -> equipeSelecionada = e }
                    )
                    1 -> PartidasTab(SnapshotStateList<Partida>().apply { addAll(partidasOrdenadas) }, equipes, {}, {})
                    2 -> TelaArtilharia(equipes, partidas, listaGlobalJogadores)
                    3 -> {
                        TelaConfiguracoesCampeonato(
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
}
