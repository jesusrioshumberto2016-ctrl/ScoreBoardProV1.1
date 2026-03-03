package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaConfiguracaoChaveamento(
    listaGrupos: List<ConfigGrupo>,
    onVoltar: () -> Unit,
    onConfirmar: (Boolean, List<Pair<String, String>>) -> Unit
) {
    var idaEVolta by remember { mutableStateOf(true) }
    val totalVagas = listaGrupos.sumOf { it.qtdClassificados }
    
    // Slots reais vindo dos grupos
    val slotsReais = remember(listaGrupos) {
        val lista = mutableListOf<String>()
        listaGrupos.forEach { grupo ->
            for (i in 1..grupo.qtdClassificados) {
                lista.add("${i}º ${grupo.nome}")
            }
        }
        lista
    }

    // Slots das fases seguintes (Vencedores dos confrontos)
    val numJogosPrimeiraFase = totalVagas / 2
    val slotsVencedores = List(numJogosPrimeiraFase) { "Vence J${it + 1}" }
    
    val numJogosSegundaFase = numJogosPrimeiraFase / 2
    val slotsVencedoresSegundaFase = List(numJogosSegundaFase) { "Vence QF${it + 1}" }

    val todasOpcoes = listOf("TBD") + slotsReais + slotsVencedores + slotsVencedoresSegundaFase

    val totalJogos = calcularTotalJogos(totalVagas)
    val confrontos = remember { 
        mutableStateListOf<Pair<String, String>>().apply {
            repeat(totalJogos) {
                add(Pair("TBD", "TBD"))
            }
        }
    }

    fun selecionarSlot(index: Int, isFirst: Boolean, novoSlot: String) {
        if (novoSlot != "TBD") {
            for (i in confrontos.indices) {
                if (confrontos[i].first == novoSlot) confrontos[i] = confrontos[i].copy(first = "TBD")
                if (confrontos[i].second == novoSlot) confrontos[i] = confrontos[i].copy(second = "TBD")
            }
        }
        if (isFirst) confrontos[index] = confrontos[index].copy(first = novoSlot)
        else confrontos[index] = confrontos[index].copy(second = novoSlot)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuração Completa do Chaveamento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Defina todos os confrontos até a grande final.", fontSize = 12.sp, color = Color.Gray)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Jogos de Ida e Volta (Mata-Mata)", fontWeight = FontWeight.Bold)
                Switch(checked = idaEVolta, onCheckedChange = { idaEVolta = it })
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(confrontos) { index, par ->
                val faseInfo = obterNomeFaseEConfronto(index, totalVagas)
                
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(faseInfo.first, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text(faseInfo.second, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MenuSelecaoSlot(selecionado = par.first, opcoes = todasOpcoes, onSelecionar = { selecionarSlot(index, true, it) })
                                }
                                Text(" x ", fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.weight(1f)) {
                                    MenuSelecaoSlot(selecionado = par.second, opcoes = todasOpcoes, onSelecionar = { selecionarSlot(index, false, it) })
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onVoltar, modifier = Modifier.weight(1f)) {
                Text("VOLTAR")
            }
            Button(onClick = { onConfirmar(idaEVolta, confrontos.toList()) }, modifier = Modifier.weight(2f), enabled = totalVagas > 0) {
                Text("CONFIRMAR")
            }
        }
    }
}

@Composable
fun MenuSelecaoSlot(selecionado: String, opcoes: List<String>, onSelecionar: (String) -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selecionado == "TBD") Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = selecionado,
                fontSize = 11.sp,
                maxLines = 1,
                fontWeight = if (selecionado == "TBD") FontWeight.Normal else FontWeight.Bold
            )
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao, fontSize = 12.sp) },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

fun calcularTotalJogos(vagas: Int): Int {
    var total = 0
    var atual = vagas
    while (atual > 1) {
        atual /= 2
        total += atual
    }
    return total
}

fun obterNomeFaseEConfronto(index: Int, vagas: Int): Pair<String, String> {
    var count = 0
    var faseVagas = vagas
    val nomesFases = listOf("PRIMEIRA FASE", "QUARTAS DE FINAL", "SEMIFINAIS", "GRANDE FINAL")
    var faseIndex = 0
    
    while (faseVagas > 1) {
        val jogosNaFase = faseVagas / 2
        if (index < count + jogosNaFase) {
            val numNoFase = index - count + 1
            val prefixo = when(faseVagas) {
                16 -> "Oitavas"
                8 -> "Quartas"
                4 -> "Semi"
                else -> "Jogo"
            }
            return Pair(nomesFases.getOrElse(faseIndex) { "FASE ADICIONAL" }, "$prefixo $numNoFase")
        }
        count += jogosNaFase
        faseVagas /= 2
        faseIndex++
    }
    return Pair("FINAL", "Grande Final")
}
