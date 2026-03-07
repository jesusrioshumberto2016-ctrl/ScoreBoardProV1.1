package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

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
    val titulosAbas = listOf("Grupos", "Mata-Mata", "Resultados", "Partidas", "Súmula", "Pré-Jogo", "Artilharia", "Configs")

    var partidaParaVerPreJogo by remember { mutableStateOf<Partida?>(null) }
    var partidaParaVerDetalhes by remember { mutableStateOf<Partida?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gruposFinalizados by remember {
        derivedStateOf { verificarFaseGruposFinalizada(partidas) }
    }
    
    // Verifica se alguma partida do campeonato foi finalizada
    val algumaPartidaFinalizada by remember {
        derivedStateOf { partidas.any { it.finalizada } }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (partidaParaVerPreJogo == null && partidaParaVerDetalhes == null) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Painel Libertadores", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            TextButton(onClick = onVoltar) { Text("Sair") }
                        },
                        actions = {
                            IconButton(onClick = {
                                onSalvarGeral(idCamp, configsIniciais)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Dados salvos com sucesso!")
                                }
                            }) {
                                Icon(Icons.Default.Save, contentDescription = "Salvar")
                            }
                        }
                    )
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
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
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (abaSelecionada) {
                                0 -> PainelGruposLibertadores(equipes, partidas, configsIniciais, listaGruposConfig)
                                1 -> {
                                    val partidasMataMata = partidas.filter { 
                                        !it.fase.contains("RODADA", ignoreCase = true) && 
                                        !it.fase.contains("ÚNICA", ignoreCase = true) && 
                                        it.fase.isNotBlank() 
                                    }.sortedWith(
                                        compareBy<Partida> { it.data.split("/").reversed().joinToString("") }
                                            .thenBy { it.horario }
                                    )
                                    
                                    ConteudoChaveamentoLibertadores(
                                        equipes = equipes, 
                                        partidas = partidasMataMata,
                                        onPreJogo = { p -> partidaParaVerPreJogo = p },
                                        onDetalhes = { p -> partidaParaVerDetalhes = p }
                                    )
                                }
                                2 -> ResultadosTab(partidas, equipes)
                                3 -> {
                                    PartidasTab(
                                        partidas = partidas,
                                        equipes = equipes,
                                        onPreJogoClick = { p -> partidaParaVerPreJogo = p },
                                        onDetalhesClick = { p -> partidaParaVerDetalhes = p },
                                        somenteMataMata = false
                                    )
                                }
                                4 -> SumulaTab(partidas, equipes, listaGlobalJogadores)
                                5 -> {
                                    val partidasOrdenadas = partidas.sortedWith(
                                        compareBy<Partida> { it.data.split("/").reversed().joinToString("") }
                                            .thenBy { it.horario }
                                    )
                                    val listaCastada = remember(partidasOrdenadas) {
                                        val novaLista = SnapshotStateList<Partida>()
                                        novaLista.addAll(partidasOrdenadas)
                                        novaLista
                                    }
                                    PreJogoTab(equipes, listaCastada, listaGlobalJogadores)
                                }
                                6 -> TelaArtilharia(equipes, partidas, listaGlobalJogadores)
                                7 -> {
                                    ConfigLibertadores(
                                        configs = configsIniciais,
                                        bloquearCriterios = algumaPartidaFinalizada, // Agora passa o estado dinâmico
                                        onSalvar = { novasConfigs ->
                                            onSalvarGeral(idCamp, novasConfigs)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Configurações atualizadas!")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 12.dp
                        ) {
                            Button(
                                onClick = { 
                                    promoverClassificadosMataMata(partidas, equipes, listaGruposConfig, configsIniciais)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Classificados definidos! Verifique a aba Mata-Mata.")
                                    }
                                },
                                enabled = gruposFinalizados,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (gruposFinalizados) Color(0xFF2E7D32) else Color.Gray,
                                    disabledContainerColor = Color.LightGray.copy(alpha = 0.6f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle, 
                                    contentDescription = null,
                                    tint = if (gruposFinalizados) Color.White else Color.DarkGray
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "CONCLUIR FASE DE GRUPOS", 
                                    fontWeight = FontWeight.Bold,
                                    color = if (gruposFinalizados) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConteudoChaveamentoLibertadores(
    equipes: List<EquipeExemplo>, 
    partidas: List<Partida>,
    onPreJogo: (Partida) -> Unit,
    onDetalhes: (Partida) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Fase Eliminatória", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (partidas.isEmpty()) {
            Text("Nenhuma partida de mata-mata gerada ainda.", color = MaterialTheme.colorScheme.secondary)
        } else {
            val fases = partidas.groupBy { it.fase }
            fases.forEach { (nomeFase, jogos) ->
                Text(nomeFase, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                jogos.forEach { partida ->
                    ItemPartidaCard(
                        partida = partida,
                        equipes = equipes,
                        onPreJogoClick = onPreJogo,
                        onDetalhesClick = onDetalhes
                    )
                }
                Spacer(Modifier.height(16.dp))
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
                    val idsEquipes = equipesDesteGrupo.map { it.id }
                    val partidasDesteGrupo = partidas.filter { it.mandanteId in idsEquipes && it.visitanteId in idsEquipes }
                    
                    TelaTabelaRanking(equipes = equipesDesteGrupo, partidas = partidasDesteGrupo, configs = configs)
                    indiceInicio += grupo.qtdTimes
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
