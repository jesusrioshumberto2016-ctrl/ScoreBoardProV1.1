package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPainelCampeonato(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    listaGruposConfig: List<ConfigGrupo>,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    
    // Define as abas com base no modelo
    val titulosAbas = when {
        modelo.contains("Mata-Mata", ignoreCase = true) -> listOf("Chaveamento", "Jogos", "Artilharia", "Configs")
        modelo.contains("Libertadores", ignoreCase = true) -> listOf("Grupos", "Jogos", "Artilharia", "Configs")
        else -> listOf("Tabela", "Jogos", "Artilharia", "Configs")
    }

    Scaffold(
        topBar = {
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
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (abaSelecionada) {
                0 -> {
                    // Aba Principal Dinâmica
                    when {
                        modelo.contains("Libertadores", ignoreCase = true) -> {
                            PainelGrupos(equipes, partidas, configsIniciais, listaGruposConfig)
                        }
                        modelo.contains("Mata-Mata", ignoreCase = true) -> {
                            PainelMataMata(equipes, partidas)
                        }
                        else -> {
                            TelaTabelaRanking(equipes = equipes, partidas = partidas, configs = configsIniciais)
                        }
                    }
                }
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
fun PainelGrupos(
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

@Composable
fun PainelMataMata(equipes: List<EquipeExemplo>, partidas: List<Partida>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Chaveamento Mata-Mata", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Aqui você pode implementar uma visualização de chaves futuramente.
        // Por enquanto, listamos os confrontos de forma organizada.
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
