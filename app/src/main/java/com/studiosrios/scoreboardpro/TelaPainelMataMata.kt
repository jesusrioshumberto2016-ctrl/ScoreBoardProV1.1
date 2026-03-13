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
fun TelaPainelMataMata(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulosAbas = listOf("Chaveamento", "Jogos", "Artilharia", "Configs")

    var partidaParaVerPreJogo by remember { mutableStateOf<Partida?>(null) }
    var partidaParaVerDetalhes by remember { mutableStateOf<Partida?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }

    Scaffold(
        topBar = {
            if (partidaParaVerPreJogo == null && partidaParaVerDetalhes == null && equipeSelecionada == null) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Painel Mata-Mata", fontWeight = FontWeight.Bold) },
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
            when {
                equipeSelecionada != null -> {
                    TelaDetalhesEquipe(
                        equipe = equipeSelecionada!!,
                        partidas = partidas,
                        onVoltar = { equipeSelecionada = null }
                    )
                }
                partidaParaVerPreJogo != null -> {
                    TelaPreJogoDetalhada(
                        partida = partidaParaVerPreJogo!!,
                        equipes = equipes,
                        todosJogadores = listaGlobalJogadores,
                        onVoltar = { partidaParaVerPreJogo = null }
                    )
                }
                partidaParaVerDetalhes != null -> {
                    TelaSumulaDetalhada(
                        partida = partidaParaVerDetalhes!!,
                        equipes = equipes,
                        onVoltar = { partidaParaVerDetalhes = null }
                    )
                }
                else -> {
                    when (abaSelecionada) {
                        0 -> {
                            ConteudoChaveamentoMataMata(
                                equipes = equipes,
                                partidas = partidas,
                                onPreJogo = { p -> partidaParaVerPreJogo = p },
                                onDetalhes = { p -> partidaParaVerDetalhes = p },
                                onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e }
                            )
                        }
                        1 -> {
                            PartidasTab(
                                partidas = partidas,
                                equipes = equipes,
                                onPreJogoClick = { p -> partidaParaVerPreJogo = p },
                                onDetalhesClick = { p -> partidaParaVerDetalhes = p },
                                somenteMataMata = true,
                                onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e }
                            )
                        }
                        2 -> TelaArtilharia(equipes, partidas, listaGlobalJogadores, onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e })
                        3 -> {
                            ConfigMataMata(
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
}

@Composable
fun ConteudoChaveamentoMataMata(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    onPreJogo: (Partida) -> Unit,
    onDetalhes: (Partida) -> Unit,
    onEquipeClick: (EquipeExemplo) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Chaveamento do Torneio", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (partidas.isEmpty()) {
            Text("Nenhum confronto gerado.", color = MaterialTheme.colorScheme.secondary)
        } else {
            val fases = partidas.groupBy { it.fase }
            fases.forEach { (nomeFase, jogos) ->
                Text(nomeFase.uppercase(), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                jogos.forEach { partida ->
                    ItemPartidaCard(
                        partida = partida,
                        equipes = equipes,
                        onPreJogoClick = onPreJogo,
                        onDetalhesClick = onDetalhes,
                        onEquipeClick = onEquipeClick
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
