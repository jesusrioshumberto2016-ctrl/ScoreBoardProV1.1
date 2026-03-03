package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    
    // Gera a lista de slots disponíveis (ex: 1º Grupo A, 2º Grupo A...)
    val slotsDisponiveis = remember(listaGrupos) {
        val lista = mutableListOf<String>()
        listaGrupos.forEach { grupo ->
            for (i in 1..grupo.qtdClassificados) {
                lista.add("${i}º ${grupo.nome}")
            }
        }
        lista
    }

    // Estado para os confrontos definidos pelo usuário
    // Inicialmente, podemos tentar um sorteio automático ou deixar vazio para o usuário preencher
    val confrontos = remember { 
        mutableStateListOf<Pair<String, String>>().apply {
            // Tenta criar pares iniciais automáticos (1º A vs 2º B, etc) se possível
            for (i in 0 until totalVagas / 2) {
                val s1 = if (i * 2 < slotsDisponiveis.size) slotsDisponiveis[i * 2] else "TBD"
                val s2 = if (i * 2 + 1 < slotsDisponiveis.size) slotsDisponiveis[i * 2 + 1] else "TBD"
                add(Pair(s1, s2))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuração do Chaveamento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Total de classificados: $totalVagas", style = MaterialTheme.typography.bodyLarge)
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Jogos de Ida e Volta", fontWeight = FontWeight.Bold)
                    Text("Válido para todo o Mata-Mata", style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = idaEVolta, onCheckedChange = { idaEVolta = it })
            }
        }

        Text("Defina os confrontos (Oitavas/Quartas):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(confrontos) { index, par ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MenuSelecaoSlot(
                                selecionado = par.first,
                                opcoes = slotsDisponiveis,
                                onSelecionar = { confrontos[index] = confrontos[index].copy(first = it) }
                            )
                        }
                        
                        Text(" VS ", fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            MenuSelecaoSlot(
                                selecionado = par.second,
                                opcoes = slotsDisponiveis,
                                onSelecionar = { confrontos[index] = confrontos[index].copy(second = it) }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onConfirmar(idaEVolta, confrontos.toList()) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = totalVagas % 2 == 0 && totalVagas > 0
        ) {
            Text("PRÓXIMO: SELECIONAR EQUIPES")
        }
        
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("VOLTAR")
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
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(selecionado, fontSize = 11.sp, maxLines = 1)
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}
