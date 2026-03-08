package com.studiosrios.scoreboardpro

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun ResultadosTab(
    partidas: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>
) {
    var subAbaSelecionada by remember { mutableIntStateOf(0) }
    val titulosSubAbas = listOf("Fase de Grupos", "Mata-Mata")

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = subAbaSelecionada, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            titulosSubAbas.forEachIndexed { index, titulo ->
                Tab(
                    selected = subAbaSelecionada == index,
                    onClick = { subAbaSelecionada = index },
                    text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val partidasFiltradas = if (subAbaSelecionada == 0) {
            partidas.filter { it.fase.contains("Rodada", ignoreCase = true) || it.fase.contains("Única", ignoreCase = true) }
        } else {
            partidas.filter { !it.fase.contains("Rodada", ignoreCase = true) && !it.fase.contains("Única", ignoreCase = true) && it.fase.isNotBlank() }
        }

        val partidasOrdenadas = obterPartidasOrdenadas(partidasFiltradas)

        if (partidasOrdenadas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhuma partida encontrada nesta fase.", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.padding(16.dp)) {
                items(partidasOrdenadas, key = { it.id }) { partida ->
                    ItemResultadoCard(partida, partidas, equipes)
                }
            }
        }
    }
}

@Composable
fun ItemResultadoCard(
    partida: Partida,
    listaGeral: SnapshotStateList<Partida>,
    equipes: List<EquipeExemplo>
) {
    val contexto = LocalContext.current
    val m = equipes.find { it.id == partida.mandanteId }?.nome ?: partida.labelMandante.ifBlank { "M" }
    val v = equipes.find { it.id == partida.visitanteId }?.nome ?: partida.labelVisitante.ifBlank { "V" }
    
    var mostrarDialogoLocal by remember { mutableStateOf(false) }
    var localTemporario by remember { mutableStateOf(partida.local) }

    if (mostrarDialogoLocal) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLocal = false },
            title = { Text("Definir Local") },
            text = { OutlinedTextField(value = localTemporario, onValueChange = { localTemporario = it }, label = { Text("Local/Estádio") }) },
            confirmButton = { 
                Button(onClick = { 
                    val idx = listaGeral.indexOfFirst { it.id == partida.id }
                    if(idx != -1) {
                        listaGeral[idx] = listaGeral[idx].copy(local = localTemporario)
                    }
                    mostrarDialogoLocal = false 
                }) { Text("OK") } 
            }
        )
    }

    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = if(partida.finalizada) Color(0xFFE3F2FD) else Color.White)) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = partida.fase.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { 
                    if(!partida.finalizada){ 
                        val c = Calendar.getInstance()
                        DatePickerDialog(contexto, { _, y, month, d -> 
                            val idx = listaGeral.indexOfFirst { it.id == partida.id }
                            if(idx != -1) {
                                val dataFormatada = "%02d/%02d".format(d, month + 1)
                                listaGeral[idx] = listaGeral[idx].copy(data = dataFormatada)
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
                    } 
                }) { Text("DATA: ${partida.data.ifBlank { "00/00" }}", fontSize = 10.sp) }

                TextButton(onClick = { 
                    if(!partida.finalizada){ 
                        TimePickerDialog(contexto, { _, h, min -> 
                            val idx = listaGeral.indexOfFirst { it.id == partida.id }
                            if(idx != -1) {
                                val horaFormatada = "%02d:%02d".format(h, min)
                                listaGeral[idx] = listaGeral[idx].copy(horario = horaFormatada)
                            }
                        }, 15, 0, true).show() 
                    } 
                }) { Text("HORA: ${partida.horario.ifBlank { "00:00" }}", fontSize = 10.sp) }

                TextButton(onClick = { if(!partida.finalizada) mostrarDialogoLocal = true }) { 
                    Text("LOCAL: ${partida.local}", fontSize = 10.sp) 
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placar de Pênaltis Mandante
                if (partida.penaltisMandante != null) {
                    Text("(${partida.penaltisMandante})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                }

                Text(m, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = partida.golsMandante?.toString() ?: "", 
                    onValueChange = { input -> 
                        if(!partida.finalizada) { 
                            val idx = listaGeral.indexOfFirst { it.id == partida.id }
                            if(idx != -1) { 
                                val num = input.filter { it.isDigit() }.toIntOrNull()
                                listaGeral[idx] = listaGeral[idx].copy(golsMandante = num) 
                            } 
                        } 
                    }, 
                    modifier = Modifier.width(60.dp), 
                    enabled = !partida.finalizada, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    textStyle = TextStyle(textAlign = TextAlign.Center)
                )
                
                Text(" X ", fontWeight = FontWeight.Black)
                
                OutlinedTextField(
                    value = partida.golsVisitante?.toString() ?: "", 
                    onValueChange = { input -> 
                        if(!partida.finalizada) { 
                            val idx = listaGeral.indexOfFirst { it.id == partida.id }
                            if(idx != -1) { 
                                val num = input.filter { it.isDigit() }.toIntOrNull()
                                listaGeral[idx] = listaGeral[idx].copy(golsVisitante = num) 
                            } 
                        } 
                    }, 
                    modifier = Modifier.width(60.dp), 
                    enabled = !partida.finalizada, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                    textStyle = TextStyle(textAlign = TextAlign.Center)
                )
                
                Text(v, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Placar de Pênaltis Visitante
                if (partida.penaltisVisitante != null) {
                    Spacer(Modifier.width(4.dp))
                    Text("(${partida.penaltisVisitante})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            
            Button(
                onClick = { 
                    val idx = listaGeral.indexOfFirst { it.id == partida.id }
                    if (idx != -1) {
                        val gM = listaGeral[idx].golsMandante ?: 0
                        val gV = listaGeral[idx].golsVisitante ?: 0
                        listaGeral[idx] = listaGeral[idx].copy(
                            finalizada = !partida.finalizada, 
                            golsMandante = gM, 
                            golsVisitante = gV
                        )
                    }
                }, 
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = if(partida.finalizada) Color.Red else Color.DarkGray)
            ) { 
                Text(if(partida.finalizada) "CORRIGIR / CANCELAR" else "CONFIRMAR RESULTADO") 
            }
        }
    }
}
