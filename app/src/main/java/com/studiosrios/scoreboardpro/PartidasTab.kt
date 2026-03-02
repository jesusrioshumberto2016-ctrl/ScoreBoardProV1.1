package com.studiosrios.scoreboardpro

import androidx.compose.foundation.clickable
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
fun PartidasTab(
    partidas: List<Partida>,
    equipes: List<EquipeExemplo>,
    onPartidaClick: (Partida) -> Unit
) {
    // Usamos a lógica global para garantir a ordem correta
    val partidasOrdenadas = obterPartidasOrdenadas(partidas)

    LazyColumn(Modifier.padding(16.dp)) {
        items(partidasOrdenadas) { partida ->
            val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
            val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"
            
            // Correção: Gols são Int?, então verificamos null em vez de isEmpty()
            val placarTexto = if (partida.golsMandante == null && partida.golsVisitante == null) {
                " VS "
            } else {
                " ${partida.golsMandante ?: 0} x ${partida.golsVisitante ?: 0} "
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onPartidaClick(partida) }
            ) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                    Text("LOCAL: ${partida.local}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(mandante, Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                        Text(placarTexto, Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.Black, color = if (partida.finalizada) Color.Blue else Color.Gray)
                        Text(visitante, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }
                    if (partida.finalizada) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFB79400), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Melhor: ${partida.melhorJogador.ifBlank { "Não definido" }}", fontSize = 10.sp, color = Color(0xFFB79400), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
