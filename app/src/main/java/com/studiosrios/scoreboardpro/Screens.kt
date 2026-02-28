package com.studiosrios.scoreboardpro

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.Calendar

@Composable
fun TelaPainelCampeonato(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    configsIniciais: ConfiguracoesCampeonato,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("PRE-JOGO", "TABELA", "PARTIDAS", "RESULTADOS", "SÚMULA", "CONFIGURAR")
    val contexto = LocalContext.current

    // Estado local das configurações para serem editadas na aba
    var estadoConfigs by remember { mutableStateOf(configsIniciais.copy()) }

    var partidaSelecionadaParaSumula by remember { mutableStateOf<Partida?>(null) }

    fun obterPartidasOrdenadas(): List<Partida> {
        return partidas.sortedWith(
            compareBy({ it.data.split("/").reversed().joinToString("") }, { it.horario })
        )
    }

    if (partidaSelecionadaParaSumula != null) {
        TelaSumulaDetalhada(
            partida = partidaSelecionadaParaSumula!!,
            equipes = equipes,
            onVoltar = { partidaSelecionadaParaSumula = null })
    } else {
        val classificacao = equipes.map { equipe ->
            var p = 0;
            var j = 0;
            var v = 0;
            var e = 0;
            var d = 0;
            var gm = 0;
            var gs = 0
            partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }
                .forEach { part ->
                    j++
                    val gM = part.golsMandante.toIntOrNull() ?: 0
                    val gV = part.golsVisitante.toIntOrNull() ?: 0
                    if (part.mandanteId == equipe.id) {
                        gm += gM; gs += gV
                        if (gM > gV) {
                            p += 3; v++
                        } else if (gM == gV) {
                            p += 1; e++
                        } else d++
                    } else {
                        gm += gV; gs += gM
                        if (gV > gM) {
                            p += 3; v++
                        } else if (gV == gM) {
                            p += 1; e++
                        } else d++
                    }
                }
            LinhaTabela(equipe.nome, p, j, v, e, d, gm, gs, gm - gs)
        }.sortedByDescending { it.pontos * 1000 + it.vitorias * 100 + it.sg }

        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = abaSelecionada,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {}) {
                titulos.forEachIndexed { index, titulo ->
                    Tab(
                        selected = abaSelecionada == index,
                        onClick = { abaSelecionada = index },
                        text = { Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold) })
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (abaSelecionada) {
                    0 -> {
                        // 1. Controle de qual partida foi clicada
                        var partidaParaPreJogo by remember { mutableStateOf<Partida?>(null) }

                        if (partidaParaPreJogo == null) {
                            // --- TELA A: LISTA DE PARTIDAS PARA SELECIONAR ---
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text("PRÉ-JOGO: SELECIONE A PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(10.dp))

                                LazyColumn {
                                    items(obterPartidasOrdenadas()) { partida ->
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
                            // --- TELA B: OPÇÕES DA PARTIDA SELECIONADA (Time A, Time B, Arbitragem) ---
                            val mandanteNome = equipes.find { it.id == partidaParaPreJogo?.mandanteId }?.nome ?: "Time A"
                            val visitanteNome = equipes.find { it.id == partidaParaPreJogo?.visitanteId }?.nome ?: "Time B"

                            // Estado para controlar as 3 opções internas
                            var opcaoSelecionada by remember { mutableIntStateOf(0) }
                            val titulosOpcoes = listOf(mandanteNome, visitanteNome, "Arbitragem")

                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                // Cabeçalho com botão voltar
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { partidaParaPreJogo = null }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                                    }
                                    Text("CONFIGURAR PARTIDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.height(8.dp))

                                // Seletor de Opções (Time A, Time B, Arbitragem)
                                TabRow(
                                    selectedTabIndex = opcaoSelecionada,
                                    containerColor = Color(0xFFF5F5F5),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ) {
                                    titulosOpcoes.forEachIndexed { index, titulo ->
                                        Tab(
                                            selected = opcaoSelecionada == index,
                                            onClick = { opcaoSelecionada = index },
                                            text = { Text(titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Conteúdo dinâmico baseado na opção selecionada
                                Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                                    when (opcaoSelecionada) {
                                        0 -> { // Interface do Time A
                                            Column {
                                                Text("ESCALAÇÃO: $mandanteNome", fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                                                Divider(Modifier.padding(vertical = 8.dp))
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Titulares") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Reservas") }, modifier = Modifier.fillMaxWidth())
                                            }
                                        }
                                        1 -> { // Interface do Time B
                                            Column {
                                                Text("ESCALAÇÃO: $visitanteNome", fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                                                Divider(Modifier.padding(vertical = 8.dp))
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Titulares") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Reservas") }, modifier = Modifier.fillMaxWidth())
                                            }
                                        }
                                        2 -> { // Interface da Arbitragem
                                            Column {
                                                Text("CORPO DE ARBITRAGEM", fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                                                Divider(Modifier.padding(vertical = 8.dp))
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Árbitro Principal") }, modifier = Modifier.fillMaxWidth())
                                                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Assistentes") }, modifier = Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }

                                // Botão Final
                                Button(
                                    onClick = { partidaParaPreJogo = null },
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                ) {
                                    Text("SALVAR E VOLTAR")
                                }
                            }
                        }
                    }


                    1 -> {
                        Column(Modifier.fillMaxSize()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.DarkGray)
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    "EQUIPE",
                                    Modifier.weight(2.5f),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                listOf("P", "J", "V", "E", "D", "GM", "GS", "SG").forEach {
                                    Text(
                                        it,
                                        Modifier.weight(1f),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                items(classificacao) { linha ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            linha.nome,
                                            Modifier.weight(2.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        listOf(
                                            linha.pontos,
                                            linha.jogos,
                                            linha.vitorias,
                                            linha.empates,
                                            linha.derrotas,
                                            linha.gm,
                                            linha.gs,
                                            linha.sg
                                        ).forEach {
                                            Text(
                                                it.toString(),
                                                Modifier.weight(1f),
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                                }
                            }
                            Button(
                                onClick = {
                                    onSalvarGeral(idCamp, estadoConfigs)
                                    Toast.makeText(
                                        contexto,
                                        if (idCamp == -1) "Campeonato Salvo!" else "Alterações Salvas!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text(if (idCamp == -1) "SALVAR CAMPEONATO" else "ATUALIZAR CAMPEONATO")
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(Modifier.padding(16.dp)) {
                            items(obterPartidasOrdenadas()) { partida ->
                                val mandante =
                                    equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
                                val visitante =
                                    equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"
                                val placarTexto =
                                    if (partida.golsMandante.isEmpty() && partida.golsVisitante.isEmpty()) " VS " else " ${partida.golsMandante} x ${partida.golsVisitante} "
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { partidaSelecionadaParaSumula = partida }) {
                                    Column(
                                        Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "${partida.data} - ${partida.horario}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "LOCAL: ${partida.local}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                mandante,
                                                Modifier.weight(1f),
                                                textAlign = TextAlign.End,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                placarTexto,
                                                Modifier.padding(horizontal = 10.dp),
                                                fontWeight = FontWeight.Black,
                                                color = if (partida.finalizada) Color.Blue else Color.Gray
                                            )
                                            Text(
                                                visitante,
                                                Modifier.weight(1f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (partida.finalizada) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    null,
                                                    tint = Color(0xFFB79400),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Melhor: ${partida.melhorJogador}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFB79400),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        LazyColumn(Modifier.padding(16.dp)) {
                            items(obterPartidasOrdenadas()) { partida ->
                                val m = equipes.find { it.id == partida.mandanteId }?.nome ?: "M"
                                val v = equipes.find { it.id == partida.visitanteId }?.nome ?: "V"
                                var mostrarDialogoLocal by remember { mutableStateOf(false) }
                                var localTemporario by remember { mutableStateOf(partida.local) }

                                if (mostrarDialogoLocal) {
                                    AlertDialog(
                                        onDismissRequest = { mostrarDialogoLocal = false },
                                        title = { Text("Definir Local") },
                                        text = { OutlinedTextField(value = localTemporario, onValueChange = { localTemporario = it }, label = { Text("Nome do Local/Estádio") }) },
                                        confirmButton = { Button(onClick = { val idx = partidas.indexOf(partida); if(idx != -1) partidas[idx] = partidas[idx].copy(local = localTemporario); mostrarDialogoLocal = false }) { Text("OK") } }
                                    )
                                }

                                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = if(partida.finalizada) Color(0xFFE3F2FD) else Color.White)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            TextButton(onClick = { if(!partida.finalizada){ val c = Calendar.getInstance(); DatePickerDialog(contexto, { _, y, month, d -> val idx = partidas.indexOf(partida); partidas[idx] = partidas[idx].copy(data = String.format("%02d/%02d", d, month + 1)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() } }) { Text("DATA: ${partida.data}", fontSize = 10.sp) }
                                            TextButton(onClick = { if(!partida.finalizada){ TimePickerDialog(contexto, { _, h, min -> val idx = partidas.indexOf(partida); partidas[idx] = partidas[idx].copy(horario = String.format("%02d:%02d", h, min)) }, 15, 0, true).show() } }) { Text("HORA: ${partida.horario}", fontSize = 10.sp) }

                                            // AQUI ESTÁ O BOTÃO RECUPERADO:
                                            TextButton(onClick = { if(!partida.finalizada) mostrarDialogoLocal = true }) { Text("LOCAL: ${partida.local}", fontSize = 10.sp) }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(m, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                            OutlinedTextField(value = partida.golsMandante, onValueChange = { if(!partida.finalizada) { val idx = partidas.indexOf(partida); if(idx != -1) partidas[idx] = partidas[idx].copy(golsMandante = it.filter { c -> c.isDigit() }) } }, modifier = Modifier.width(60.dp), enabled = !partida.finalizada, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center))
                                            Text(" X ", fontWeight = FontWeight.Black)
                                            OutlinedTextField(value = partida.golsVisitante, onValueChange = { if(!partida.finalizada) { val idx = partidas.indexOf(partida); if(idx != -1) partidas[idx] = partidas[idx].copy(golsVisitante = it.filter { c -> c.isDigit() }) } }, modifier = Modifier.width(60.dp), enabled = !partida.finalizada, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center))
                                            Text(v, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                        }
                                        Button(onClick = { val idx = partidas.indexOf(partida); if(idx != -1) partidas[idx] = partidas[idx].copy(finalizada = !partida.finalizada, golsMandante = if(partida.golsMandante.isEmpty()) "0" else partida.golsMandante, golsVisitante = if(partida.golsVisitante.isEmpty()) "0" else partida.golsVisitante) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = if(partida.finalizada) Color.Red else Color.DarkGray)) { Text(if(partida.finalizada) "CORRIGIR / CANCELAR" else "CONFIRMAR RESULTADO") }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        SubTelaSumulaEvento(partidas, equipes, listaGlobalJogadores)
                    }

                    5 -> {
                        SubTelaConfigurarTorneio(
                            idCamp,
                            modelo,
                            equipes,
                            partidas,
                            estadoConfigs,
                            { novaConfig -> estadoConfigs = novaConfig },
                            onSalvarGeral
                        )
                    }
                }
            }


            // BOTÕES INFERIORES: Adicionado Botão SALVAR para abas específicas
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onVoltar, modifier = Modifier.weight(1f)) {
                        Text("VOLTAR AO MENU", fontSize = 12.sp)
                    }

                    // Mostra o botão SALVAR apenas nas abas de Resultados (2) e Súmula (3)
                    if (abaSelecionada == 2 || abaSelecionada == 3) {
                        Button(
                            onClick = {
                                onSalvarGeral(idCamp, estadoConfigs)
                                Toast.makeText(contexto, "Dados Salvos!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("SALVAR", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelaInicialMenu(onNavegar: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ScoreBoard Pro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(30.dp))

        BotaoMenu("Cadastrar campeonato") { onNavegar("cadastrar_campeonato") }
        BotaoMenu("Gerenciar campeonato") { onNavegar("gerenciar_campeonato") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar equipe") { onNavegar("cadastrar_equipe") }
        BotaoMenu("Gerenciar equipe") { onNavegar("gerenciar_equipe") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar jogador") { onNavegar("cadastrar_jogador") }
        BotaoMenu("Gerenciar jogador") { onNavegar("gerenciar_jogador") }
    }
}