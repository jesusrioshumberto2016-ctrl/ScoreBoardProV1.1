package com.studiosrios.scoreboardpro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreJogoTab(
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>
) {
    var partidaParaPreJogo by remember { mutableStateOf<Partida?>(null) }

    if (partidaParaPreJogo == null) {
        // --- TELA A: LISTA DE PARTIDAS ---
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("PRÉ-JOGO: SELECIONE A PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            LazyColumn {
                // Aqui usamos as partidas do snapshot diretamente para manter a lista atualizada
                items(partidas) { partida ->
                    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time A"
                    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time B"

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { partidaParaPreJogo = partida },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${partida.data} às ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                                Text("$mandante vs $visitante", fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    } else {
        // --- TELA B: CONFIGURAÇÃO COMPLETA ---
        val mandanteNome = equipes.find { it.id == partidaParaPreJogo?.mandanteId }?.nome ?: "Time A"
        val visitanteNome = equipes.find { it.id == partidaParaPreJogo?.visitanteId }?.nome ?: "Time B"

        // 0 = Mandante, 1 = Visitante, 2 = Arbitragem
        var opcaoSelecionada by remember { mutableIntStateOf(0) }
        val idx = partidas.indexOfFirst { it.id == partidaParaPreJogo?.id }

        if (idx != -1) {
            val p = partidas[idx]

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { partidaParaPreJogo = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                    Text("CONFIGURAR PARTIDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TabRow(selectedTabIndex = opcaoSelecionada, containerColor = Color(0xFFF5F5F5)) {
                    Tab(selected = opcaoSelecionada == 0, onClick = { opcaoSelecionada = 0 }, text = { Text(mandanteNome, fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = opcaoSelecionada == 1, onClick = { opcaoSelecionada = 1 }, text = { Text(visitanteNome, fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                    Tab(selected = opcaoSelecionada == 2, onClick = { opcaoSelecionada = 2 }, text = { Text("ARBITRAGEM", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                }

                Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    if (opcaoSelecionada == 2) {
                        // --- ABA DE ARBITRAGEM ---
                        Column(Modifier.fillMaxSize()) {
                            Text("TRIO DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Divider(Modifier.padding(vertical = 8.dp))

                            OutlinedTextField(value = p.arbitroPrincipal, onValueChange = { partidas[idx] = p.copy(arbitroPrincipal = it); partidaParaPreJogo = partidas[idx] }, label = { Text("Árbitro Principal") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = p.assistente1, onValueChange = { partidas[idx] = p.copy(assistente1 = it); partidaParaPreJogo = partidas[idx] }, label = { Text("Assistente 1") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = p.assistente2, onValueChange = { partidas[idx] = p.copy(assistente2 = it); partidaParaPreJogo = partidas[idx] }, label = { Text("Assistente 2") }, modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        // --- ABAS DOS TIMES (COMISSÃO + ESCALAÇÃO) ---
                        val isMandante = opcaoSelecionada == 0
                        val idTimeAtual = if (isMandante) p.mandanteId else p.visitanteId
                        val jogadoresDoTime = listaGlobalJogadores.filter { it.equipeId == idTimeAtual }

                        LazyColumn(Modifier.fillMaxSize()) {
                            // CABEÇALHO: COMISSÃO TÉCNICA
                            item {
                                Text("COMISSÃO TÉCNICA", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                                Divider(Modifier.padding(vertical = 8.dp))

                                OutlinedTextField(
                                    value = if (isMandante) p.tecnicoMandante else p.tecnicoVisitante,
                                    onValueChange = { v -> partidas[idx] = if (isMandante) p.copy(tecnicoMandante = v) else p.copy(tecnicoVisitante = v); partidaParaPreJogo = partidas[idx] },
                                    label = { Text("Técnico") }, modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = if (isMandante) p.auxiliar1Mandante else p.auxiliar1Visitante,
                                    onValueChange = { v -> partidas[idx] = if (isMandante) p.copy(auxiliar1Mandante = v) else p.copy(auxiliar1Visitante = v); partidaParaPreJogo = partidas[idx] },
                                    label = { Text("Auxiliar 1") }, modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = if (isMandante) p.auxiliar2Mandante else p.auxiliar2Visitante,
                                    onValueChange = { v -> partidas[idx] = if (isMandante) p.copy(auxiliar2Mandante = v) else p.copy(auxiliar2Visitante = v); partidaParaPreJogo = partidas[idx] },
                                    label = { Text("Auxiliar 2") }, modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = if (isMandante) p.massagistaMandante else p.massagistaVisitante,
                                    onValueChange = { v -> partidas[idx] = if (isMandante) p.copy(massagistaMandante = v) else p.copy(massagistaVisitante = v); partidaParaPreJogo = partidas[idx] },
                                    label = { Text("Massagista") }, modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(Modifier.height(16.dp))
                                Text("ESCALAÇÃO (TITULARES E RESERVAS)", fontWeight = FontWeight.Bold, color = Color.Gray)
                                Divider(Modifier.padding(vertical = 8.dp))
                            }

                            // LISTA DE JOGADORES (A RESTAURAÇÃO DE TIT E RES)
                            items(jogadoresDoTime) { jog ->
                                val isTitular = if (isMandante) p.titularesMandante.contains(jog.id) else p.titularesVisitante.contains(jog.id)
                                val isReserva = if (isMandante) p.reservasMandante.contains(jog.id) else p.reservasVisitante.contains(jog.id)

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = when { isTitular -> Color(0xFFE8F5E9); isReserva -> Color(0xFFFFF3E0); else -> Color.White })
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(jog.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${jog.posicao} | ${jog.idade} anos", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Row {
                                            TextButton(onClick = {
                                                partidas[idx] = if (isMandante) {
                                                    p.copy(titularesMandante = if (isTitular) p.titularesMandante - jog.id else p.titularesMandante + jog.id, reservasMandante = p.reservasMandante - jog.id)
                                                } else {
                                                    p.copy(titularesVisitante = if (isTitular) p.titularesVisitante - jog.id else p.titularesVisitante + jog.id, reservasVisitante = p.reservasVisitante - jog.id)
                                                }
                                                partidaParaPreJogo = partidas[idx]
                                            }) { Text("TIT", color = if (isTitular) Color(0xFF2E7D32) else Color.Gray) }

                                            TextButton(onClick = {
                                                partidas[idx] = if (isMandante) {
                                                    p.copy(reservasMandante = if (isReserva) p.reservasMandante - jog.id else p.reservasMandante + jog.id, titularesMandante = p.titularesMandante - jog.id)
                                                } else {
                                                    p.copy(reservasVisitante = if (isReserva) p.reservasVisitante - jog.id else p.reservasVisitante + jog.id, titularesVisitante = p.titularesVisitante - jog.id)
                                                }
                                                partidaParaPreJogo = partidas[idx]
                                            }) { Text("RES", color = if (isReserva) Color(0xFFEF6C00) else Color.Gray) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(onClick = { partidaParaPreJogo = null }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("SALVAR E VOLTAR")
                }
            }
        }
    }
}
