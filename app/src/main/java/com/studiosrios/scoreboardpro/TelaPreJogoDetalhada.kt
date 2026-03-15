package com.studiosrios.scoreboardpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun TelaPreJogoDetalhada(
    partida: Partida,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    val mandante = equipes.find { it.id == partida.mandanteId }
    val visitante = equipes.find { it.id == partida.visitanteId }
    
    val titularesM = todosJogadores.filter { it.id in partida.titularesMandante }
    val titularesV = todosJogadores.filter { it.id in partida.titularesVisitante }
    val reservasM = todosJogadores.filter { it.id in partida.reservasMandante }
    val reservasV = todosJogadores.filter { it.id in partida.reservasVisitante }

    var jogadorParaVerAcoes by remember { mutableStateOf<JogadorExemplo?>(null) }

    if (jogadorParaVerAcoes != null) {
        val acoes = partida.eventos.filter { it.jogadorNome == jogadorParaVerAcoes?.nome }
        AlertDialog(
            onDismissRequest = { jogadorParaVerAcoes = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = jogadorParaVerAcoes!!.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(jogadorParaVerAcoes!!.apelido.ifBlank { jogadorParaVerAcoes!!.nome }, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("AÇÕES NESTA PARTIDA:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    if (acoes.isEmpty()) {
                        Text("Nenhuma ação registrada.", color = Color.LightGray, fontSize = 14.sp)
                    } else {
                        acoes.forEach { ev ->
                            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("[${ev.minuto}']", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(ev.tipo, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { jogadorParaVerAcoes = null }) { Text("FECHAR") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Surface(shadowElevation = 4.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("PRÉ-JOGO DETALHADO", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(partida.fase.uppercase(), fontWeight = FontWeight.Black, color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = mandante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background, contentDescription = null, modifier = Modifier.size(50.dp))
                            Text(mandante?.nome ?: partida.labelMandante, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                        Text("VS", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = visitante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background, contentDescription = null, modifier = Modifier.size(50.dp))
                            Text(visitante?.nome ?: partida.labelVisitante, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("FORMAÇÃO NO CAMPO", modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
            Box(modifier = Modifier.fillMaxWidth().height(550.dp).padding(16.dp).background(Color(0xFF2E7D32)).border(2.dp, Color.White)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val fieldColor = Color.White
                    val strokeWidth = 2.dp.toPx()
                    
                    drawLine(fieldColor, Offset(0f, h/2), Offset(w, h/2), strokeWidth = strokeWidth)
                    drawCircle(fieldColor, radius = 60.dp.toPx(), center = Offset(w/2, h/2), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    
                    drawRect(fieldColor, topLeft = Offset(w*0.2f, 0f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    drawRect(fieldColor, topLeft = Offset(w*0.35f, 0f), size = androidx.compose.ui.geometry.Size(w*0.3f, h*0.04f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    
                    drawRect(fieldColor, topLeft = Offset(w*0.2f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    drawRect(fieldColor, topLeft = Offset(w*0.35f, h*0.96f), size = androidx.compose.ui.geometry.Size(w*0.3f, h*0.04f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                }
                
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        DistribuirJogadoresNoCampoDetalhado(titularesV, partida, true) { j -> jogadorParaVerAcoes = j }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        DistribuirJogadoresNoCampoDetalhado(titularesM, partida, false) { j -> jogadorParaVerAcoes = j }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    reservasM.forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).clickable { jogadorParaVerAcoes = res }) {
                            AsyncImage(model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                            Spacer(Modifier.width(8.dp))
                            Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                        }
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    reservasV.forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { jogadorParaVerAcoes = res }) {
                            Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            AsyncImage(model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                        }
                    }
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column(Modifier.padding(16.dp)) {
                    InfoPreJogoItem("Técnico Mandante", partida.tecnicoMandante)
                    InfoPreJogoItem("Técnico Visitante", partida.tecnicoVisitante)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    InfoPreJogoItem("Árbitro Principal", partida.arbitroPrincipal)
                    InfoPreJogoItem("Assistente 1", partida.assistente1)
                    InfoPreJogoItem("Assistente 2", partida.assistente2)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoPreJogoItem(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BoxScope.DistribuirJogadoresNoCampoDetalhado(jogadores: List<JogadorExemplo>, p: Partida, isVisitante: Boolean, onClick: (JogadorExemplo) -> Unit) {
    val jogadoresComPosicao = jogadores.sortedBy { jog ->
        val pos = p.posicoesNoJogo[jog.id] ?: jog.posicao
        when {
            pos.contains("GOL") -> 0
            pos.contains("ZAG") || pos.contains("LAT") -> 1
            pos.contains("VOL") || pos.contains("MEI") -> 2
            else -> 3
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        val grupos = if (isVisitante) jogadoresComPosicao.chunked(3) else jogadoresComPosicao.reversed().chunked(3)
        grupos.forEach { fila ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                fila.forEach { jog ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick(jog) }) {
                        Surface(modifier = Modifier.size(35.dp), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(2.dp, if(isVisitante) Color.Red else Color.Blue)) {
                            AsyncImage(model = jog.fotoUri.ifBlank { R.drawable.ic_launcher_background }, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        }
                        Text(jog.apelido.ifBlank { jog.nome.take(5) }, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black.copy(0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                    }
                }
            }
        }
    }
}
