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
    val contexto = LocalContext.current
    // Usamos a função de ordenação definida no MatchLogic.kt
    val partidasOrdenadas = obterPartidasOrdenadas(partidas)

    LazyColumn(Modifier.padding(16.dp)) {
        items(partidasOrdenadas, key = { it.id }) { partida ->
            val m = equipes.find { it.id == partida.mandanteId }?.nome ?: "M"
            val v = equipes.find { it.id == partida.visitanteId }?.nome ?: "V"
            var mostrarDialogoLocal by remember { mutableStateOf(false) }
            var localTemporario by remember { mutableStateOf(partida.local) }

            if (mostrarDialogoLocal) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoLocal = false },
                    title = { Text("Definir Local") },
                    text = { OutlinedTextField(value = localTemporario, onValueChange = { localTemporario = it }, label = { Text("Local/Estádio") }) },
                    confirmButton = { 
                        Button(onClick = { 
                            val idx = partidas.indexOfFirst { it.id == partida.id }
                            if(idx != -1) {
                                partidas[idx] = partidas[idx].copy(local = localTemporario)
                            }
                            mostrarDialogoLocal = false 
                        }) { Text("OK") } 
                    }
                )
            }

            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = if(partida.finalizada) Color(0xFFE3F2FD) else Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { 
                            if(!partida.finalizada){ 
                                val c = Calendar.getInstance()
                                DatePickerDialog(contexto, { _, y, month, d -> 
                                    val idx = partidas.indexOfFirst { it.id == partida.id }
                                    if(idx != -1) {
                                        val dataFormatada = "%02d/%02d".format(d, month + 1)
                                        partidas[idx] = partidas[idx].copy(data = dataFormatada)
                                    }
                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() 
                            } 
                        }) { Text("DATA: ${partida.data.ifBlank { "00/00" }}", fontSize = 10.sp) }

                        TextButton(onClick = { 
                            if(!partida.finalizada){ 
                                TimePickerDialog(contexto, { _, h, min -> 
                                    val idx = partidas.indexOfFirst { it.id == partida.id }
                                    if(idx != -1) {
                                        val horaFormatada = "%02d:%02d".format(h, min)
                                        partidas[idx] = partidas[idx].copy(horario = horaFormatada)
                                    }
                                }, 15, 0, true).show() 
                            } 
                        }) { Text("HORA: ${partida.horario.ifBlank { "00:00" }}", fontSize = 10.sp) }

                        TextButton(onClick = { if(!partida.finalizada) mostrarDialogoLocal = true }) { 
                            Text("LOCAL: ${partida.local}", fontSize = 10.sp) 
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(m, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        
                        OutlinedTextField(
                            value = partida.golsMandante?.toString() ?: "", 
                            onValueChange = { input -> 
                                if(!partida.finalizada) { 
                                    val idx = partidas.indexOfFirst { it.id == partida.id }
                                    if(idx != -1) { 
                                        val num = input.filter { it.isDigit() }.toIntOrNull()
                                        partidas[idx] = partidas[idx].copy(golsMandante = num) 
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
                                    val idx = partidas.indexOfFirst { it.id == partida.id }
                                    if(idx != -1) { 
                                        val num = input.filter { it.isDigit() }.toIntOrNull()
                                        partidas[idx] = partidas[idx].copy(golsVisitante = num) 
                                    } 
                                } 
                            }, 
                            modifier = Modifier.width(60.dp), 
                            enabled = !partida.finalizada, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                            textStyle = TextStyle(textAlign = TextAlign.Center)
                        )
                        
                        Text(v, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { 
                            val idx = partidas.indexOfFirst { it.id == partida.id }
                            if (idx != -1) {
                                val gM = partidas[idx].golsMandante ?: 0
                                val gV = partidas[idx].golsVisitante ?: 0
                                partidas[idx] = partidas[idx].copy(
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
    }
}
