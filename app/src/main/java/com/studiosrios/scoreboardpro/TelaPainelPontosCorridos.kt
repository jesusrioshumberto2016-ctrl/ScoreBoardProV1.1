package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.studiosrios.scoreboardpro.data.repository.DataRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelPontosCorridos(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    isOrganizador: Boolean = true,
    repository: DataRepository? = null,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulosAbas = listOf("Tabela", "Jogos", "Artilharia", "Configs")
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var houveAlteracaoGeral by remember { mutableStateOf(false) }

    // Estado local para as configurações para permitir edição em tempo real antes de salvar no DB
    var configsEditaveis by remember(configsIniciais) { mutableStateOf(configsIniciais) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (equipeSelecionada == null) {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Painel: $modelo", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            Button(
                                onClick = onVoltar,
                                modifier = Modifier.padding(start = 8.dp).height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("SAIR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        },
                        actions = {
                            if (isOrganizador) {
                                IconButton(onClick = {
                                    onSalvarGeral(idCamp, configsEditaveis)
                                    houveAlteracaoGeral = false
                                    scope.launch { snackbarHostState.showSnackbar("Dados salvos!") }
                                }) { 
                                    BadgedBox(badge = { if(houveAlteracaoGeral) Badge(containerColor = Color.Red) }) {
                                        Icon(Icons.Default.Save, contentDescription = "Salvar") 
                                    }
                                }
                            }
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
                        configs = configsEditaveis,
                        onEquipeClick = { e -> equipeSelecionada = e }
                    )
                    1 -> PartidasTab(
                        partidas = SnapshotStateList<Partida>().apply { addAll(partidasOrdenadas) }, 
                        equipes = equipes, 
                        onPreJogoClick = {}, 
                        onDetalhesClick = {},
                        onEquipeClick = { e -> equipeSelecionada = e }
                    )
                    2 -> TelaArtilharia(
                        equipes = equipes, 
                        partidas = partidas, 
                        listaGlobalJogadores = listaGlobalJogadores,
                        onEquipeClick = { e -> equipeSelecionada = e }
                    )
                    3 -> {
                        ConfigTab(
                            idCamp = idCamp,
                            equipes = equipes,
                            partidas = partidas,
                            configsAtuais = configsEditaveis,
                            onConfigsChanged = { novas -> 
                                configsEditaveis = novas
                                houveAlteracaoGeral = true 
                            },
                            onSalvarGeral = { id, finalConfigs ->
                                onSalvarGeral(id, finalConfigs)
                                houveAlteracaoGeral = false
                                scope.launch { snackbarHostState.showSnackbar("Configurações salvas!") }
                            }
                        )
                    }
                }
            }
        }
    }
}
