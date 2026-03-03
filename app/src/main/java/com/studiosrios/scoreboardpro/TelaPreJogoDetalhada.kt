package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun TelaPreJogoDetalhada(
    partida: Partida,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    val mandante = equipes.find { it.id == partida.mandanteId }
    val visitante = equipes.find { it.id == partida.visitanteId }

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Text("INFORMAÇÕES PRÉ-JOGO", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))

        // Card de Informações Gerais (Árbitros)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("EQUIPE DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Divider(Modifier.padding(vertical = 8.dp))
                Text("Árbitro: ${partida.arbitroPrincipal.ifBlank { "A definir" }}")
                Text("Assistentes: ${partida.assistente1.ifBlank { "-" }} / ${partida.assistente2.ifBlank { "-" }}")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Exibição das Escalações
        Row(Modifier.fillMaxWidth().weight(1f)) {
            // Coluna Mandante
            Column(Modifier.weight(1f).padding(4.dp)) {
                Text(mandante?.nome ?: "Mandante", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("Técnico: ${partida.tecnicoMandante.ifBlank { "-" }}", fontSize = 10.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("TITULARES", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                LazyColumn {
                    items(partida.titularesMandante) { id ->
                        val jog = todosJogadores.find { it.id == id }
                        Text("• ${jog?.nome ?: "Desconhecido"}", fontSize = 11.sp)
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

            // Coluna Visitante
            Column(Modifier.weight(1f).padding(4.dp)) {
                Text(visitante?.nome ?: "Visitante", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text("Técnico: ${partida.tecnicoVisitante.ifBlank { "-" }}", fontSize = 10.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("TITULARES", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                LazyColumn {
                    items(partida.titularesVisitante) { id ->
                        val jog = todosJogadores.find { it.id == id }
                        Text("• ${jog?.nome ?: "Desconhecido"}", fontSize = 11.sp)
                    }
                }
            }
        }

        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("VOLTAR PARA PARTIDAS")
        }
    }
}
