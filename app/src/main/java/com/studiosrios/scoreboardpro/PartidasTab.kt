package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun PartidasTab(
    partidas: List<Partida>,
    equipes: List<EquipeExemplo>,
    onPreJogoClick: (Partida) -> Unit,
    onDetalhesClick: (Partida) -> Unit,
    somenteMataMata: Boolean = false,
    onEquipeClick: (EquipeExemplo) -> Unit = {}
) {
    val partidasFiltradas = remember(partidas, somenteMataMata) {
        if (somenteMataMata) {
            partidas.filter { 
                it.fase.contains("OITAVAS", true) || 
                it.fase.contains("QUARTAS", true) || 
                it.fase.contains("SEMI", true) || 
                it.fase.contains("FINAL", true) ||
                it.fase.contains("PF", true)
            }
        } else {
            partidas.filter { it.fase.contains("RODADA", true) || it.fase.contains("ÚNICA", true) || it.fase.isBlank() }
        }
    }

    val fasesDisponiveis = remember(partidasFiltradas) {
        partidasFiltradas.map { it.fase.ifBlank { "Rodada" } }.distinct()
    }

    var indiceFaseAtual by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(fasesDisponiveis) {
        if (indiceFaseAtual >= fasesDisponiveis.size) indiceFaseAtual = 0
    }

    val faseExibida = if (fasesDisponiveis.isNotEmpty()) fasesDisponiveis[indiceFaseAtual] else "Nenhum jogo"
    
    val partidasDaFase = remember(partidasFiltradas, faseExibida) {
        val lista = partidasFiltradas.filter { (it.fase.ifBlank { "Rodada" }) == faseExibida }
        lista.sortedWith(
            compareBy<Partida> { it.data.split("/").reversed().joinToString("") }
                .thenBy { it.horario }
        )
    }

    Column(Modifier.fillMaxSize()) {
        if (fasesDisponiveis.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (indiceFaseAtual > 0) indiceFaseAtual-- },
                        enabled = indiceFaseAtual > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Anterior")
                    }

                    Text(
                        text = faseExibida.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    IconButton(
                        onClick = { if (indiceFaseAtual < fasesDisponiveis.size - 1) indiceFaseAtual++ },
                        enabled = indiceFaseAtual < fasesDisponiveis.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Próxima")
                    }
                }
            }
        }

        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            if (partidasDaFase.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma partida encontrada nesta fase.", color = Color.Gray)
                    }
                }
            } else {
                items(partidasDaFase) { partida ->
                    ItemPartidaCard(partida, equipes, onPreJogoClick, onDetalhesClick, onEquipeClick)
                }
            }
        }
    }
}

@Composable
fun ItemPartidaCard(
    partida: Partida,
    equipes: List<EquipeExemplo>,
    onPreJogoClick: (Partida) -> Unit,
    onDetalhesClick: (Partida) -> Unit,
    onEquipeClick: (EquipeExemplo) -> Unit = {}
) {
    val equipeMandante = equipes.find { it.id == partida.mandanteId }
    val equipeVisitante = equipes.find { it.id == partida.visitanteId }
    
    val mandante = equipeMandante?.nome ?: partida.labelMandante
    val visitante = equipeVisitante?.nome ?: partida.labelVisitante
    
    val placarTexto = if (partida.golsMandante == null && partida.golsVisitante == null) {
        " VS "
    } else {
        val pM = partida.golsMandante ?: 0
        val pV = partida.golsVisitante ?: 0
        val penStr = if (partida.penaltisMandante != null) " (${partida.penaltisMandante}) $pM x $pV (${partida.penaltisVisitante}) " 
                     else " $pM x $pV "
        penStr
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                Text("LOCAL: ${partida.local}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Mandante Shield + Name
                Row(
                    modifier = Modifier.weight(1f).clickable(enabled = equipeMandante != null) { equipeMandante?.let { onEquipeClick(it) } }, 
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = mandante, 
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp, 
                        maxLines = 1
                    )
                    Spacer(Modifier.width(8.dp))
                    AsyncImage(
                        model = equipeMandante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White).border(0.5.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = placarTexto, 
                    modifier = Modifier.padding(horizontal = 8.dp), 
                    fontWeight = FontWeight.Black, 
                    fontSize = 14.sp, 
                    color = if (partida.finalizada) Color.Blue else Color.Gray, 
                    textAlign = TextAlign.Center
                )

                // Visitante Shield + Name
                Row(
                    modifier = Modifier.weight(1f).clickable(enabled = equipeVisitante != null) { equipeVisitante?.let { onEquipeClick(it) } }, 
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.Start
                ) {
                    AsyncImage(
                        model = equipeVisitante?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White).border(0.5.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = visitante, 
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp, 
                        maxLines = 1
                    )
                }
            }

            if (partida.finalizada) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFB79400), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Melhor: ${partida.melhorJogador.ifBlank { "Não definido" }}", fontSize = 10.sp, color = Color(0xFFB79400), fontWeight = FontWeight.Bold)
                }
            }

            Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { onPreJogoClick(partida) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp).weight(1f)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("PRÉ-JOGO", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onDetalhesClick(partida) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp).weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("DETALHES", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
