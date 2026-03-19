package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesEquipe(
    equipe: EquipeExemplo,
    partidas: List<Partida>,
    onVoltar: () -> Unit
) {
    var jogadorSelecionado by remember { mutableStateOf<JogadorExemplo?>(null) }

    if (jogadorSelecionado != null) {
        TelaDetalhesJogadorTelespectador(
            jogador = jogadorSelecionado!!,
            partidas = partidas,
            equipes = listOf(equipe),
            onVoltar = { jogadorSelecionado = null }
        )
        return
    }

    // Cálculo das estatísticas da equipe no campeonato
    val stats = remember(equipe.id, partidas) {
        var v = 0; var e = 0; var d = 0; var gm = 0; var gs = 0
        partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }.forEach { p ->
            val isMandante = p.mandanteId == equipe.id
            val golsF = if (isMandante) (p.golsMandante ?: 0) else (p.golsVisitante ?: 0)
            val golsC = if (isMandante) (p.golsVisitante ?: 0) else (p.golsMandante ?: 0)
            gm += golsF
            gs += golsC
            when {
                golsF > golsC -> v++
                golsF < golsC -> d++
                else -> e++
            }
        }
        object {
            val pontos = (v * 3) + e
            val jogos = v + e + d
            val vitorias = v
            val empates = e
            val derrotas = d
            val golsMarcados = gm
            val golsSofridos = gs
            val saldoGols = gm - gs
        }
    }

    BackHandler {
        onVoltar()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalhes da Equipe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // CABEÇALHO DA EQUIPE
            AsyncImage(
                model = equipe.escudoUri.ifBlank { R.drawable.ic_launcher_background },
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
            Text(equipe.nome, fontSize = 22.sp, fontWeight = FontWeight.Black)
            if (equipe.identificacao.isNotBlank()) {
                Text(equipe.identificacao.uppercase(), fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(equipe.city, fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            // ESTATÍSTICAS NO CAMPEONATO
            Text("DESEMPENHO NO CAMPEONATO", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        StatItem("${stats.pontos}", "PTS")
                        StatItem("${stats.jogos}", "J")
                        StatItem("${stats.vitorias}", "V")
                        StatItem("${stats.empates}", "E")
                        StatItem("${stats.derrotas}", "D")
                        StatItem("${stats.saldoGols}", "SG")
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Gols: ", fontSize = 11.sp, color = Color.Gray)
                        Text("${stats.golsMarcados} marcados", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(" / ", fontSize = 11.sp, color = Color.Gray)
                        Text("${stats.golsSofridos} sofridos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // PATROCINADORES
            if (equipe.patrocinadores.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("PATROCINADORES", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(equipe.patrocinadores) { pat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = pat.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Text(pat.nome, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // ELENCO
            Spacer(Modifier.height(24.dp))
            Text("ELENCO", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            equipe.jogadores.forEach { jogador ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { jogadorSelecionado = jogador },
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = jogador.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(jogador.apelido.ifBlank { jogador.nome }, fontWeight = FontWeight.Bold)
                            Text("${jogador.posicao} | ${jogador.idade} anos", fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp).rotate(180f), tint = Color.LightGray)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesJogadorTelespectador(
    jogador: JogadorExemplo,
    partidas: List<Partida>,
    equipes: List<EquipeExemplo> = emptyList(),
    onVoltar: () -> Unit
) {
    val pontuacao = remember(jogador.nome, partidas, equipes) {
        calcularPontuacaoJogador(jogador, partidas, equipes)
    }

    BackHandler {
        onVoltar()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estatísticas do Jogador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = jogador.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(12.dp))
            Text(jogador.nome, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("${jogador.posicao} | ${jogador.idade}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            
            Spacer(Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PONTUAÇÃO TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format(Locale.US, "%.1f", pontuacao.total), 
                        fontSize = 42.sp, 
                        fontWeight = FontWeight.Black, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("PONTOS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("DETALHAMENTO DO DESEMPENHO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            
            ItemPontuacaoDetalhada("Gols (+5.0)", pontuacao.gols, pontuacao.gols * 5.0)
            ItemPontuacaoDetalhada("Assistências (+2.5)", pontuacao.assistencias, pontuacao.assistencias * 2.5)
            ItemPontuacaoDetalhada("Defesa s/ sofrer gol (+3.0)", pontuacao.sg, pontuacao.sg * 3.0)
            ItemPontuacaoDetalhada("Melhor da Partida (+5.0)", pontuacao.mvp, pontuacao.mvp * 5.0)
            ItemPontuacaoDetalhada("Cartão Amarelo (-0.5)", pontuacao.amarelos, pontuacao.amarelos * -0.5)
            ItemPontuacaoDetalhada("Cartão Vermelho (-3.0)", pontuacao.vermelhos, pontuacao.vermelhos * -3.0)
            ItemPontuacaoDetalhada("Gol Contra (-5.0)", pontuacao.golsContra, pontuacao.golsContra * -5.0)
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ItemPontuacaoDetalhada(label: String, quantidade: Int, pontos: Double) {
    if (quantidade != 0) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Quantidade: $quantidade", fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                text = (if(pontos > 0) "+" else "") + String.format(Locale.US, "%.1f", pontos),
                fontWeight = FontWeight.Bold,
                color = if(pontos > 0) Color(0xFF2E7D32) else if(pontos < 0) Color.Red else Color.Gray,
                fontSize = 16.sp
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
    }
}
