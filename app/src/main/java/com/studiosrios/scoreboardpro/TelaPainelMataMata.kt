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

    Scaffold(
        topBar = {
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
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (abaSelecionada) {
                0 -> ConteudoChaveamento(equipes, partidas)
                1 -> TelaListaJogos(partidas, equipes)
                2 -> TelaArtilharia(equipes, listaGlobalJogadores)
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

@Composable
fun ConteudoChaveamento(equipes: List<EquipeExemplo>, partidas: List<Partida>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Chaveamento Mata-Mata", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        partidas.forEach { partida ->
            val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "TBD"
            val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "TBD"
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(mandante, fontWeight = FontWeight.Medium)
                    Text("vs", color = MaterialTheme.colorScheme.secondary)
                    Text(visitante, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
