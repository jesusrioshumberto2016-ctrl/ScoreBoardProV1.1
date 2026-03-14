package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun PreJogoTab(
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    onEntrarEdicao: (Int) -> Unit = {},
    onSairEdicao: () -> Unit = {},
    onAlteracao: () -> Unit = {}
) {
    var partidaParaPreJogo by remember { mutableStateOf<Partida?>(null) }

    if (partidaParaPreJogo == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("PRÉ-JOGO: SELECIONE A PARTIDA", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))

            LazyColumn {
                items(partidas) { partida ->
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
        val idx = partidas.indexOfFirst { it.id == partidaParaPreJogo?.id }
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
                onAlteracao = onAlteracao
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
    onAlteracao: () -> Unit
) {
    // ESTADO LOCAL: As alterações não vão direto para a lista global até clicar em Salvar
    var pEditada by remember { mutableStateOf(p) }
    
    val mandanteNome = equipes.find { it.id == p.mandanteId }?.nome ?: p.labelMandante
    val visitanteNome = equipes.find { it.id == p.visitanteId }?.nome ?: p.labelVisitante
    var opcaoSelecionada by remember { mutableIntStateOf(0) }
    
    // Controle local de alterações para aviso ao sair
    var houveMudancaLocal by remember { mutableStateOf(false) }
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }

    val tentarVoltar = {
        if (houveMudancaLocal) {
            mostrarConfirmacaoSair = true
        } else {
            onVoltar()
        }
    }

    // Gerenciamento do botão voltar do celular para esta sub-tela
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
                                    houveMudancaLocal = true
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
                            val posicaoNoJogo = pEditada.posicoesNoJogo[jog.id] ?: jog.posicao

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = when { isTitular -> Color(0xFFE8F5E9); isReserva -> Color(0xFFFFF3E0); else -> Color.White })
                            ) {
                                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = jog.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                        contentDescription = null,
                                        modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(jog.apelido.ifBlank { jog.nome }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$posicaoNoJogo | ${jog.idade}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    
                                    var showPosMenu by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(
                                            onClick = { showPosMenu = true },
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(posicaoNoJogo, fontSize = 10.sp)
                                        }
                                        DropdownMenu(expanded = showPosMenu, onDismissRequest = { showPosMenu = false }) {
                                            val posicoes = listOf("GOL", "ZAG", "LAT", "VOL", "MEI", "MAT", "ALA", "PT", "CA")
                                            posicoes.forEach { pos ->
                                                DropdownMenuItem(
                                                    text = { Text(pos) },
                                                    onClick = {
                                                        val novoMapa = pEditada.posicoesNoJogo.toMutableMap()
                                                        novoMapa[jog.id] = pos
                                                        pEditada = pEditada.copy(posicoesNoJogo = novoMapa)
                                                        houveMudancaLocal = true
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
                                            houveMudancaLocal = true
                                        }) { Text("TIT", color = if (isTitular) Color(0xFF2E7D32) else Color.Gray) }

                                        TextButton(onClick = {
                                            pEditada = if (isMandante) {
                                                pEditada.copy(reservasMandante = if (isReserva) pEditada.reservasMandante - jog.id else pEditada.reservasMandante + jog.id, titularesMandante = pEditada.titularesMandante - jog.id)
                                            } else {
                                                pEditada.copy(reservasVisitante = if (isReserva) pEditada.reservasVisitante - jog.id else pEditada.reservasVisitante + jog.id, titularesVisitante = pEditada.titularesVisitante - jog.id)
                                            }
                                            houveMudancaLocal = true
                                        }) { Text("RES", color = if (isReserva) Color(0xFFEF6C00) else Color.Gray) }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> CampoVisualEstiloSofa(pEditada, equipes, listaGlobalJogadores)
                3 -> {
                    Column(Modifier.fillMaxSize()) {
                        Text("EQUIPE DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = Color.Gray)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        OutlinedTextField(value = pEditada.arbitroPrincipal, onValueChange = { pEditada = pEditada.copy(arbitroPrincipal = it); houveMudancaLocal = true }, label = { Text("Árbitro Principal") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pEditada.assistente1, onValueChange = { pEditada = pEditada.copy(assistente1 = it); houveMudancaLocal = true }, label = { Text("Assistente 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pEditada.assistente2, onValueChange = { pEditada = pEditada.copy(assistente2 = it); houveMudancaLocal = true }, label = { Text("Assistente 2") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        Button(
            onClick = { 
                partidas[idx] = pEditada // Confirma as mudanças na lista global
                houveMudancaLocal = false // Importante: Reset do sinalizador antes de voltar
                onAlteracao()
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
fun CampoVisualEstiloSofa(p: Partida, equipes: List<EquipeExemplo>, todosJogadores: List<JogadorExemplo>) {
    val equipeM = equipes.find { it.id == p.mandanteId }
    val equipeV = equipes.find { it.id == p.visitanteId }
    
    val tMandante = todosJogadores.filter { it.id in p.titularesMandante }
    val tVisitante = todosJogadores.filter { it.id in p.titularesVisitante }
    
    val rMandante = todosJogadores.filter { it.id in p.reservasMandante }
    val rVisitante = todosJogadores.filter { it.id in p.reservasVisitante }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("FORMAÇÃO TÁTICA", fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(550.dp).background(Color(0xFF2E7D32)).padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val lineCol = Color.White.copy(0.6f)
                drawLine(lineCol, Offset(0f, h/2), Offset(w, h/2), strokeWidth = 2.dp.toPx())
                drawCircle(lineCol, radius = 60.dp.toPx(), center = Offset(w/2, h/2), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawRect(lineCol, topLeft = Offset(w*0.2f, 0f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                drawRect(lineCol, topLeft = Offset(w*0.2f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.6f, h*0.12f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
            }

            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampo(tVisitante, p, isVisitante = true)
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirJogadoresNoCampo(tMandante, p, isVisitante = false)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoMandante.ifBlank { "--" }}", fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(equipeM?.nome ?: p.labelMandante, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.width(4.dp))
                    AsyncImage(
                        model = equipeM?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(Color.White).border(0.5.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("RESERVAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                rMandante.forEach { res ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        AsyncImage(
                            model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(res.apelido.ifBlank { res.nome }, fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }

            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoVisitante.ifBlank { "--" }}", fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = equipeV?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(Color.White).border(0.5.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(equipeV?.nome ?: p.labelVisitante, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(8.dp))
                Text("RESERVAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                rVisitante.forEach { res ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        AsyncImage(
                            model = res.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(res.apelido.ifBlank { res.nome }, fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun DistribuirJogadoresNoCampo(jogadores: List<JogadorExemplo>, p: Partida, isVisitante: Boolean) {
    fun filtrar(pos: List<String>) = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao).split(" ").first() in pos }

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
                val pos = p.posicoesNoJogo[jog.id] ?: jog.posicao
                when {
                    pos.contains("(E)") -> 0
                    pos.contains("(D)") -> 2
                    else -> 1
                }
            })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                linhaOrdenada.forEach { jog ->
                    JogadorIconeCampo(jog, p, if(isVisitante) Color(0xFFD32F2F) else Color(0xFF1976D2))
                }
            }
        }
    }
}

@Composable
fun JogadorIconeCampo(j: JogadorExemplo, p: Partida, corTime: Color) {
    val posLado = p.posicoesNoJogo[j.id] ?: ""

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .padding(horizontal = 4.dp, vertical = 1.dp),
            maxLines = 1
        )
    }
}
