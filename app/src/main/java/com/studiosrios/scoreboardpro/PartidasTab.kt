package com.studiosrios.scoreboardpro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
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
fun PartidasTab(
    partidas: List<Partida>,
    equipes: List<EquipeExemplo>,
    onPreJogoClick: (Partida) -> Unit,
    onDetalhesClick: (Partida) -> Unit
) {
    val partidasOrdenadas = obterPartidasOrdenadas(partidas)

    LazyColumn(Modifier.padding(16.dp)) {
        items(partidasOrdenadas) { partida ->
            val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
            val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"
            
            val placarTexto = if (partida.golsMandante == null && partida.golsVisitante == null) {
                " VS "
            } else {
                " ${partida.golsMandante ?: 0} x ${partida.golsVisitante ?: 0} "
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Cabeçalho da Partida (Data, Hora, Local)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                        Text("LOCAL: ${partida.local}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Spacer(Modifier.height(8.dp))

                    // Placar Central
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(mandante, Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(placarTexto, Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (partida.finalizada) Color.Blue else Color.Gray)
                        Text(visitante, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    if (partida.finalizada) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFB79400), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Melhor: ${partida.melhorJogador.ifBlank { "Não definido" }}", fontSize = 10.sp, color = Color(0xFFB79400), fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    // BOTÕES DE AÇÃO: PRÉ-JOGO (Esquerda) e DETALHES (Direita)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OutlinedButton(
                            onClick = { onPreJogoClick(partida) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Login, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PRÉ-JOGO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onDetalhesClick(partida) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("DETALHES", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
