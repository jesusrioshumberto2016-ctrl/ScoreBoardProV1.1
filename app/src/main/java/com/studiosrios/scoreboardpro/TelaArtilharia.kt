package com.studiosrios.scoreboardpro // 'p' minúsculo corrigido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaArtilharia(
    equipes: List<EquipeExemplo>,
    listaGlobalJogadores: List<JogadorExemplo>
) {
    // Lógica inteligente: usamos a listaGlobalJogadores que já recebemos
    // Assim não precisamos mexer na classe EquipeExemplo
    val artilheiros = listaGlobalJogadores
        .filter { it.gols > 0 }
        .sortedByDescending { it.gols }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Artilharia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (artilheiros.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum gol registrado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(artilheiros) { jogador ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = jogador.nome,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                // Busca o nome da equipe do jogador
                                val nomeEquipe = equipes.find { it.id == jogador.equipeId }?.nome ?: "Sem Equipe"
                                Text(
                                    text = nomeEquipe,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "${jogador.gols} Gols",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
