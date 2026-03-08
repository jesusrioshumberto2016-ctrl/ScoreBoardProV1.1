package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
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
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { 
                            Text("ABRIR SÚMULA") 
                        }
                    }
                }
            }
        }
    } else {
        val idx = partidas.indexOfFirst { it.id == partidaFocadaId }

        if (idx != -1) {
            ConteudoRegistrarEventos(
                p = partidas[idx],
                idxPartida = idx,
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
    idxPartida: Int,
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

    var showDialogSubstituicaoEntrada by remember { mutableStateOf(false) }
    var jogadorSaindo by remember { mutableStateOf<JogadorExemplo?>(null) }

    var showDialogPenaltis by remember { mutableStateOf(false) }
    var penMandanteInput by remember { mutableStateOf(p.penaltisMandante?.toString() ?: "") }
    var penVisitanteInput by remember { mutableStateOf(p.penaltisVisitante?.toString() ?: "") }

    // DIÁLOGO DE PÊNALTIS
    if (showDialogPenaltis) {
        AlertDialog(
            onDismissRequest = { showDialogPenaltis = false },
            title = { Text("Disputa de Pênaltis") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = penMandanteInput,
                            onValueChange = { penMandanteInput = it.filter { c -> c.isDigit() } },
                            label = { Text(mNome.take(5)) },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text("X", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = penVisitanteInput,
                            onValueChange = { penVisitanteInput = it.filter { c -> c.isDigit() } },
                            label = { Text(vNome.take(5)) },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text("Registrar Cobrança Individual:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = { tipoAtual = "CONVERTEU PÊNALTI"; showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f).padding(2.dp)
                        ) { Text("CONVERTEU", fontSize = 9.sp) }
                        
                        Button(
                            onClick = { tipoAtual = "PERDEU PÊNALTI"; showDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f).padding(2.dp)
                        ) { Text("PERDEU", fontSize = 10.sp) }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val pM = penMandanteInput.toIntOrNull()
                    val pV = penVisitanteInput.toIntOrNull()
                    partidas[idxPartida] = p.copy(penaltisMandante = pM, penaltisVisitante = pV)
                    showDialogPenaltis = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("SALVAR PLACAR") }
            }
        )
    }

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

    // DIÁLOGO DE SELEÇÃO DE JOGADOR
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
                            partidas[idxPartida] = partidas[idxPartida].copy(melhorJogador = jog.nome)
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
        val penStr = if (p.penaltisMandante != null) " (Pen: ${p.penaltisMandante} x ${p.penaltisVisitante})" else ""
        Text("Placar: $pM x $pV$penStr", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val btnMod = Modifier.weight(1f)
            val addMarco = { tipo: String ->
                val marco = EventoPartida(jogadorNome = "Partida", equipeNome = "Geral", tipo = tipo)
                partidas[idxPartida] = p.copy(eventos = p.eventos + marco)
            }
            Button(onClick = { addMarco("INÍCIO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("INÍCIO", fontSize = 9.sp) }
            Button(onClick = { addMarco("INTERVALO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("INTERVALO", fontSize = 8.sp) }
            Button(onClick = { addMarco("FIM DE JOGO") }, modifier = btnMod, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("FIM", fontSize = 9.sp) }
        }

        val ehMataMata = !p.fase.contains("RODADA", ignoreCase = true) && !p.fase.contains("ÚNICA", ignoreCase = true)
        if (ehMataMata) {
            Button(
                onClick = { showDialogPenaltis = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))
            ) {
                Text("DISPUTA DE PÊNALTIS", fontWeight = FontWeight.Bold)
            }
        }

        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                Spacer(Modifier.width(8.dp))
                Text("Melhor: ${p.melhorJogador.ifBlank { "Não definido" }}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
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
                ev.tipo.contains("CONVERTEU") -> Color(0xFF2E7D32)
                ev.tipo.contains("PERDEU") -> Color.Red
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
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onVoltar, 
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Icon(Icons.Default.Save, null)
            Spacer(Modifier.width(8.dp))
            Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(8.dp))
        
        TextButton(
            onClick = onVoltar, 
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text("VOLTAR / SAIR")
        }
    }
}

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

        // --- LÓGICA DE CONTABILIZAÇÃO DE GOLS ---
        // Apenas GOL e GOL (PÊNALTI) somam no placar principal e artilharia
        // CONVERTEU PÊNALTI e PERDEU PÊNALTI são apenas registros históricos para a disputa final
        if (tipo == "GOL" || tipo == "GOL (PÊNALTI)") {
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
