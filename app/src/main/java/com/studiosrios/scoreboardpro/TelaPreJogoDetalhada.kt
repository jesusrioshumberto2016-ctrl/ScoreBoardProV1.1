package com.studiosrios.scoreboardpro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun TelaPreJogoDetalhada(
    partida: Partida,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("CAMPO", "ÁRBITROS")

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) { Icon(Icons.Default.ArrowBack, "Voltar") }
            Text(
                text = "INFORMAÇÕES DA PARTIDA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        TabRow(selectedTabIndex = abaSelecionada, containerColor = Color(0xFFF5F5F5)) {
            titulos.forEachIndexed { index, titulo ->
                Tab(
                    selected = abaSelecionada == index,
                    onClick = { abaSelecionada = index },
                    text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (abaSelecionada) {
                0 -> {
                    VisualizacaoCampoLeitura(partida, equipes, todosJogadores)
                }
                1 -> {
                    Column(Modifier.fillMaxSize().padding(24.dp)) {
                        Text("EQUIPE DE ARBITRAGEM", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        InfoItemLeitura("Árbitro Principal", partida.arbitroPrincipal)
                        InfoItemLeitura("Assistente 1", partida.assistente1)
                        InfoItemLeitura("Assistente 2", partida.assistente2)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text("COMISSÃO TÉCNICA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        InfoItemLeitura("Técnico (Mandante)", partida.tecnicoMandante)
                        InfoItemLeitura("Técnico (Visitante)", partida.tecnicoVisitante)
                    }
                }
            }
        }

        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text("VOLTAR PARA PARTIDAS")
        }
    }
}

@Composable
fun VisualizacaoCampoLeitura(p: Partida, equipes: List<EquipeExemplo>, todosJogadores: List<JogadorExemplo>) {
    val mandante = equipes.find { it.id == p.mandanteId }
    val visitante = equipes.find { it.id == p.visitanteId }
    
    val tMandante = todosJogadores.filter { it.id in p.titularesMandante }
    val tVisitante = todosJogadores.filter { it.id in p.titularesVisitante }

    val rMandante = todosJogadores.filter { it.id in p.reservasMandante }
    val rVisitante = todosJogadores.filter { it.id in p.reservasVisitante }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(500.dp).background(Color(0xFF2E7D32)).padding(8.dp)) {
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
                    DistribuirLeituraNoCampo(tVisitante, p, isVisitante = true)
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    DistribuirLeituraNoCampo(tMandante, p, isVisitante = false)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoMandante.ifBlank { "---" }}", fontSize = 10.sp, color = Color.Gray)
                Text(mandante?.nome ?: p.labelMandante, color = Color(0xFF1976D2), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("RESERVAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                rMandante.forEach { res ->
                    Text("${res.nome} (${p.posicoesNoJogo[res.id] ?: res.posicao})", fontSize = 9.sp, color = Color.DarkGray)
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Técnico: ${p.tecnicoVisitante.ifBlank { "---" }}", fontSize = 10.sp, color = Color.Gray)
                Text(visitante?.nome ?: p.labelVisitante, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("RESERVAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                rVisitante.forEach { res ->
                    Text("${res.nome} (${p.posicoesNoJogo[res.id] ?: res.posicao})", fontSize = 9.sp, color = Color.DarkGray)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun BoxScope.DistribuirLeituraNoCampo(jogadores: List<JogadorExemplo>, p: Partida, isVisitante: Boolean) {
    fun filtrar(lista: List<String>) = jogadores.filter { (p.posicoesNoJogo[it.id] ?: it.posicao).split(" ").first() in lista }

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
                    JogadorIconeLeitura(jog, if(isVisitante) Color(0xFFD32F2F) else Color(0xFF1976D2))
                }
            }
        }
    }
}

@Composable
fun JogadorIconeLeitura(j: JogadorExemplo, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = cor, border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)) {
            Box(contentAlignment = Alignment.Center) {
                Text(j.nome.take(1).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(j.nome.split(" ").last(), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoItemLeitura(label: String, valor: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = valor.ifBlank { "A definir" }, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
