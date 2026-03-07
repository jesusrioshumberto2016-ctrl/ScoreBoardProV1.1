package com.studiosrios.scoreboardpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit

@Composable
fun PreJogoTab(
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    onEntrarEdicao: (Int) -> Unit = {},
    onSairEdicao: () -> Unit = {}
) {
    var partidaParaPreJogo by remember { mutableStateOf<Partida?>(null) }

    if (partidaParaPreJogo == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("PRÉ-JOGO: SELECIONE A PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            LazyColumn {
                items(partidas) { partida ->
                    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: partida.labelMandante
                    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: partida.labelVisitante

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                            partidaParaPreJogo = partida 
                            onEntrarEdicao(partida.id)
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(partida.fase.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                Text("$mandante vs $visitante", fontWeight = FontWeight.Bold)
                                Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    } else {
        val idx = partidas.indexOfFirst { it.id == partidaParaPreJogo?.id }
        if (idx != -1) {
            ConfiguracaoPreJogoDetalhada(
                p = partidas[idx],
                idx = idx,
                partidas = partidas,
                equipes = equipes,
                listaGlobalJogadores = listaGlobalJogadores,
                onVoltar = { 
                    partidaParaPreJogo = null 
                    onSairEdicao()
                }
            )
        }
    }
}

@Composable
fun ConfiguracaoPreJogoDetalhada(
    p: Partida,
    idx: Int,
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    listaGlobalJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    val mandanteNome = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
    val visitanteNome = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
    var opcaoSelecionada by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) { Icon(Icons.Default.ArrowBack, "Voltar") }
            Text("CONFIGURAR PARTIDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TabRow(selectedTabIndex = opcaoSelecionada, containerColor = Color(0xFFF5F5F5)) {
            Tab(selected = opcaoSelecionada == 0, onClick = { opcaoSelecionada = 0 }, text = { Text(mandanteNome.take(8), fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 1, onClick = { opcaoSelecionada = 1 }, text = { Text(visitanteNome.take(8), fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 2, onClick = { opcaoSelecionada = 2 }, text = { Text("CAMPO", fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 3, onClick = { opcaoSelecionada = 3 }, text = { Text("ÁRBITROS", fontSize = 9.sp, fontWeight = FontWeight.Bold) })
        }

        Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            when (opcaoSelecionada) {
                0, 1 -> {
                    val isMandante = opcaoSelecionada == 0
                    val idTimeAtual = if (isMandante) p.mandanteId else p.visitanteId
                    val jogadoresDoTime = listaGlobalJogadores.filter { it.equipeId == idTimeAtual }

                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Text("COMISSÃO TÉCNICA", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            OutlinedTextField(
                                value = if (isMandante) p.tecnicoMandante else p.tecnicoVisitante,
                                onValueChange = { v -> partidas[idx] = if (isMandante) p.copy(tecnicoMandante = v) else p.copy(tecnicoVisitante = v) },
                                label = { Text("Técnico") }, modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("ESCALAÇÃO (TITULARES E RESERVAS)", fontWeight = FontWeight.Bold, color = Color.Gray)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }

                        items(jogadoresDoTime) { jog ->
                            val isTitular = if (isMandante) p.titularesMandante.contains(jog.id) else p.titularesVisitante.contains(jog.id)
                            val isReserva = if (isMandante) p.reservasMandante.contains(jog.id) else p.reservasVisitante.contains(jog.id)
                            val posicaoNoJogo = p.posicoesNoJogo[jog.id] ?: jog.posicao

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = when { isTitular -> Color(0xFFE8F5E9); isReserva -> Color(0xFFFFF3E0); else -> Color.White })
                            ) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(jog.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$posicaoNoJogo | ${jog.idade}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    
                                    if (isTitular) {
                                        var showPosMenu by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedButton(
                                                onClick = { showPosMenu = true },
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text(posicaoNoJogo, fontSize = 10.sp)
                                            }
                                            DropdownMenu(expanded = showPosMenu, onDismissRequest = { showPosMenu = false }) {
                                                val posicoes = listOf("GOL", "ZAG", "LAT", "VOL", "MEI", "MAT", "ALA", "PT", "CA")
                                                posicoes.forEach { pos ->
                                                    DropdownMenuItem(
                                                        text = { Text(pos) },
                                                        onClick = {
                                                            val novoMapa = p.posicoesNoJogo.toMutableMap()
                                                            novoMapa[jog.id] = pos
                                                            partidas[idx] = p.copy(posicoesNoJogo = novoMapa)
                                                            showPosMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row {
                                        TextButton(onClick = {
                                            partidas[idx] = if (isMandante) {
                                                p.copy(titularesMandante = if (isTitular) p.titularesMandante - jog.id else p.titularesMandante + jog.id, reservasMandante = p.reservasMandante - jog.id)
                                            } else {
                                                p.copy(titularesVisitante = if (isTitular) p.titularesVisitante - jog.id else p.titularesVisitante + jog.id, reservasVisitante = p.reservasVisitante - jog.id)
                                            }
                                        }) { Text("TIT", color = if (isTitular) Color(0xFF2E7D32) else Color.Gray) }

                                        TextButton(onClick = {
                                            partidas[idx] = if (isMandante) {
                                                p.copy(reservasMandante = if (isReserva) p.reservasMandante - jog.id else p.reservasMandante + jog.id, titularesMandante = p.titularesMandante - jog.id)
                                            } else {
                                                p.copy(reservasVisitante = if (isReserva) p.reservasVisitante - jog.id else p.reservasVisitante + jog.id, titularesVisitante = p.titularesVisitante - jog.id)
                                            }
                                        }) { Text("RES", color = if (isReserva) Color(0xFFEF6C00) else Color.Gray) }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> CampoVisualEstiloSofa(p, equipes, listaGlobalJogadores)
                3 -> {
                    Column(Modifier.fillMaxSize()) {
                        Text("EQUIPE DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = Color.Gray)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        OutlinedTextField(value = p.arbitroPrincipal, onValueChange = { partidas[idx] = p.copy(arbitroPrincipal = it) }, label = { Text("Árbitro Principal") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = p.assistente1, onValueChange = { partidas[idx] = p.copy(assistente1 = it) }, label = { Text("Assistente 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = p.assistente2, onValueChange = { partidas[idx] = p.copy(assistente2 = it) }, label = { Text("Assistente 2") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("SALVAR E VOLTAR")
        }
    }
}

@Composable
fun CampoVisualEstiloSofa(p: Partida, equipes: List<EquipeExemplo>, todosJogadores: List<JogadorExemplo>) {
    val mandante = equipes.find { it.id == p.mandanteId }
    val visitante = equipes.find { it.id == p.visitanteId }
    
    val tMandante = todosJogadores.filter { it.id in p.titularesMandante }
    val tVisitante = todosJogadores.filter { it.id in p.titularesVisitante }
    
    val rMandante = todosJogadores.filter { it.id in p.reservasMandante }
    val rVisitante = todosJogadores.filter { it.id in p.reservasVisitante }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("FORMAÇÃO TÁTICA", fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .background(Color(0xFF2E7D32))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val lineCol = Color.White.copy(0.6f)
                drawLine(lineCol, Offset(0f, h/2), Offset(w, h/2), strokeWidth = 2.dp.toPx())
                drawCircle(lineCol, radius = 60.dp.toPx(), center = Offset(w/2, h/2), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawRect(lineCol, topLeft = Offset(w*0.2f, 0f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawRect(lineCol, topLeft = Offset(w*0.2f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
            }

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampo(tVisitante, p, isVisitante = true)
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampo(tMandante, p, isVisitante = false)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoMandante.ifBlank { "---" }}", fontSize = 10.sp, color = Color.Gray)
                Text(mandante?.nome ?: p.labelMandante, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("RESERVAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                rMandante.forEach { res ->
                    Text("${res.nome} (${res.posicao})", fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoVisitante.ifBlank { "---" }}", fontSize = 10.sp, color = Color.Gray)
                Text(visitante?.nome ?: p.labelVisitante, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("RESERVAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                rVisitante.forEach { res ->
                    Text("${res.nome} (${res.posicao})", fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun BoxScope.DistribuirJogadoresNoCampo(jogadores: List<JogadorExemplo>, p: Partida, isVisitante: Boolean) {
    val goleiros = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao) == "GOL" }
    val defensores = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao) in listOf("ZAG", "LAT") }
    val volantes = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao) == "VOL" }
    val meias = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao) in listOf("MEI", "MAT", "ALA") }
    val atacantes = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao) in listOf("PT", "CA") }

    val linhas = if (isVisitante) {
        listOf(goleiros, defensores, volantes, meias, atacantes)
    } else {
        listOf(atacantes, meias, volantes, defensores, goleiros)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        linhas.forEach { linha ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                linha.forEach { jog ->
                    JogadorIconeCampo(jog, if(isVisitante) Color(0xFFD32F2F) else Color(0xFF1976D2))
                }
            }
        }
    }
}

@Composable
fun JogadorIconeCampo(j: JogadorExemplo, corTime: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = corTime,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = j.nome.take(1).uppercase() + (j.nome.split(" ").getOrNull(1)?.take(1)?.uppercase() ?: ""),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        Text(
            text = j.nome.split(" ").last(), 
            fontSize = 9.sp, 
            color = Color.White, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
