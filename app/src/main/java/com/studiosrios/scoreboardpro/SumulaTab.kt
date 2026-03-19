package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SumulaTab(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onEntrarEdicao: (Int) -> Unit = {},
    onSairEdicao: () -> Unit = {},
    onAlteracao: () -> Unit = {},
    onSalvar: () -> Unit = {}
) {
    var partidaFocadaId by remember { mutableStateOf<Int?>(null) }

    if (partidaFocadaId == null) {
        val partidasOrdenadas = obterPartidasOrdenadas(partidas)

        LazyColumn(Modifier.padding(16.dp)) {
            items(partidasOrdenadas, key = { it.id }) { p: Partida ->
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
                },
                onAlteracao = onAlteracao,
                onSalvar = onSalvar
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
    onVoltar: () -> Unit,
    onAlteracao: () -> Unit = {},
    onSalvar: () -> Unit = {}
) {
    val mNome = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
    val vNome = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
    
    // FILTRAR APENAS JOGADORES RELACIONADOS (TITULARES OU RESERVAS)
    val relacionadosIds = p.titularesMandante + p.reservasMandante + p.titularesVisitante + p.reservasVisitante
    val jogadoresDaPartida = todosJogadores.filter { it.id in relacionadosIds }

    var houveMudancaLocal by remember { mutableStateOf(false) }
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }

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

    var jogadorParaVerAcoes by remember { mutableStateOf<JogadorExemplo?>(null) }

    val tentarVoltarLocal = {
        if (houveMudancaLocal) {
            mostrarConfirmacaoSair = true
        } else {
            onVoltar()
        }
    }

    BackHandler {
        tentarVoltarLocal()
    }

    if (mostrarConfirmacaoSair) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoSair = false },
            title = { Text("Alterações pendentes") },
            text = { Text("Você realizou alterações nesta súmula. Deseja sair sem salvar?") },
            confirmButton = {
                Button(onClick = { mostrarConfirmacaoSair = false; onVoltar() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("SAIR SEM SALVAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoSair = false }) { Text("CONTINUAR") }
            }
        )
    }

    if (jogadorParaVerAcoes != null) {
        val acoes = p.eventos.filter { it.jogadorNome == jogadorParaVerAcoes?.nome }
        AlertDialog(
            onDismissRequest = { jogadorParaVerAcoes = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = jogadorParaVerAcoes!!.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(jogadorParaVerAcoes!!.apelido.ifBlank { jogadorParaVerAcoes!!.nome }, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        Button(onClick = { tipoAtual = "CONVERTEU PÊNALTI"; showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), modifier = Modifier.weight(1f).padding(2.dp)) { Text("CONVERTEU", fontSize = 9.sp) }
                        Button(onClick = { tipoAtual = "PERDEU PÊNALTI"; showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f).padding(2.dp)) { Text("PERDEU", fontSize = 10.sp) }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val pM = penMandanteInput.toIntOrNull()
                    val pV = penVisitanteInput.toIntOrNull()
                    partidas[idxPartida] = p.copy(penaltisMandante = pM, penaltisVisitante = pV)
                    houveMudancaLocal = true; onAlteracao(); showDialogPenaltis = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("SALVAR PLACAR") }
            }
        )
    }

    if (showDialogMinuto) {
        AlertDialog(
            onDismissRequest = { showDialogMinuto = false },
            title = { Text("Registrar Minuto") },
            text = {
                OutlinedTextField(value = minutoInput, onValueChange = { minutoInput = it.filter { c -> c.isDigit() } }, label = { Text("Minuto") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    val jog = jogadorPendenteParaEvento
                    if (jog != null) {
                        if (tipoAtual == "GOL" || tipoAtual == "GOL (PÊNALTI)") {
                            jogadorDoGol = jog; minutoDoGol = minutoInput; tipoDoGol = tipoAtual; showDialogMinuto = false; showDialogAssistencia = true
                        } else if (tipoAtual == "SAÍDA (SUB)") {
                            jogadorSaindo = jog; showDialogMinuto = false; showDialogSubstituicaoEntrada = true
                        } else {
                            salvarEventoImpl(p, partidas, equipes, jog, tipoAtual, minutoInput)
                            houveMudancaLocal = true; onAlteracao(); showDialogMinuto = false; minutoInput = ""; jogadorPendenteParaEvento = null
                        }
                    }
                }) { Text("PRÓXIMO") }
            }
        )
    }

    if (showDialogAssistencia) {
        AlertDialog(
            onDismissRequest = { showDialogAssistencia = false },
            title = { Text("Quem deu a assistência?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TextButton(onClick = {
                        jogadorDoGol?.let { salvarEventoImpl(p, partidas, equipes, it, tipoDoGol, minutoDoGol) }
                        houveMudancaLocal = true; onAlteracao(); showDialogAssistencia = false; minutoInput = ""; jogadorPendenteParaEvento = null
                    }) { Text("SEM ASSISTÊNCIA") }
                    val companheiros = jogadoresDaPartida.filter { it.equipeId == jogadorDoGol?.equipeId && it.id != jogadorDoGol?.id }
                    companheiros.forEach { comp ->
                        TextButton(onClick = {
                            jogadorDoGol?.let { salvarEventoImpl(p, partidas, equipes, it, tipoDoGol, minutoDoGol) }
                            salvarEventoImpl(p, partidas, equipes, comp, "ASSISTÊNCIA", minutoDoGol)
                            houveMudancaLocal = true; onAlteracao(); showDialogAssistencia = false; minutoInput = ""; jogadorPendenteParaEvento = null
                        }) { Text(comp.nome) }
                    }
                }
            }, confirmButton = {}
        )
    }

    if (showDialogSubstituicaoEntrada) {
        AlertDialog(
            onDismissRequest = { showDialogSubstituicaoEntrada = false },
            title = { Text("Quem entra?") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    val reservas = jogadoresDaPartida.filter { it.equipeId == jogadorSaindo?.equipeId && it.id != jogadorSaindo?.id }
                    reservas.forEach { reserva ->
                        TextButton(onClick = {
                            val min = minutoInput
                            val sai = jogadorSaindo
                            val entra = reserva
                            if (sai != null) {
                                salvarEventoImpl(p, partidas, equipes, sai, "SAÍDA (SUB)", min)
                                salvarEventoImpl(p, partidas, equipes, entra, "ENTRADA (SUB)", min)
                            }
                            houveMudancaLocal = true; onAlteracao(); showDialogSubstituicaoEntrada = false; minutoInput = ""; jogadorPendenteParaEvento = null; jogadorSaindo = null
                        }) { Text(reserva.nome) }
                    }
                }
            }, confirmButton = {}
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(tipoAtual) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    jogadoresDaPartida.forEach { jog ->
                        TextButton(onClick = { jogadorPendenteParaEvento = jog; showDialog = false; showDialogMinuto = true }) { Text("${jog.nome} (${jog.posicao})") }
                    }
                }
            }, confirmButton = {}
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
                            partidas[idxPartida] = partidas[idxPartida].copy(melhorJogador = jog.nome)
                            houveMudancaLocal = true; onAlteracao(); showDialogMelhor = false
                        }) { Text(jog.nome) }
                    }
                }
            }, confirmButton = {}
        )
    }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = tentarVoltarLocal) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
            Text("SÚMULA: $mNome x $vNome", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val addMarco = { tipo: String ->
                val marco = EventoPartida(jogadorNome = "Partida", equipeNome = "Geral", tipo = tipo)
                val isFim = tipo == "FIM DE JOGO"
                partidas[idxPartida] = p.copy(
                    eventos = p.eventos + marco,
                    finalizada = if (isFim) true else p.finalizada
                )
                houveMudancaLocal = true; onAlteracao()
            }
            Button(onClick = { addMarco("INÍCIO") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("INÍCIO", fontSize = 9.sp) }
            Button(onClick = { addMarco("INTERVALO") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("INTERVALO", fontSize = 8.sp) }
            Button(onClick = { addMarco("FIM DE JOGO") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("FIM", fontSize = 9.sp) }
        }

        if (!p.fase.contains("RODADA", true)) {
            Button(onClick = { showDialogPenaltis = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))) { Text("DISPUTA DE PÊNALTIS") }
        }

        Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                Spacer(Modifier.width(8.dp))
                Text("Melhor: ${p.melhorJogador.ifBlank { "Não definido" }}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Button(onClick = { showDialogMelhor = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB79400))) { Text("DEFINIR", fontSize = 10.sp) }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tipoAtual = "GOL"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("GOL") }
                Button(onClick = { tipoAtual = "GOL (PÊNALTI)"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) { Text("PÊNALTI") }
                Button(onClick = { tipoAtual = "ASSISTÊNCIA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))) { Text("ASSIST.") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { tipoAtual = "YELLOW CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)) { Text("AMARELO") }
                Button(onClick = { tipoAtual = "RED CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("VERMELHO") }
                Button(onClick = { tipoAtual = "GOL CONTRA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("CONTRA") }
            }
            Button(onClick = { tipoAtual = "SAÍDA (SUB)"; showDialog = true }, modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Icon(Icons.Default.SyncAlt, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("REGISTRAR SUBSTITUIÇÃO", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("HISTÓRICO DE EVENTOS:", fontWeight = FontWeight.Bold)
        p.eventos.forEach { ev ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { 
                        val jog = jogadoresDaPartida.find { it.nome == ev.jogadorNome }
                        if (jog != null) jogadorParaVerAcoes = jog
                    }, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minStr = if(ev.minuto.isNotBlank()) "${ev.minuto}' " else ""
                Text("$minStr${ev.tipo}: ${ev.jogadorNome}", color = Color.DarkGray, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), fontSize = 12.sp)
                IconButton(onClick = {
                    val pEd = partidas[idxPartida]
                    var nGM = pEd.golsMandante; var nGV = pEd.golsVisitante
                    var nAM = pEd.cartoesAmarelosMandante; var nVM = pEd.cartoesVermelhosMandante
                    var nAV = pEd.cartoesAmarelosVisitante; var nVV = pEd.cartoesVermelhosVisitante

                    val jog = todosJogadores.find { it.nome == ev.jogadorNome }
                    if (jog != null) {
                        if (ev.tipo == "GOL" || ev.tipo == "GOL (PÊNALTI)" || ev.tipo == "CONVERTEU PÊNALTI") {
                            if (jog.equipeId == pEd.mandanteId) nGM = (nGM ?: 0) - 1
                            else nGV = (nGV ?: 0) - 1
                        } else if (ev.tipo == "GOL CONTRA") {
                            if (jog.equipeId == pEd.mandanteId) nGV = (nGV ?: 0) - 1
                            else nGM = (nGM ?: 0) - 1
                        } else if (ev.tipo == "YELLOW CARD") {
                            if (jog.equipeId == pEd.mandanteId) nAM--
                            else nAV--
                        } else if (ev.tipo == "RED CARD") {
                            if (jog.equipeId == pEd.mandanteId) nVM--
                            else nVV--
                        }
                    }

                    val isFim = ev.tipo == "FIM DE JOGO"
                    val novaLista = pEd.eventos.toMutableList().apply { remove(ev) }
                    partidas[idxPartida] = pEd.copy(
                        eventos = novaLista,
                        finalizada = if (isFim) false else pEd.finalizada,
                        golsMandante = if ((nGM ?: 0) < 0) 0 else nGM,
                        golsVisitante = if ((nGV ?: 0) < 0) 0 else nGV,
                        cartoesAmarelosMandante = if (nAM < 0) 0 else nAM,
                        cartoesVermelhosMandante = if (nVM < 0) 0 else nVM,
                        cartoesAmarelosVisitante = if (nAV < 0) 0 else nAV,
                        cartoesVermelhosVisitante = if (nVV < 0) 0 else nVV
                    )
                    houveMudancaLocal = true
                    onAlteracao()
                }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { 
                onSalvar()
                houveMudancaLocal = false
                onVoltar() 
            }, 
            modifier = Modifier.fillMaxWidth().height(55.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold)
        }
    }
}

fun salvarEventoImpl(p: Partida, partidas: SnapshotStateList<Partida>, equipes: List<EquipeExemplo>, jog: JogadorExemplo, tipo: String, minuto: String) {
    val idx = partidas.indexOfFirst { it.id == p.id }
    if (idx != -1) {
        val pEd = partidas[idx]
        val nomeEq = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"
        var nGM = pEd.golsMandante; var nGV = pEd.golsVisitante
        var nAM = pEd.cartoesAmarelosMandante; var nVM = pEd.cartoesVermelhosMandante
        var nAV = pEd.cartoesAmarelosVisitante; var nVV = pEd.cartoesVermelhosVisitante

        if (tipo == "GOL" || tipo == "GOL (PÊNALTI)") {
            if (jog.equipeId == pEd.mandanteId) nGM = (nGM ?: 0) + 1 else nGV = (nGV ?: 0) + 1
        } else if (tipo == "GOL CONTRA") {
            if (jog.equipeId == pEd.mandanteId) nGV = (nGV ?: 0) + 1 else nGM = (nGM ?: 0) + 1
        } else if (tipo == "YELLOW CARD") {
            if (jog.equipeId == pEd.mandanteId) nAM++ else nAV++
        } else if (tipo == "RED CARD") {
            if (jog.equipeId == pEd.mandanteId) nVM++ else nVV++
        }

        val novo = EventoPartida(jogadorNome = jog.nome, equipeNome = nomeEq, tipo = tipo, minuto = minuto)
        partidas[idx] = pEd.copy(eventos = pEd.eventos + novo, golsMandante = nGM, golsVisitante = nGV, cartoesAmarelosMandante = nAM, cartoesVermelhosMandante = nVM, cartoesAmarelosVisitante = nAV, cartoesVermelhosVisitante = nVV)
    }
}
