package com.studiosrios.scoreboardpro

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTelaConfigurarTorneio(
    idCamp: Int,
    modelo: String,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    configsAtuais: ConfiguracoesCampeonato,
    onConfigsChanged: (ConfiguracoesCampeonato) -> Unit,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit
) {
    val jaIniciou = partidas.any { it.finalizada }
    val contexto = LocalContext.current
    val totalEsperadoTurnoUnico = (equipes.size * (equipes.size - 1)) / 2

    // Opções e Títulos
    val opcoesCriterios = listOf("Selecionar", "Confronto direto", "Vitórias", "Saldo de gols", "Gols marcados", "Menos cartões amarelos", "Menos cartões vermelhos")
    val titulosCriterios = listOf("Primeiro critério", "Segundo critério", "Terceiro critério", "Quarto critério", "Quinto critério", "Sexto critério")

    // Estados dos menus (Dropdowns)
    val expandedStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("CONFIGURAÇÕES DO TORNEIO", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(24.dp))

        Text("SISTEMA DE DISPUTA", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        if (jaIniciou) {
            Surface(
                color = Color(0xFFFFEBEE),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    "O sistema de disputa não pode ser alterado pois já existem partidas finalizadas.",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !configsAtuais.modoReturno,
                onClick = {
                    if (!jaIniciou && configsAtuais.modoReturno) {
                        val listaOriginal = partidas.take(totalEsperadoTurnoUnico)
                        partidas.clear()
                        partidas.addAll(listaOriginal)
                        onConfigsChanged(configsAtuais.copy(modoReturno = false))
                    }
                },
                enabled = !jaIniciou
            )
            Text("Turno Único")
            Spacer(Modifier.width(20.dp))
            RadioButton(
                selected = configsAtuais.modoReturno,
                onClick = {
                    if (!jaIniciou && !configsAtuais.modoReturno) {
                        val returno = mutableListOf<Partida>()
                        var novoId = partidas.maxByOrNull { it.id }?.id ?: 0
                        partidas.forEach { p ->
                            returno.add(Partida(++novoId, p.visitanteId, p.mandanteId))
                        }
                        partidas.addAll(returno)
                        onConfigsChanged(configsAtuais.copy(modoReturno = true))
                    }
                },
                enabled = !jaIniciou
            )
            Text("Turno e Returno")
        }

        Spacer(Modifier.height(24.dp))
        Text("CRITÉRIOS DE DESEMPATE", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        titulosCriterios.forEachIndexed { index, titulo ->
            ExposedDropdownMenuBox(
                expanded = expandedStates[index],
                onExpandedChange = { expandedStates[index] = !expandedStates[index] },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = configsAtuais.criteriosDesempate[index],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(titulo) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStates[index]) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedStates[index],
                    onDismissRequest = { expandedStates[index] = false }
                ) {
                    opcoesCriterios.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao) },
                            onClick = {
                                val novaLista = configsAtuais.criteriosDesempate.toMutableList()
                                if (opcao != "Selecionar") {
                                    val indexAntigo = novaLista.indexOf(opcao)
                                    if (indexAntigo != -1 && indexAntigo != index) {
                                        novaLista[indexAntigo] = "Selecionar"
                                    }
                                }
                                novaLista[index] = opcao
                                onConfigsChanged(configsAtuais.copy(criteriosDesempate = novaLista))
                                expandedStates[index] = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                onSalvarGeral(idCamp, configsAtuais)
                Toast.makeText(contexto, "Configurações Atualizadas e Salvas!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun TelaSumulaDetalhada(partida: Partida, equipes: List<EquipeExemplo>, onVoltar: () -> Unit) {
    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"

    val gM = if(partida.golsMandante.isEmpty()) "-" else partida.golsMandante
    val gV = if(partida.golsVisitante.isEmpty()) "-" else partida.golsVisitante

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
                    Text("MELHOR JOGADOR: ${partida.melhorJogador}", fontWeight = FontWeight.Bold, color = Color(0xFFB79400))
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

@Composable
fun SubTelaSumulaEvento(partidas: SnapshotStateList<Partida>, equipes: List<EquipeExemplo>, todosJogadores: List<JogadorExemplo>) {
    var partidaFocada by remember { mutableStateOf<Partida?>(null) }

    if (partidaFocada == null) {
        LazyColumn(Modifier.padding(16.dp)) {
            items(partidas) { p ->
                val mandante = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
                val visitante = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$mandante vs $visitante", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Button(onClick = { partidaFocada = p }) { Text("ABRIR SÚMULA") }
                    }
                }
            }
        }
    } else {
        val p = partidaFocada!!
        val mNome = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
        val vNome = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
        val jogadoresDaPartida = todosJogadores.filter { it.equipeId == p.mandanteId || it.equipeId == p.visitanteId }

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
                            val partidaEmEdicao = partidas[idx]
                            val nomeEquipeJogador = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"

                            if (tipoAtual == "GOL") {
                                if (jog.equipeId == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) + 1
                                    partidaEmEdicao.golsMandante = gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) + 1
                                    partidaEmEdicao.golsVisitante = gols.toString()
                                }
                            } else if (tipoAtual == "GOL CONTRA") {
                                if (jog.equipeId == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) + 1
                                    partidaEmEdicao.golsVisitante = gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) + 1
                                    partidaEmEdicao.golsMandante = gols.toString()
                                }
                            }

                            if (tipoAtual == "YELLOW CARD") {
                                val jaTemAmarelo = partidaEmEdicao.eventos.any { it.jogadorNome == jog.nome && it.tipo == "YELLOW CARD" }
                                partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = "YELLOW CARD", minuto = minutoInput))
                                if (jaTemAmarelo) {
                                    partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = "RED CARD", minuto = minutoInput))
                                }
                            } else {
                                partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = tipoAtual, minuto = minutoInput))
                            }

                            partidas[idx] = partidaEmEdicao.copy()
                            partidaFocada = partidas[idx]
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
                                    partidaFocada = partidas[idx]
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
            val pM = if(p.golsMandante.isEmpty()) "-" else p.golsMandante
            val pV = if(p.golsVisitante.isEmpty()) "-" else p.golsVisitante
            Text("Placar: $pM x $pV", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val btnMod = Modifier.weight(1f)
                val addMarco = { tipo: String ->
                    val idx = partidas.indexOfFirst { it.id == p.id }
                    if (idx != -1) {
                        partidas[idx].eventos.add(EventoSumula(jogadorNome = "Partida", equipeNome = "Geral", tipo = tipo))
                        partidas[idx] = partidas[idx].copy()
                        partidaFocada = partidas[idx]
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
                    Text("Melhor da Partida: ${p.melhorJogador}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Button(onClick = { showDialogMelhor = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB79400))) {
                        Text("DEFINIR", fontSize = 10.sp)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Button(onClick = { tipoAtual = "GOL"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("GOL", fontSize = 10.sp) }
                    Button(onClick = { tipoAtual = "ASSISTÊNCIA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))) { Text("ASSIST.", fontSize = 10.sp) }
                    Button(onClick = { tipoAtual = "YELLOW CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)) { Text("YELLOW", fontSize = 8.sp) }
                    Button(onClick = { tipoAtual = "RED CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RED", fontSize = 8.sp) }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Button(onClick = { tipoAtual = "GOL CONTRA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("CONTRA", fontSize = 9.sp) }
                    Button(onClick = { tipoAtual = "COMETEU PÊNALTI"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("COM. PÊN.", fontSize = 8.sp) }
                    Button(onClick = { tipoAtual = "SOFREU PÊNALTI"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("SOF. PÊN.", fontSize = 8.sp) }
                    Button(onClick = { tipoAtual = "PÊNALTI DEFENDIDO"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("DEFEND.", fontSize = 9.sp) }
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
                    "PERDEU PÊNALTI" -> Color.Gray
                    "PÊNALTI DEFENDIDO" -> Color(0xFF1976D2)
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
                    Text("$minStr${ev.tipo}: ${ev.jogadorNome} (${ev.equipeNome})", color = cor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val idx = partidas.indexOfFirst { it.id == p.id }
                        if (idx != -1) {
                            val partidaEmEdicao = partidas[idx]

                            if (ev.tipo == "GOL") {
                                val equipeDoJogador = todosJogadores.find { it.nome == ev.jogadorNome }?.equipeId
                                if (equipeDoJogador == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsMandante = if (gols <= 0) "" else gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsVisitante = if (gols <= 0) "" else gols.toString()
                                }
                            } else if (ev.tipo == "GOL CONTRA") {
                                val equipeDoJogador = todosJogadores.find { it.nome == ev.jogadorNome }?.equipeId
                                if (equipeDoJogador == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsVisitante = if (gols <= 0) "" else gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsMandante = if (gols <= 0) "" else gols.toString()
                                }
                            }

                            partidaEmEdicao.eventos.remove(ev)
                            partidas[idx] = partidaEmEdicao.copy()
                            partidaFocada = partidas[idx]
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = { partidaFocada = null }, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
        }
    }
}

