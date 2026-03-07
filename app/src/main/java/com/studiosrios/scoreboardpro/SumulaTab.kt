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
import androidx.compose.material.icons.filled.SyncAlt
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
    todosJogadores: List<JogadorExemplo>,
    onEntrarEdicao: (Int) -> Unit = {},
    onSairEdicao: () -> Unit = {}
) {
    var partidaFocadaId by remember { mutableStateOf<Int?>(null) }

    if (partidaFocadaId == null) {
        val partidasOrdenadas = obterPartidasOrdenadas(partidas)

        LazyColumn(Modifier.padding(16.dp)) {
            items(partidasOrdenadas, key = { it.id }) { p ->
                val mandante = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
                val visitante = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = p.fase.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text("$mandante vs $visitante", fontWeight = FontWeight.Bold)
                            Text("${p.data} - ${p.horario}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Button(onClick = { 
                            partidaFocadaId = p.id 
                            onEntrarEdicao(p.id)
                        }) { Text("ABRIR SÚMULA") }
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
                onVoltar = { 
                    partidaFocadaId = null 
                    onSairEdicao()
                }
            )
        } else {
            partidaFocadaId = null
            onSairEdicao()
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
    val mNome = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
    val vNome = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
    val relacionadosIds = p.titularesMandante + p.reservasMandante + p.titularesVisitante + p.reservasVisitante
    val jogadoresDaPartida = todosJogadores.filter { it.id in relacionadosIds || it.equipeId == p.mandanteId || it.equipeId == p.visitanteId }

    var showDialog by remember { mutableStateOf(false) }
    var tipoAtual by remember { mutableStateOf("GOL") }
    var showDialogMelhor by remember { mutableStateOf(false) }

    var showDialogMinuto by remember { mutableStateOf(false) }
    var minutoInput by remember { mutableStateOf("") }
    var jogadorPendenteParaEvento by remember { mutableStateOf<JogadorExemplo?>(null) }

    var showDialogAssistencia by remember { mutableStateOf(false) }
    var jogadorDoGol by remember { mutableStateOf<JogadorExemplo?>(null) }
    var minutoDoGol by remember { mutableStateOf("") }
    var tipoDoGol by remember { mutableStateOf("") }

    // Estado para Substituição
    var showDialogSubstituicaoEntrada by remember { mutableStateOf(false) }
    var jogadorSaindo by remember { mutableStateOf<JogadorExemplo?>(null) }

    // DIÁLOGO DE MINUTO
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
                    if (jog != null) {
                        if (tipoAtual == "GOL" || tipoAtual == "GOL (PÊNALTI)") {
                            jogadorDoGol = jog
                            minutoDoGol = minutoInput
                            tipoDoGol = tipoAtual
                            showDialogMinuto = false
                            showDialogAssistencia = true
                        } else if (tipoAtual == "SAÍDA (SUB)") {
                            jogadorSaindo = jog
                            showDialogMinuto = false
                            showDialogSubstituicaoEntrada = true
                        } else {
                            // CORREÇÃO: Função salvarEvento movida para baixo para ser visível
                            salvarEventoImpl(p, partidas, equipes, jog, tipoAtual, minutoInput)
                            showDialogMinuto = false
                            minutoInput = ""
                            jogadorPendenteParaEvento = null
                        }
                    }
                }) { Text("PRÓXIMO") }
            }
        )
    }

    // DIÁLOGO DE ASSISTÊNCIA
    if (showDialogAssistencia) {
        AlertDialog(
            onDismissRequest = { showDialogAssistencia = false },
            title = { Text("Quem deu a assistência?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TextButton(onClick = {
                        val author = jogadorDoGol
                        if (author != null) {
                            salvarEventoImpl(p, partidas, equipes, author, tipoDoGol, minutoDoGol)
                        }
                        showDialogAssistencia = false
                        minutoInput = ""
                        jogadorPendenteParaEvento = null
                    }) { Text("SEM ASSISTÊNCIA", fontWeight = FontWeight.Bold, color = Color.Gray) }
                    HorizontalDivider()
                    val companheiros = jogadoresDaPartida.filter { it.equipeId == jogadorDoGol?.equipeId && it.id != jogadorDoGol?.id }
                    companheiros.forEach { comp ->
                        TextButton(onClick = {
                            val author = jogadorDoGol
                            if (author != null) {
                                salvarEventoImpl(p, partidas, equipes, author, tipoDoGol, minutoDoGol)
                                salvarEventoImpl(p, partidas, equipes, comp, "ASSISTÊNCIA", minutoDoGol)
                            }
                            showDialogAssistencia = false
                            minutoInput = ""
                            jogadorPendenteParaEvento = null
                        }) { Text(comp.nome) }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // DIÁLOGO DE ENTRADA (SUBSTITUIÇÃO)
    if (showDialogSubstituicaoEntrada) {
        AlertDialog(
            onDismissRequest = { showDialogSubstituicaoEntrada = false },
            title = { Text("Quem entra no lugar de ${jogadorSaindo?.nome}?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    val reservas = jogadoresDaPartida.filter { it.equipeId == jogadorSaindo?.equipeId && it.id != jogadorSaindo?.id }
                    reservas.forEach { reserva ->
                        TextButton(onClick = {
                            val msg = "SUB: Sai ${jogadorSaindo?.nome} / Entra ${reserva.nome}"
                            salvarEventoImpl(p, partidas, equipes, jogadorSaindo!!, msg, minutoInput)
                            showDialogSubstituicaoEntrada = false
                            minutoInput = ""
                            jogadorPendenteParaEvento = null
                            jogadorSaindo = null
                        }) { Text(reserva.nome) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDialogSubstituicaoEntrada = false }) { Text("CANCELAR") } }
        )
    }

    // DIÁLOGO DE SELEÇÃO DE JOGADOR (BOTÕES PRINCIPAIS)
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
                        Text("Nenhum jogador apto.", Modifier.padding(8.dp), color = Color.Gray)
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

    // ELEGER MELHOR JOGADOR
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
                        }) { Text(jog.nome) }
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
                Button(onClick = { tipoAtual = "GOL (PÊNALTI)"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) { Text("PÊNALTI", fontSize = 9.sp) }
                Button(onClick = { tipoAtual = "ASSISTÊNCIA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))) { Text("ASSIST.", fontSize = 10.sp) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tipoAtual = "YELLOW CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)) { Text("YELLOW", fontSize = 8.sp) }
                Button(onClick = { tipoAtual = "RED CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RED", fontSize = 8.sp) }
                Button(onClick = { tipoAtual = "GOL CONTRA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("GOL CONTRA", fontSize = 8.sp) }
            }
            Button(
                onClick = { tipoAtual = "SAÍDA (SUB)"; showDialog = true }, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.Default.SyncAlt, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("REGISTRAR SUBSTITUIÇÃO", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("HISTÓRICO DE EVENTOS:", fontWeight = FontWeight.Bold)
        HorizontalDivider()

        p.eventos.forEach { ev ->
            val cor = when {
                ev.tipo.contains("SUB") -> Color.Gray
                ev.tipo == "YELLOW CARD" -> Color(0xFFB79400)
                ev.tipo == "RED CARD" -> Color.Red
                ev.tipo == "GOL CONTRA" -> Color(0xFFD32F2F)
                else -> Color(0xFF2E7D32)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val minStr = if(ev.minuto.isNotBlank()) "${ev.minuto}' " else ""
                Text("$minStr${ev.tipo}: ${ev.jogadorNome}", color = cor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), fontSize = 12.sp)
                IconButton(onClick = {
                    val idxPartida = partidas.indexOfFirst { it.id == p.id }
                    if (idxPartida != -1) {
                        val pEd = partidas[idxPartida]
                        var novosGolsMandante = pEd.golsMandante
                        var novosGolsVisitante = pEd.golsVisitante

                        if (ev.tipo == "GOL" || ev.tipo == "GOL (PÊNALTI)" || ev.tipo == "CONVERTEU PÊNALTI") {
                            val jog = todosJogadores.find { it.nome == ev.jogadorNome }
                            if (jog != null) {
                                if (jog.equipeId == pEd.mandanteId) novosGolsMandante = (novosGolsMandante ?: 0) - 1
                                else novosGolsVisitante = (novosGolsVisitante ?: 0) - 1
                            }
                        } else if (ev.tipo == "GOL CONTRA") {
                            val jog = todosJogadores.find { it.nome == ev.jogadorNome }
                            if (jog != null) {
                                if (jog.equipeId == pEd.mandanteId) novosGolsVisitante = (novosGolsVisitante ?: 0) - 1
                                else novosGolsMandante = (novosGolsMandante ?: 0) - 1
                            }
                        }

                        val novaLista = pEd.eventos.toMutableList().apply { remove(ev) }
                        partidas[idxPartida] = pEd.copy(
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

// RENOMEADO PARA EVITAR CONFLITO E MOVIDO PARA O TOPO DA VISIBILIDADE
fun salvarEventoImpl(
    p: Partida,
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    jog: JogadorExemplo,
    tipo: String,
    minuto: String
) {
    val idx = partidas.indexOfFirst { it.id == p.id }
    if (idx != -1) {
        val pEd = partidas[idx]
        val nomeEq = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"

        var novosGolsMandante = pEd.golsMandante
        var novosGolsVisitante = pEd.golsVisitante

        if (tipo == "GOL" || tipo == "GOL (PÊNALTI)" || tipo == "CONVERTEU PÊNALTI") {
            if (jog.equipeId == pEd.mandanteId) novosGolsMandante = (novosGolsMandante ?: 0) + 1
            else novosGolsVisitante = (novosGolsVisitante ?: 0) + 1
        } else if (tipo == "GOL CONTRA") {
            if (jog.equipeId == pEd.mandanteId) novosGolsVisitante = (novosGolsVisitante ?: 0) + 1
            else novosGolsMandante = (novosGolsMandante ?: 0) + 1
        }

        val novo = EventoPartida(jogadorNome = jog.nome, equipeNome = nomeEq, tipo = tipo, minuto = minuto)
        val listaEventos = pEd.eventos + novo

        partidas[idx] = pEd.copy(
            eventos = listaEventos,
            golsMandante = novosGolsMandante,
            golsVisitante = novosGolsVisitante
        )
    }
}
