package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CastConnected
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
import com.studiosrios.scoreboardpro.data.repository.DataRepository
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
    repository: DataRepository? = null,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    
    val titulosAbas = if (isOrganizador) {
        listOf("Grupos", "Mata-Mata", "Resultados", "Partidas", "Súmula", "Pré-Jogo", "Artilharia", "Configs")
    } else {
        listOf("Grupos", "Mata-Mata", "Partidas", "Artilharia", "Equipes")
    }

    // Ordenação global para ser usada em todas as abas
    val partidasOrdenadas by remember {
        derivedStateOf {
            obterPartidasOrdenadas(partidas)
        }
    }

    var partidaParaVerPreJogo by remember { mutableStateOf<Partida?>(null) }
    var partidaParaVerDetalhes by remember { mutableStateOf<Partida?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }
    var jogadorSelecionadoParaDetalhes by remember { mutableStateOf<JogadorExemplo?>(null) }
    
    var subAbaArtilhariaSelecionada by remember { mutableIntStateOf(0) }

    var editandoSumulaId by remember { mutableStateOf<Int?>(null) }
    var editandoPreJogoId by remember { mutableStateOf<Int?>(null) }

    var houveAlteracaoGeral by remember { mutableStateOf(false) }
    var temTrabalhoNaoConfirmado by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacaoSair by remember { mutableStateOf(false) }
    var abaPretendida by remember { mutableIntStateOf(-1) }

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
                          jogadorSelecionadoParaDetalhes != null ||
                          editandoSumulaId != null || 
                          editandoPreJogoId != null

    val tentarSairDoPainel = {
        abaPretendida = -1 
        if (isOrganizador && houveAlteracaoGeral && !ocultarNavegacao) {
            mostrarDialogoConfirmacaoSair = true
        } else {
            onVoltar()
        }
    }

    BackHandler(enabled = true) {
        when {
            jogadorSelecionadoParaDetalhes != null -> jogadorSelecionadoParaDetalhes = null
            equipeSelecionada != null -> equipeSelecionada = null
            partidaParaVerPreJogo != null -> partidaParaVerPreJogo = null
            partidaParaVerDetalhes != null -> partidaParaVerDetalhes = null
            editandoSumulaId != null || editandoPreJogoId != null -> { }
            else -> tentarSairDoPainel()
        }
    }

    if (mostrarDialogoConfirmacaoSair) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacaoSair = false },
            title = { Text("Alterações não salvas") },
            text = { Text("Você possui alterações que ainda não foram gravadas permanentemente. Deseja descartar as mudanças?") },
            confirmButton = {
                Button(
                    onClick = { 
                        mostrarDialogoConfirmacaoSair = false
                        houveAlteracaoGeral = false
                        temTrabalhoNaoConfirmado = false
                        if (abaPretendida != -1) {
                            abaSelecionada = abaPretendida
                            abaPretendida = -1
                        } else {
                            onVoltar()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("DESCARTAR") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacaoSair = false }) { Text("CONTINUAR EDITANDO") }
            }
        )
    }

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
                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray),
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
                            Button(
                                onClick = tentarSairDoPainel,
                                modifier = Modifier.padding(start = 8.dp).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.6.dp))
                                Text(if (isOrganizador) "SAIR" else "VOLTAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        },
                        actions = {
                            if (isOrganizador) {
                                IconButton(onClick = {
                                    repository?.let { repo ->
                                        val campParaExibicao = CampeonatoSalvo(
                                            id = idCamp,
                                            nomeExibicao = nomeCamp,
                                            nome = nomeCamp,
                                            fotoUri = fotoCamp,
                                            equipes = equipes,
                                            partidas = partidas.toList(),
                                            modelo = "Libertadores",
                                            configs = configsIniciais,
                                            gruposConfig = listaGruposConfig
                                        )
                                        repo.salvarEmExibicao(campParaExibicao)
                                        scope.launch { snackbarHostState.showSnackbar("Publicado no Mural!") }
                                    }
                                }) {
                                    Icon(Icons.Default.CastConnected, contentDescription = "Publicar Mural", tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = {
                                    onSalvarGeral(idCamp, configsIniciais)
                                    houveAlteracaoGeral = false
                                    temTrabalhoNaoConfirmado = false
                                    scope.launch { snackbarHostState.showSnackbar("Dados salvos com sucesso!") }
                                }) { 
                                    BadgedBox(badge = { if(houveAlteracaoGeral) Badge(containerColor = Color.Red) }) {
                                        Icon(Icons.Default.Save, contentDescription = "Salvar") 
                                    }
                                }
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
                                onClick = {
                                    if (isOrganizador && temTrabalhoNaoConfirmado && abaSelecionada != index) {
                                        abaPretendida = index
                                        mostrarDialogoConfirmacaoSair = true
                                    } else {
                                        abaSelecionada = index
                                    }
                                },
                                text = { Text(titulo, fontSize = 11.sp, fontWeight = if(isOrganizador) FontWeight.Normal else FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(if (ocultarNavegacao) PaddingValues(0.dp) else paddingValues).fillMaxSize()) {
            when {
                jogadorSelecionadoParaDetalhes != null -> {
                    TelaDetalhesJogadorTelespectador(
                        jogador = jogadorSelecionadoParaDetalhes!!,
                        partidas = partidas,
                        equipes = equipes,
                        onVoltar = { jogadorSelecionadoParaDetalhes = null }
                    )
                }
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
                    val abaNome = titulosAbas[abaSelecionada]
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            when (abaNome) {
                                "Grupos" -> PainelGruposLibertadores(equipes, partidasOrdenadas, configsIniciais, listaGruposConfig, onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e })
                                "Mata-Mata" -> { 
                                    val partidasMataMata = partidasOrdenadas.filter { 
                                        !it.fase.contains("RODADA", ignoreCase = true) && !it.fase.contains("ÚNICA", ignoreCase = true) && it.fase.isNotBlank() 
                                    }
                                    ConteudoChaveamentoLibertadores(equipes, partidasMataMata, { p: Partida -> partidaParaVerPreJogo = p }, { p: Partida -> partidaParaVerDetalhes = p }, { e: EquipeExemplo -> equipeSelecionada = e })
                                }
                                "Equipes" -> AbaEquipesTelespectador(equipes, onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e })
                                "Resultados" -> ResultadosTab(
                                    partidas = partidas, 
                                    equipes = equipes, 
                                    onConfirmarResultado = { _: Partida -> 
                                        houveAlteracaoGeral = true
                                        onSalvarGeral(idCamp, configsIniciais) 
                                    }
                                )
                                "Partidas" -> PartidasTab(
                                    partidas = partidas, 
                                    equipes = equipes, 
                                    onPreJogoClick = {p: Partida -> partidaParaVerPreJogo = p}, 
                                    onDetalhesClick = {p: Partida -> partidaParaVerDetalhes = p}, 
                                    somenteMataMata = false, 
                                    onEquipeClick = {e: EquipeExemplo -> equipeSelecionada = e}
                                )
                                "Súmula" -> SumulaTab(
                                    partidas = partidas, 
                                    equipes = equipes, 
                                    todosJogadores = listaGlobalJogadores,
                                    onEntrarEdicao = {id: Int -> editandoSumulaId = id}, 
                                    onSairEdicao = {editandoSumulaId = null},
                                    onAlteracao = { houveAlteracaoGeral = true },
                                    onSalvar = { 
                                        houveAlteracaoGeral = true
                                        onSalvarGeral(idCamp, configsIniciais)
                                    }
                                )
                                "Pré-Jogo" -> PreJogoTab(
                                    equipes = equipes, 
                                    partidas = partidas,
                                    listaGlobalJogadores = listaGlobalJogadores, 
                                    onEntrarEdicao = {id: Int -> editandoPreJogoId = id}, 
                                    onSairEdicao = {editandoPreJogoId = null},
                                    onAlteracao = { houveAlteracaoGeral = true },
                                    onSalvar = {
                                        houveAlteracaoGeral = true
                                        onSalvarGeral(idCamp, configsIniciais)
                                    }
                                )
                                "Artilharia" -> TelaArtilharia(
                                    equipes = equipes, 
                                    partidas = partidas, 
                                    listaGlobalJogadores = listaGlobalJogadores, 
                                    subAbaSelecionada = subAbaArtilhariaSelecionada,
                                    onSubAbaSelecionadaChange = { subAbaArtilhariaSelecionada = it },
                                    onEquipeClick = { e: EquipeExemplo -> equipeSelecionada = e },
                                    onJogadorClick = { j: JogadorExemplo -> jogadorSelecionadoParaDetalhes = j }
                                )
                                "Configs" -> ConfigLibertadores(
                                    configs = configsIniciais,
                                    bloquearCriterios = algumaPartidaFinalizada,
                                    onSalvar = { novasConfigs: ConfiguracoesCampeonato ->
                                        onSalvarGeral(idCamp, novasConfigs)
                                        houveAlteracaoGeral = false
                                        temTrabalhoNaoConfirmado = false
                                        scope.launch { snackbarHostState.showSnackbar("Configurações atualizadas!") }
                                    },
                                    onVoltar = { tentarSairDoPainel() },
                                    onAlteracao = { temTrabalhoNaoConfirmado = true; houveAlteracaoGeral = true }
                                )
                            }
                        }

                        if (isOrganizador && !ocultarNavegacao) {
                            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                                Button(
                                    onClick = { 
                                        promoverClassificadosMataMata(partidas, equipes, listaGruposConfig, configsIniciais)
                                        houveAlteracaoGeral = true
                                        onSalvarGeral(idCamp, configsIniciais)
                                        scope.launch { snackbarHostState.showSnackbar("Classificados definidos!") }
                                    },
                                    enabled = gruposFinalizados,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (gruposFinalizados) Color(0xFF2E7D32) else Color(0xFF1976D2))
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
    onDetalhes: (Partida) -> Unit,
    onEquipeClick: (EquipeExemplo) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Fase Eliminatória", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (partidas.isEmpty()) {
            Text("Nenhum partida de mata-mata gerada ainda.", color = Color.Gray)
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
                        onDetalhesClick = onDetalhes,
                        onEquipeClick = onEquipeClick
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
            for (grupo in listaGrupos) {
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

@Composable
fun AbaEquipesTelespectador(
    equipes: List<EquipeExemplo>,
    onEquipeClick: (EquipeExemplo) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(equipes) { equipe ->
            ListItem(
                headlineContent = { Text(equipe.nome, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(equipe.city) },
                modifier = Modifier.clickable { onEquipeClick(equipe) },
                leadingContent = {
                    AsyncImage(
                        model = equipe.escudoUri.ifBlank { R.drawable.ic_launcher_background },
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(0.5.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
        }
    }
}
