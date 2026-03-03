package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    val opcoesComTbd = listOf("TBD") + slotsReais

    // Estado dos confrontos iniciais
    val confrontos = remember { 
        mutableStateListOf<Pair<String, String>>().apply {
            for (i in 0 until totalVagas / 2) {
                add(Pair("TBD", "TBD"))
            }
        }
    }

    // Lógica para garantir que um time só apareça uma vez
    fun selecionarSlot(index: Int, isFirst: Boolean, novoSlot: String) {
        if (novoSlot != "TBD") {
            // Remove de qualquer outro lugar que já esteja
            for (i in confrontos.indices) {
                if (confrontos[i].first == novoSlot) confrontos[i] = confrontos[i].copy(first = "TBD")
                if (confrontos[i].second == novoSlot) confrontos[i] = confrontos[i].copy(second = "TBD")
            }
        }
        
        // Atualiza a posição desejada
        if (isFirst) {
            confrontos[index] = confrontos[index].copy(first = novoSlot)
        } else {
            confrontos[index] = confrontos[index].copy(second = novoSlot)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuração do Chaveamento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Defina os confrontos da 1ª fase eliminatória.", fontSize = 12.sp, color = Color.Gray)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jogos de Ida e Volta", fontWeight = FontWeight.Bold)
                Switch(checked = idaEVolta, onCheckedChange = { idaEVolta = it })
            }
        }

        Spacer(Modifier.height(16.dp))

        // Usamos LazyColumn para os confrontos
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text("CAMINHO ATÉ A FINAL", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(confrontos) { index, par ->
                val matchNum = index + 1
                // Agrupamento visual para mostrar quem enfrenta quem na próxima fase
                val proxFase = if (matchNum % 2 != 0) "Vence J$matchNum vs Vence J${matchNum + 1}" else null
                
                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("JOGO $matchNum", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MenuSelecaoSlot(
                                        selecionado = par.first,
                                        opcoes = opcoesComTbd,
                                        onSelecionar = { selecionarSlot(index, true, it) }
                                    )
                                }
                                Text(" x ", fontWeight = FontWeight.Bold)
                                Box(modifier = Modifier.weight(1f)) {
                                    MenuSelecaoSlot(
                                        selecionado = par.second,
                                        opcoes = opcoesComTbd,
                                        onSelecionar = { selecionarSlot(index, false, it) }
                                    )
                                }
                            }
                        }
                    }
                    
                    if (proxFase != null && matchNum < confrontos.size) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "↓ Caminho para Quartas ↓",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                ResumoFinal(confrontos)
                Spacer(Modifier.height(80.dp))
            }
        }

        Button(
            onClick = { onConfirmar(idaEVolta, confrontos.toList()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = totalVagas > 0
        ) {
            Text("CONFIRMAR CHAVEAMENTO")
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

@Composable
fun ResumoFinal(confrontos: List<Pair<String, String>>) {
    val totalMatches = confrontos.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text("ESTRUTURA DO TORNEIO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("• 1ª Fase Knockout: $totalMatches jogos", fontSize = 11.sp)
        if (totalMatches >= 4) Text("• Quartas de Final: ${totalMatches/2} jogos", fontSize = 11.sp)
        if (totalMatches >= 2) Text("• Semifinais: ${totalMatches/4} jogos", fontSize = 11.sp)
        Text("• Grande Final: 1 jogo", fontSize = 11.sp)
    }
}
