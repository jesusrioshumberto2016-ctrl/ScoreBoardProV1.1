package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SumulaTab(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>
) {
    var partidaFocadaId by remember { mutableStateOf<Int?>(null) }

    if (partidaFocadaId == null) {
        LazyColumn(Modifier.padding(16.dp)) {
            items(partidas) { p ->
                val mandante = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
                val visitante = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$mandante vs $visitante", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Button(onClick = { partidaFocadaId = p.id }) { Text("ABRIR SÚMULA") }
                    }
                }
            }
        }
    } else {
        val partidaAtualizada = partidas.find { it.id == partidaFocadaId }

        if (partidaAtualizada != null) {
            ConteudoRegistrarEventos(
                p = partidaAtualizada,
                partidas = partidas,
                equipes = equipes,
                todosJogadores = todosJogadores,
                onVoltar = { partidaFocadaId = null }
            )
        } else {
            partidaFocadaId = null
        }
    }
}

@Composable
fun ConteudoRegistrarEventos(
    p: Partida,
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    val mNome = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
    val vNome = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
    val relacionadosIds = p.titularesMandante + p.reservasMandante + p.titularesVisitante + p.reservasVisitante
    val jogadoresDaPartida = todosJogadores.filter { it.id in relacionadosIds || it.equipeId == p.mandanteId || it.equipeId == p.visitanteId }

    var showDialog by remember { mutableStateOf(false) }
    var tipoAtual by remember { mutableStateOf("GOL") }
    var showDialogMelhor by remember { mutableStateOf(false) }

    var showDialogMinuto by remember { mutableStateOf(false) }
    var minutoInput by remember { mutableStateOf("") }
    var jogadorPendenteParaEvento by remember { mutableStateOf<JogadorExemplo?>(null) }

    if (showDialogMinuto) {
        AlertDialog(
            onDismissRequest = { showDialogMinuto = false },
            title = { Text("Registrar Minuto") },
            text = {
                OutlinedTextField(
                    value = minutoInput,
                    onValueChange = { minutoInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Minuto do Evento") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val jog = jogadorPendenteParaEvento
                    val idx = partidas.indexOfFirst { it.id == p.id }
                    if (idx != -1 && jog != null) {
                        var pEd = partidas[idx]
                        val nomeEq = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"

                        var novosGolsMandante = pEd.golsMandante
                        var novosGolsVisitante = pEd.golsVisitante

                        if (tipoAtual == "GOL" || tipoAtual == "CONVERTEU PÊNALTI") {
                            if (jog.equipeId == pEd.mandanteId) {
                                novosGolsMandante = (novosGolsMandante ?: 0) + 1
                            } else {
                                novosGolsVisitante = (novosGolsVisitante ?: 0) + 1
                            }
                        } else if (tipoAtual == "GOL CONTRA") {
                            if (jog.equipeId == pEd.mandanteId) {
                                novosGolsVisitante = (novosGolsVisitante ?: 0) + 1
                            } else {
                                novosGolsMandante = (novosGolsMandante ?: 0) + 1
                            }
                        }

                        val novo = EventoPartida(jogadorNome = jog.nome, equipeNome = nomeEq, tipo = tipoAtual, minuto = minutoInput)
                        var listaEventos = pEd.eventos + novo

                        if (tipoAtual == "YELLOW CARD") {
                            val jaTem = pEd.eventos.any { it.jogadorNome == jog.nome && it.tipo == "YELLOW CARD" }
                            if (jaTem) listaEventos = listaEventos + EventoPartida(jogadorNome = jog.nome, equipeNome = nomeEq, tipo = "RED CARD", minuto = minutoInput)
                        }

                        partidas[idx] = pEd.copy(
                            eventos = listaEventos,
                            golsMandante = novosGolsMandante,
                            golsVisitante = novosGolsVisitante
                        )
                    }
                    showDialogMinuto = false
                    minutoInput = ""
                    jogadorPendenteParaEvento = null
                }) { Text("CONFIRMAR") }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registrar $tipoAtual") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    val listaFiltrada = if (tipoAtual == "PÊNALTI DEFENDIDO") {
                        jogadoresDaPartida.filter { it.posicao == "GOL" }
                    } else {
                        jogadoresDaPartida
                    }

                    if (listaFiltrada.isEmpty()) {
                        Text("Nenhum jogador apto para esta ação.", Modifier.padding(8.dp), color = Color.Gray)
                    } else {
                        listaFiltrada.forEach { jog ->
                            val nomeEquipeJogador = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"
                            TextButton(onClick = {
                                jogadorPendenteParaEvento = jog
                                showDialog = false
                                showDialogMinuto = true
                            }) {
                                Text("${jog.nome} (${jog.posicao} - $nomeEquipeJogador)")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("FECHAR") } }
        )
    }

    if (showDialogMelhor) {
        AlertDialog(
            onDismissRequest = { showDialogMelhor = false },
            title = { Text("Eleger Melhor Jogador") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    jogadoresDaPartida.forEach { jog ->
                        TextButton(onClick = {
                            val idx = partidas.indexOfFirst { it.id == p.id }
                            if (idx != -1) {
                                partidas[idx] = partidas[idx].copy(melhorJogador = jog.nome)
                            }
                            showDialogMelhor = false
                        }) {
                            Text(jog.nome)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDialogMelhor = false }) { Text("CANCELAR") } }
        )
    }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("SÚMULA: $mNome x $vNome", fontSize = 18.sp, fontWeight = FontWeight.Black)
        val pM = if(p.golsMandante == null) "-" else p.golsMandante.toString()
        val pV = if(p.golsVisitante == null) "-" else p.golsVisitante.toString()
        Text("Placar: $pM x $pV", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val btnMod = Modifier.weight(1f)
            val addMarco = { tipo: String ->
                val idx = partidas.indexOfFirst { it.id == p.id }
                if (idx != -1) {
                    val marco = EventoPartida(jogadorNome = "Partida", equipeNome = "Geral", tipo = tipo)
                    partidas[idx] = partidas[idx].copy(eventos = partidas[idx].eventos + marco)
                }
            }

            Button(onClick = { addMarco("INÍCIO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("INÍCIO", fontSize = 9.sp) }
            Button(onClick = { addMarco("INTERVALO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("INTERVALO", fontSize = 8.sp) }
            Button(onClick = { addMarco("FIM DE JOGO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("FIM", fontSize = 9.sp) }
        }

        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                Spacer(Modifier.width(8.dp))
                Text("Melhor da Partida: ${p.melhorJogador.ifBlank { "Não definido" }}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Button(onClick = { showDialogMelhor = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB79400))) {
                    Text("DEFINIR", fontSize = 10.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tipoAtual = "GOL"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("GOL", fontSize = 10.sp) }
                Button(onClick = { tipoAtual = "ASSISTÊNCIA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))) { Text("ASSIST.", fontSize = 10.sp) }
                Button(onClick = { tipoAtual = "YELLOW CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)) { Text("YELLOW", fontSize = 8.sp) }
                Button(onClick = { tipoAtual = "RED CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RED", fontSize = 8.sp) }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("HISTÓRICO DE EVENTOS:", fontWeight = FontWeight.Bold)
        Divider()

        p.eventos.forEach { ev ->
            val cor = when(ev.tipo) {
                "YELLOW CARD" -> Color(0xFFB79400)
                "RED CARD" -> Color.Red
                "GOL CONTRA" -> Color(0xFFD32F2F)
                "ASSISTÊNCIA" -> Color(0xFF00ACC1)
                "INÍCIO", "INTERVALO", "FIM DE JOGO" -> Color.Black
                else -> Color(0xFF2E7D32)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val minStr = if(ev.minuto.isNotBlank()) "${ev.minuto}' " else ""
                Text("$minStr${ev.tipo}: ${ev.jogadorNome}", color = cor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val idx = partidas.indexOfFirst { it.id == p.id }
                    if (idx != -1) {
                        var pEd = partidas[idx]
                        var novosGolsMandante = pEd.golsMandante
                        var novosGolsVisitante = pEd.golsVisitante

                        if (ev.tipo == "GOL" || ev.tipo == "CONVERTEU PÊNALTI") {
                            val jog = todosJogadores.find { it.nome == ev.jogadorNome }
                            if (jog != null) {
                                if (jog.equipeId == pEd.mandanteId) {
                                    novosGolsMandante = (novosGolsMandante ?: 0) - 1
                                } else {
                                    novosGolsVisitante = (novosGolsVisitante ?: 0) - 1
                                }
                            }
                        } else if (ev.tipo == "GOL CONTRA") {
                            val jog = todosJogadores.find { it.nome == ev.jogadorNome }
                            if (jog != null) {
                                if (jog.equipeId == pEd.mandanteId) {
                                    novosGolsVisitante = (novosGolsVisitante ?: 0) - 1
                                } else {
                                    novosGolsMandante = (novosGolsMandante ?: 0) - 1
                                }
                            }
                        }

                        val novaLista = pEd.eventos.toMutableList().apply { remove(ev) }
                        partidas[idx] = pEd.copy(
                            eventos = novaLista,
                            golsMandante = if ((novosGolsMandante ?: 0) < 0) 0 else novosGolsMandante,
                            golsVisitante = if ((novosGolsVisitante ?: 0) < 0) 0 else novosGolsVisitante
                        )
                    }
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
    }
}
