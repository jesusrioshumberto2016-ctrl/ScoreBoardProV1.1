package com.studiosrios.scoreboardpro

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigTab(
    idCamp: Int,
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    configsAtuais: ConfiguracoesCampeonato,
    onConfigsChanged: (ConfiguracoesCampeonato) -> Unit,
    onSalvarGeral: (Int, ConfiguracoesCampeonato) -> Unit
) {
    val jaIniciou = partidas.any { it.finalizada }
    val contexto = LocalContext.current
    val totalEsperadoTurnoUnico = (equipes.size * (equipes.size - 1)) / 2

    val opcoesCriterios = listOf("Selecionar", "Confronto direto", "Vitórias", "Saldo de gols", "Gols marcados", "Menos cartões amarelos", "Menos cartões vermelhos")
    val titulosCriterios = listOf("Primeiro critério", "Segundo critério", "Terceiro critério", "Quarto critério", "Quinto critério", "Sexto critério")
    val expandedStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("CONFIGURAÇÕES DO TORNEIO", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(24.dp))
        Text("SISTEMA DE DISPUTA", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)

        // CORREÇÃO AQUI: De HorizontalDivider para Divider
        Divider(Modifier.padding(vertical = 8.dp))

        if (jaIniciou) {
            Surface(color = Color(0xFFFFEBEE), shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("O sistema de disputa não pode ser alterado pois já existem partidas finalizadas.", modifier = Modifier.padding(8.dp), fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
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
                        // O curativo entra aqui!
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

        // CORREÇÃO AQUI: De HorizontalDivider para Divider
        Divider(Modifier.padding(vertical = 8.dp))

        titulosCriterios.forEachIndexed { index, titulo ->
            ExposedDropdownMenuBox(expanded = expandedStates[index], onExpandedChange = { expandedStates[index] = !expandedStates[index] }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedTextField(value = configsAtuais.criteriosDesempate[index], onValueChange = {}, readOnly = true, label = { Text(titulo) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStates[index]) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expandedStates[index], onDismissRequest = { expandedStates[index] = false }) {
                    opcoesCriterios.forEach { opcao ->
                        DropdownMenuItem(text = { Text(opcao) }, onClick = {
                            val novaLista = configsAtuais.criteriosDesempate.toMutableList()
                            if (opcao != "Selecionar") {
                                val indexAntigo = novaLista.indexOf(opcao)
                                if (indexAntigo != -1 && indexAntigo != index) novaLista[indexAntigo] = "Selecionar"
                            }
                            novaLista[index] = opcao
                            onConfigsChanged(configsAtuais.copy(criteriosDesempate = novaLista))
                            expandedStates[index] = false
                        })
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = { onSalvarGeral(idCamp, configsAtuais); Toast.makeText(contexto, "Configurações Salvas!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) {
            Icon(Icons.Default.CheckCircle, null)
            Spacer(Modifier.width(8.dp))
            Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
        }
    }
}
