package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun AbaEquipesTelespectador(equipes: List<EquipeExemplo>, onEquipeClick: (EquipeExemplo) -> Unit) {
    val equipesOrdenadas = equipes.sortedBy { it.nome }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(equipesOrdenadas) { equipe ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onEquipeClick(equipe) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(equipe.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(equipe.city, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesEquipe(
    equipe: EquipeExemplo,
    partidas: List<Partida>,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("Cadastro", "Jogadores", "Estatísticas")
    var jogadorSelecionadoParaDetalhes by remember { mutableStateOf<JogadorExemplo?>(null) }

    if (jogadorSelecionadoParaDetalhes != null) {
        TelaDetalhesJogadorTelespectador(
            jogador = jogadorSelecionadoParaDetalhes!!,
            partidas = partidas,
            onVoltar = { jogadorSelecionadoParaDetalhes = null }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(equipe.nome, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TabRow(selectedTabIndex = abaSelecionada) {
                    titulos.forEachIndexed { index, titulo ->
                        Tab(
                            selected = abaSelecionada == index,
                            onClick = { abaSelecionada = index },
                            text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                when (abaSelecionada) {
                    0 -> AbaCadastroEquipe(equipe)
                    1 -> AbaJogadoresEquipe(equipe) { j -> jogadorSelecionadoParaDetalhes = j }
                    2 -> AbaEstatisticasEquipe(equipe, partidas)
                }
            }
        }
    }
}

@Composable
fun AbaCadastroEquipe(equipe: EquipeExemplo) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AsyncImage(
            model = equipe.escudoUri.ifBlank { R.drawable.ic_launcher_background },
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(24.dp))
        InfoRowEquipe("Nome do Time", equipe.nome)
        InfoRowEquipe("Cidade", equipe.city)
        if (equipe.identificacao.isNotBlank()) {
            InfoRowEquipe("Identificação", equipe.identificacao)
        }
        
        if (equipe.patrocinadores.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Patrocinadores", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(equipe.patrocinadores) { pat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = pat.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(pat.nome, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowEquipe(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
    }
}

@Composable
fun AbaJogadoresEquipe(equipe: EquipeExemplo, onJogadorClick: (JogadorExemplo) -> Unit) {
    if (equipe.jogadores.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum jogador cadastrado.", color = Color.Gray)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            items(equipe.jogadores) { jogador ->
                ListItem(
                    modifier = Modifier.clickable { onJogadorClick(jogador) },
                    leadingContent = {
                        AsyncImage(
                            model = jogador.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    },
                    headlineContent = { Text(jogador.nome, fontWeight = FontWeight.Bold) },
                    supportingContent = { 
                        Text("${jogador.posicao} | ${jogador.idade} anos | ${jogador.altura}")
                    },
                    trailingContent = {
                        if (jogador.gols > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${jogador.gols}", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), fontSize = 18.sp)
                                Text("GOLS", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun AbaEstatisticasEquipe(equipe: EquipeExemplo, partidas: List<Partida>) {
    val stats = remember(equipe.id, partidas) {
        var j = 0; var v = 0; var e = 0; var d = 0; var gm = 0; var gs = 0; var am = 0; var vm = 0

        partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }.forEach { p ->
            j++
            val gM = p.golsMandante ?: 0
            val gV = p.golsVisitante ?: 0

            if (p.mandanteId == equipe.id) {
                gm += gM; gs += gV
                am += p.cartoesAmarelosMandante; vm += p.cartoesVermelhosMandante
                if (gM > gV) v++ else if (gM == gV) e++ else d++
            } else {
                gm += gV; gs += gM
                am += p.cartoesAmarelosVisitante; vm += p.cartoesVermelhosVisitante
                if (gV > gM) v++ else if (gV == gM) e++ else d++
            }
        }
        val pts = (v * 3) + e
        val aproveitamento = if (j > 0) (pts.toDouble() / (j * 3)) * 100 else 0.0
        
        listOf(
            "Pontos" to pts.toString(),
            "Jogos" to j.toString(),
            "Vitórias" to v.toString(),
            "Empates" to e.toString(),
            "Derrotas" to d.toString(),
            "Gols Marcados" to gm.toString(),
            "Gols Sofridos" to gs.toString(),
            "Saldo de Gols" to (gm - gs).toString(),
            "Cartões Amarelos" to am.toString(),
            "Cartões Vermelhos" to vm.toString(),
            "Aproveitamento" to String.format(Locale.US, "%.1f%%", aproveitamento)
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Desempenho no Campeonato", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
        }
        items(stats) { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = Color.DarkGray)
                Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesJogadorTelespectador(
    jogador: JogadorExemplo,
    partidas: List<Partida>,
    onVoltar: () -> Unit
) {
    val pontuacao = remember(jogador.nome, partidas) {
        calcularPontuacaoJogador(jogador, partidas)
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
            Text("${jogador.posicao} | ${jogador.idade} anos", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            
            Spacer(Modifier.height(24.dp))
            
            // CARD DE PONTUAÇÃO TOTAL
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
            
            // LISTA DE PONTOS POR CATEGORIA
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
            verticalAlignment = Alignment.CenterVertically
        ) {
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
