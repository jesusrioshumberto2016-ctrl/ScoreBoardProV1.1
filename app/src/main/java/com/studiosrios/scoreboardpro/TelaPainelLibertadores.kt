package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelLibertadores(
    idCamp: Int,
    nomeCamp: String, 
    fotoCamp: String, 
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    isOrganizador: Boolean = true,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    
    val titulosAbas = if (isOrganizador) {
        listOf("Grupos", "Mata-Mata", "Resultados", "Partidas", "Súmula", "Pré-Jogo", "Artilharia", "Configs")
    } else {
        listOf("Grupos", "Mata", "Partidas", "Artilharia", "Equipes")
    }

    var partidaParaVerPreJogo by remember { mutableStateOf<Partida?>(null) }
    var partidaParaVerDetalhes by remember { mutableStateOf<Partida?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }
    
    var editandoSumulaId by remember { mutableStateOf<Int?>(null) }
    var editandoPreJogoId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val gruposFinalizados by remember {
        derivedStateOf { verificarFaseGruposFinalizada(partidas) }
    }
    
    val algumaPartidaFinalizada by remember {
        derivedStateOf { partidas.any { it.finalizada } }
    }

    val ocultarNavegacao = partidaParaVerPreJogo != null || 
                          partidaParaVerDetalhes != null || 
                          equipeSelecionada != null ||
                          editandoSumulaId != null || 
                          editandoPreJogoId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!ocultarNavegacao) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isOrganizador) {
                                    AsyncImage(
                                        model = fotoCamp.ifBlank { R.drawable.ic_launcher_background },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isOrganizador) "Painel Libertadores" else nomeCamp, 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isOrganizador) 18.sp else 16.sp,
                                    maxLines = 1
                                ) 
                            }
                        },
                        navigationIcon = {
                            TextButton(onClick = onVoltar, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { 
                                Text(if (isOrganizador) "Sair" else "Voltar") 
                            }
                        },
                        actions = {
                            if (isOrganizador) {
                                IconButton(onClick = {
                                    onSalvarGeral(idCamp, configsIniciais)
                                    scope.launch { snackbarHostState.showSnackbar("Dados salvos!") }
                                }) { Icon(Icons.Default.Save, contentDescription = "Salvar") }
                            }
                        }
                    )
                    
                    if (isOrganizador) {
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
                    } else {
                        TabRow(
                            selectedTabIndex = abaSelecionada,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            titulosAbas.forEachIndexed { index, titulo ->
                                Tab(
                                    selected = abaSelecionada == index,
                                    onClick = { abaSelecionada = index },
                                    text = { 
                                        Text(
                                            text = titulo, 
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        ) 
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(if (ocultarNavegacao) PaddingValues(0.dp) else paddingValues).fillMaxSize()) {
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
                equipeSelecionada != null -> {
                    TelaDetalhesEquipe(
                        equipe = equipeSelecionada!!,
                        partidas = partidas,
                        onVoltar = { equipeSelecionada = null }
                    )
                }
                else -> {
                    val abaNome = titulosAbas[abaSelecionada]
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (abaNome) {
                                "Grupos" -> PainelGruposLibertadores(equipes, partidas, configsIniciais, listaGruposConfig, onEquipeClick = { e -> equipeSelecionada = e })
                                "Mata", "Mata-Mata" -> { 
                                    val partidasMataMata = partidas.filter { 
                                        !it.fase.contains("RODADA", ignoreCase = true) && 
                                        !it.fase.contains("ÚNICA", ignoreCase = true) && 
                                        it.fase.isNotBlank() 
                                    }.sortedWith(compareBy<Partida> { it.data.split("/").reversed().joinToString("") }.thenBy { it.horario })
                                    
                                    ConteudoChaveamentoLibertadores(
                                        equipes = equipes, 
                                        partidas = partidasMataMata,
                                        onPreJogo = { p -> partidaParaVerPreJogo = p },
                                        onDetalhes = { p -> partidaParaVerDetalhes = p }
                                    )
                                }
                                "Equipes" -> AbaEquipesTelespectador(equipes, onEquipeClick = { e -> equipeSelecionada = e })
                                "Resultados" -> ResultadosTab(
                                    partidas = partidas, 
                                    equipes = equipes,
                                    onConfirmarResultado = { p ->
                                        if (!p.fase.contains("RODADA", ignoreCase = true) && !p.fase.contains("ÚNICA", ignoreCase = true)) {
                                            promoverClassificadosMataMata(partidas, equipes, listaGruposConfig, configsIniciais)
                                        }
                                    }
                                )
                                "Partidas" -> PartidasTab(partidas, equipes, {p -> partidaParaVerPreJogo = p}, {p -> partidaParaVerDetalhes = p}, false)
                                "Súmula" -> SumulaTab(partidas, equipes, listaGlobalJogadores, {id -> editandoSumulaId = id}, {editandoSumulaId = null})
                                "Pré-Jogo" -> PreJogoTab(equipes, partidas, listaGlobalJogadores, {id -> editandoPreJogoId = id}, {editandoPreJogoId = null})
                                "Artilharia" -> TelaArtilharia(equipes, partidas, listaGlobalJogadores)
                                "Configs" -> ConfigLibertadores(configsIniciais, algumaPartidaFinalizada) { novasConfigs ->
                                    onSalvarGeral(idCamp, novasConfigs)
                                    scope.launch { snackbarHostState.showSnackbar("Configurações atualizadas!") }
                                }
                            }
                        }

                        if (isOrganizador && !ocultarNavegacao) {
                            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                                Button(
                                    onClick = { 
                                        promoverClassificadosMataMata(partidas, equipes, listaGruposConfig, configsIniciais)
                                        scope.launch { snackbarHostState.showSnackbar("Classificados definidos!") }
                                    },
                                    enabled = gruposFinalizados,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (gruposFinalizados) Color(0xFF2E7D32) else Color.Gray)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("CONCLUIR FASE", fontWeight = FontWeight.Bold)
                                }
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
            Text("Nenhuma partida de mata-mata gerada ainda.", color = Color.Gray)
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
    listaGrupos: List<ConfigGrupo>,
    onEquipeClick: (EquipeExemplo) -> Unit = {}
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
                    
                    TelaTabelaRanking(
                        equipes = equipesDesteGrupo, 
                        partidas = partidasDesteGrupo, 
                        configs = configs,
                        onEquipeClick = onEquipeClick
                    )
                    indiceInicio += grupo.qtdTimes
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
