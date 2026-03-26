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
import java.util.Locale

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
        val jog = jogadorParaVerAcoes!!
        val acoes = partida.eventos.filter { it.jogadorNome == jog.nome }
        AlertDialog(
            onDismissRequest = { jogadorParaVerAcoes = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = jog.fotoUri.ifBlank { "" },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(jog.apelido.ifBlank { jog.nome }, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            AsyncImage(model = mandante?.escudoUri?.ifBlank { "" } ?: "", contentDescription = null, modifier = Modifier.size(50.dp))
                            Text(mandante?.nome ?: partida.labelMandante, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                        Text("VS", fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(model = visitante?.escudoUri?.ifBlank { "" } ?: "", contentDescription = null, modifier = Modifier.size(50.dp))
                            Text(visitante?.nome ?: partida.labelVisitante, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("FORMAÇÃO NO CAMPO", modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
            Box(modifier = Modifier.fillMaxWidth().height(650.dp).padding(16.dp).background(Color(0xFF2E7D32)).border(2.dp, Color.White)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val fieldColor = Color.White
                    val strokeWidth = 2.dp.toPx()
                    
                    drawLine(fieldColor, Offset(0f, h/2), Offset(w, h/2), strokeWidth = strokeWidth)
                    drawCircle(fieldColor, radius = 60.dp.toPx(), center = Offset(w/2, h/2), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    
                    drawRect(fieldColor, topLeft = Offset(w*0.2f, 0f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                    drawRect(fieldColor, topLeft = Offset(w*0.2f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
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
                    if (partida.tecnicoMandante.isNotBlank()) {
                        Text("TÉC: ${partida.tecnicoMandante.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = mandante?.escudoUri?.ifBlank { "" } ?: "",
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    }
                    reservasM.forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).clickable { jogadorParaVerAcoes = res }) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AsyncImage(model = res.fotoUri.ifBlank { "" }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                        }
                    }
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    if (partida.tecnicoVisitante.isNotBlank()) {
                        Text("TÉC: ${partida.tecnicoVisitante.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray, textAlign = TextAlign.End, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End)
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(
                            model = visitante?.escudoUri?.ifBlank { "" } ?: "",
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                        )
                    }
                    reservasV.forEach { res ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { jogadorParaVerAcoes = res }) {
                            Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                            Spacer(Modifier.width(8.dp))
                            Box(contentAlignment = Alignment.BottomEnd) {
                                AsyncImage(model = res.fotoUri.ifBlank { "" }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                            }
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

fun obterCorNota(nota: Double): Color {
    return when {
        nota >= 8.5 -> Color(0xFF1B5E20) // Verde Escuro (Excelente)
        nota >= 7.5 -> Color(0xFF4CAF50) // Verde (Bom)
        nota >= 6.5 -> Color(0xFFCDDC39) // Lima (Regular+)
        nota >= 5.5 -> Color(0xFFFFEB3B) // Amarelo (Regular)
        nota >= 4.5 -> Color(0xFFFF9800) // Laranja (Ruim)
        else -> Color(0xFFF44336) // Vermelho (Péssimo)
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
    fun filtrar(pos: List<String>) = jogadores.filter { (p.posicoesNoJogo[it.id.toString()] ?: it.posicao).split(" ").first() in pos }

    val goleiros = filtrar(listOf("GOL"))
    val defesas = filtrar(listOf("ZAG", "LAT"))
    val volantes = filtrar(listOf("VOL"))
    val meias = filtrar(listOf("MEI", "MAT", "ALA"))
    val atacantes = filtrar(listOf("PT", "CA"))

    val linhas = if (isVisitante) listOf(goleiros, defesas, volantes, meias, atacantes)
                 else listOf(atacantes, meias, volantes, defesas, goleiros)

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        linhas.forEach { linha ->
            val linhaOrdenada = linha.sortedWith(compareBy<JogadorExemplo> { jog ->
                val pos = p.posicoesNoJogo[jog.id.toString()] ?: jog.posicao
                when {
                    pos.contains("(E)") -> 0
                    pos.contains("(D)") -> 2
                    else -> 1
                }
            })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                linhaOrdenada.forEach { jog ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick(jog) }) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(2.dp, if(isVisitante) Color(0xFFD32F2F) else Color(0xFF1976D2))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AsyncImage(
                                        model = jog.fotoUri.ifBlank { "" },
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Text(
                            text = jog.apelido.ifBlank { jog.nome.split(" ").last() },
                            fontSize = 9.sp, 
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
