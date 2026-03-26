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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun PreJogoTab(
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    onEntrarEdicao: (Int) -> Unit = {},
    onSairEdicao: () -> Unit = {},
    onAlteracao: () -> Unit = {},
    onSalvar: () -> Unit = {}
) {
    var partidaParaPreJogo by remember { mutableStateOf<Partida?>(null) }

    if (partidaParaPreJogo == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("PRÉ-JOGO: SELECIONE A PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            val partidasOrdenadas = obterPartidasOrdenadas(partidas)

            LazyColumn {
                items(partidasOrdenadas) { partida ->
                    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: partida.labelMandante
                    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: partida.labelVisitante

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                            partidaParaPreJogo = partida 
                            onEntrarEdicao(partida.id)
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(partida.fase.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                Text("$mandante vs $visitante", fontWeight = FontWeight.Bold)
                                Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    } else {
        val currentPartida = partidaParaPreJogo
        val idx = partidas.indexOfFirst { it.id == currentPartida?.id }
        if (idx != -1) {
            ConfiguracaoPreJogoDetalhada(
                p = partidas[idx],
                idx = idx,
                partidas = partidas,
                equipes = equipes,
                listaGlobalJogadores = listaGlobalJogadores,
                onVoltar = { 
                    partidaParaPreJogo = null 
                    onSairEdicao()
                },
                onAlteracao = onAlteracao,
                onSalvar = onSalvar
            )
        }
    }
}

@Composable
fun ConfiguracaoPreJogoDetalhada(
    p: Partida,
    idx: Int,
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>,
    listaGlobalJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit,
    onAlteracao: () -> Unit,
    onSalvar: () -> Unit
) {
    var pEditada by remember { mutableStateOf(p) }
    
    val mandanteNome = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
    val visitanteNome = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
    var opcaoSelecionada by remember { mutableIntStateOf(0) }
    
    val houveMudancaLocal by remember(pEditada) { derivedStateOf { pEditada != p } }
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }
    var jogadorParaVerAcoes by remember { mutableStateOf<JogadorExemplo?>(null) }

    val tentarVoltar = {
        if (houveMudancaLocal) {
            mostrarConfirmacaoSair = true
        } else {
            onVoltar()
        }
    }

    BackHandler {
        tentarVoltar()
    }

    if (mostrarConfirmacaoSair) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoSair = false },
            title = { Text("Alterações pendentes") },
            text = { Text("Você realizou alterações na escalação/dados desta partida. Deseja sair sem salvar?") },
            confirmButton = {
                Button(onClick = { mostrarConfirmacaoSair = false; onVoltar() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("SAIR SEM SALVAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoSair = false }) { Text("CONTINUAR EDITANDO") }
            }
        )
    }

    if (jogadorParaVerAcoes != null) {
        val jog = jogadorParaVerAcoes!!
        val acoes = p.eventos.filter { it.jogadorNome == jog.nome }
        AlertDialog(
            onDismissRequest = { jogadorParaVerAcoes = null },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = jog.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(jog.apelido.ifBlank { jog.nome }, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = tentarVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
            Text("CONFIGURAR PARTIDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        TabRow(selectedTabIndex = opcaoSelecionada, containerColor = Color(0xFFF5F5F5)) {
            Tab(selected = opcaoSelecionada == 0, onClick = { opcaoSelecionada = 0 }, text = { Text(mandanteNome.take(8), fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 1, onClick = { opcaoSelecionada = 1 }, text = { Text(visitanteNome.take(8), fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 2, onClick = { opcaoSelecionada = 2 }, text = { Text("CAMPO", fontSize = 9.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = opcaoSelecionada == 3, onClick = { opcaoSelecionada = 3 }, text = { Text("ÁRBITROS", fontSize = 9.sp, fontWeight = FontWeight.Bold) })
        }

        Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            when (opcaoSelecionada) {
                0, 1 -> {
                    val isMandante = opcaoSelecionada == 0
                    val idTimeAtual = if (isMandante) p.mandanteId else p.visitanteId
                    val jogadoresDoTime = listaGlobalJogadores.filter { it.equipeId == idTimeAtual }

                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Text("COMISSÃO TÉCNICA", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            OutlinedTextField(
                                value = if (isMandante) pEditada.tecnicoMandante else pEditada.tecnicoVisitante,
                                onValueChange = { v -> 
                                    pEditada = if (isMandante) pEditada.copy(tecnicoMandante = v) else pEditada.copy(tecnicoVisitante = v)
                                },
                                label = { Text("Técnico") }, modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("ESCALAÇÃO (TITULARES E RESERVAS)", fontWeight = FontWeight.Bold, color = Color.Gray)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }

                        items(jogadoresDoTime) { jog ->
                            val isTitular = if (isMandante) pEditada.titularesMandante.contains(jog.id) else pEditada.titularesVisitante.contains(jog.id)
                            val isReserva = if (isMandante) pEditada.reservasMandante.contains(jog.id) else pEditada.reservasVisitante.contains(jog.id)
                            val posicaoNoJogo = pEditada.posicoesNoJogo[jog.id.toString()] ?: jog.posicao

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = when { isTitular -> Color(0xFFE8F5E9); isReserva -> Color(0xFFFFF3E0); else -> Color.White })
                            ) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = jog.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                        contentDescription = null,
                                        modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.LightGray).clickable { jogadorParaVerAcoes = jog },
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(jog.apelido.ifBlank { jog.nome }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$posicaoNoJogo | ${jog.idade}", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    Spacer(Modifier.width(4.dp))
                                    
                                    var showPosMenu by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(
                                            onClick = { showPosMenu = true },
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(posicaoNoJogo, fontSize = 9.sp)
                                        }
                                        DropdownMenu(expanded = showPosMenu, onDismissRequest = { showPosMenu = false }) {
                                            val posicoes = listOf("GOL", "ZAG", "LAT", "VOL", "MEI", "MAT", "ALA", "PT", "CA")
                                            posicoes.forEach { pos ->
                                                DropdownMenuItem(
                                                    text = { Text(pos) },
                                                    onClick = {
                                                        val novoMapa = pEditada.posicoesNoJogo.toMutableMap()
                                                        novoMapa[jog.id.toString()] = pos
                                                        pEditada = pEditada.copy(posicoesNoJogo = novoMapa)
                                                        showPosMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        TextButton(onClick = {
                                            pEditada = if (isMandante) {
                                                pEditada.copy(titularesMandante = if (isTitular) pEditada.titularesMandante - jog.id else pEditada.titularesMandante + jog.id, reservasMandante = pEditada.reservasMandante - jog.id)
                                            } else {
                                                pEditada.copy(titularesVisitante = if (isTitular) pEditada.titularesVisitante - jog.id else pEditada.titularesVisitante + jog.id, reservasVisitante = pEditada.reservasVisitante - jog.id)
                                            }
                                        }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("TIT", fontSize = 10.sp, color = if (isTitular) Color(0xFF2E7D32) else Color.Gray) }

                                        TextButton(onClick = {
                                            pEditada = if (isMandante) {
                                                pEditada.copy(reservasMandante = if (isReserva) pEditada.reservasMandante - jog.id else pEditada.reservasMandante + jog.id, titularesMandante = pEditada.titularesMandante - jog.id)
                                            } else {
                                                pEditada.copy(reservasVisitante = if (isReserva) pEditada.reservasVisitante - jog.id else pEditada.reservasVisitante + jog.id, titularesVisitante = pEditada.titularesVisitante - jog.id)
                                            }
                                        }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("RES", fontSize = 10.sp, color = if (isReserva) Color(0xFFEF6C00) else Color.Gray) }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    val mandanteTitulares = pEditada.titularesMandante.mapNotNull { id -> listaGlobalJogadores.find { it.id == id } }
                    val visitanteTitulares = pEditada.titularesVisitante.mapNotNull { id -> listaGlobalJogadores.find { it.id == id } }
                    val mandanteReservas = pEditada.reservasMandante.mapNotNull { id -> listaGlobalJogadores.find { it.id == id } }
                    val visitanteReservas = pEditada.reservasVisitante.mapNotNull { id -> listaGlobalJogadores.find { it.id == id } }

                    PainelCampoSimulado(
                        titularesM = mandanteTitulares,
                        titularesV = visitanteTitulares,
                        reservasM = mandanteReservas,
                        reservasV = visitanteReservas,
                        partida = pEditada,
                        equipes = equipes,
                        onJogadorClick = { j -> jogadorParaVerAcoes = j }
                    )
                }
                3 -> {
                    Column(Modifier.fillMaxSize()) {
                        Text("EQUIPE DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = Color.Gray)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        OutlinedTextField(value = pEditada.arbitroPrincipal, onValueChange = { pEditada = pEditada.copy(arbitroPrincipal = it) }, label = { Text("Árbitro Principal") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pEditada.assistente1, onValueChange = { pEditada = pEditada.copy(assistente1 = it) }, label = { Text("Assistente 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pEditada.assistente2, onValueChange = { pEditada = pEditada.copy(assistente2 = it) }, label = { Text("Assistente 2") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        Button(
            onClick = { 
                partidas[idx] = pEditada 
                onAlteracao()
                onSalvar()
                onVoltar() 
            }, 
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("SALVAR E VOLTAR")
        }
    }
}

@Composable
fun PainelCampoSimulado(
    titularesM: List<JogadorExemplo>,
    titularesV: List<JogadorExemplo>,
    reservasM: List<JogadorExemplo>,
    reservasV: List<JogadorExemplo>,
    partida: Partida,
    equipes: List<EquipeExemplo>,
    onJogadorClick: (JogadorExemplo) -> Unit
) {
    val scrollState = rememberScrollState()
    val mandante = equipes.find { it.id == partida.mandanteId }
    val visitante = equipes.find { it.id == partida.visitanteId }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().height(650.dp).padding(16.dp).background(Color(0xFF2E7D32)).border(2.dp, Color.White)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val fieldColor = Color.White
                val strokeWidth = 2.dp.toPx()
                drawLine(fieldColor, Offset(0f, h/2), Offset(w, h/2), strokeWidth = strokeWidth)
                drawCircle(fieldColor, radius = 60.dp.toPx(), center = Offset(w/2, h/2), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                drawRect(fieldColor, topLeft = Offset(w*0.2f, 0f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
                drawRect(fieldColor, topLeft = Offset(w*0.2f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
            }
            
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampoLocal(titularesV, partida, true, onJogadorClick)
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampoLocal(titularesM, partida, false, onJogadorClick)
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                if (partida.tecnicoMandante.isNotBlank()) {
                    Text("TÉC: ${partida.tecnicoMandante.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = mandante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                }
                reservasM.forEach { res ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).clickable { onJogadorClick(res) }) {
                        AsyncImage(model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                        Spacer(Modifier.width(8.dp))
                        Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                    }
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                if (partida.tecnicoVisitante.isNotBlank()) {
                    Text("TÉC: ${partida.tecnicoVisitante.uppercase()}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.DarkGray, textAlign = TextAlign.End, modifier = Modifier.padding(bottom = 4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    Text("RESERVAS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.End)
                    Spacer(Modifier.width(8.dp))
                    AsyncImage(
                        model = visitante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                    )
                }
                reservasV.forEach { res ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onJogadorClick(res) }) {
                        Text(res.apelido.ifBlank { res.nome }, fontSize = 12.sp)
                        Spacer(Modifier.width(8.dp))
                        AsyncImage(model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background }, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray))
                    }
                }
            }
        }
    }
}

@Composable
fun DistribuirJogadoresNoCampoLocal(jogadores: List<JogadorExemplo>, p: Partida, isVisitante: Boolean, onJogadorClick: (JogadorExemplo) -> Unit) {
    fun filtrar(pos: List<String>) = jogadores.filter { (p.posicoesNoJogo[it.id.toString()] ?: it.posicao).split(" ").first() in pos }

    val goleiros = filtrar(listOf("GOL"))
    val defesas = filtrar(listOf("ZAG", "LAT"))
    val volantes = filtrar(listOf("VOL"))
    val meias = filtrar(listOf("MEI", "MAT", "ALA"))
    val atacantes = filtrar(listOf("PT", "CA"))

    val linhas = if (isVisitante) listOf(goleiros, defesas, volantes, meias, atacantes)
                 else listOf(atacantes, meias, volantes, defesas, goleiros)

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        linhas.forEach { linha ->
            val linhaOrdenada = linha.sortedWith(compareBy { jog ->
                val pos = p.posicoesNoJogo[jog.id.toString()] ?: jog.posicao
                when {
                    pos.contains("(E)") -> 0
                    pos.contains("(D)") -> 2
                    else -> 1
                }
            })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                linhaOrdenada.forEach { jog ->
                    JogadorIconeCampoLocal(jog, if(isVisitante) Color(0xFFD32F2F) else Color(0xFF1976D2), onJogadorClick)
                }
            }
        }
    }
}

@Composable
fun JogadorIconeCampoLocal(j: JogadorExemplo, corTime: Color, onJogadorClick: (JogadorExemplo) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onJogadorClick(j) }) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, corTime)
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = j.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            text = j.apelido.ifBlank { j.nome.split(" ").last() },
            fontSize = 9.sp, 
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp),
            maxLines = 1
        )
    }
}
