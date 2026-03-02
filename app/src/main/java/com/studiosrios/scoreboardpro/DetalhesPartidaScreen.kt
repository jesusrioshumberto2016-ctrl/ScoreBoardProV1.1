package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaSumulaDetalhada(partida: Partida, equipes: List<EquipeExemplo>, onVoltar: () -> Unit) {
    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"

    // Correção: Gols são Int?, então verificamos se são null em vez de isEmpty()
    val gM = if(partida.golsMandante == null) "-" else partida.golsMandante.toString()
    val gV = if(partida.golsVisitante == null) "-" else partida.golsVisitante.toString()

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Text("DETALHES DA PARTIDA", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${partida.data} às ${partida.horario}", fontWeight = FontWeight.Bold)
                Text("Local: ${partida.local}")
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mandante, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("$gM X $gV", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(visitante, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                    Spacer(Modifier.width(8.dp))
                    Text("MELHOR JOGADOR: ${partida.melhorJogador.ifBlank { "Não definido" }}", fontWeight = FontWeight.Bold, color = Color(0xFFB79400))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("EVENTOS DA PARTIDA", fontWeight = FontWeight.Bold, color = Color.Gray)
        Divider()

        if (partida.eventos.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Nenhum evento registrado nesta súmula.", color = Color.LightGray)
            }
        } else {
            LazyColumn(Modifier.weight(1f).padding(top = 8.dp)) {
                items(partida.eventos) { ev ->
                    val corEvento = when(ev.tipo) {
                        "YELLOW CARD" -> Color(0xFFB79400)
                        "RED CARD" -> Color.Red
                        "GOL CONTRA" -> Color(0xFFD32F2F)
                        "PÊNALTI PERDIDO" -> Color(0xFF757575)
                        "PÊNALTI DEFENDIDO" -> Color(0xFF1976D2)
                        "ASSISTÊNCIA" -> Color(0xFF00ACC1)
                        "INÍCIO", "INTERVALO", "FIM DE JOGO" -> Color.Black
                        else -> Color(0xFF2E7D32)
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = corEvento,
                            modifier = Modifier.size(12.dp),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {}
                        Spacer(Modifier.width(12.dp))
                        Column {
                            val prefixoMin = if(ev.minuto.isNotBlank()) "[${ev.minuto}'] " else ""
                            Text(ev.tipo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = corEvento)
                            Text("$prefixoMin${ev.jogadorNome} (${ev.equipeNome})", fontSize = 14.sp)
                        }
                    }
                    Divider(Modifier.padding(start = 24.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }
        }

        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("VOLTAR PARA PARTIDAS")
        }
    }
}
