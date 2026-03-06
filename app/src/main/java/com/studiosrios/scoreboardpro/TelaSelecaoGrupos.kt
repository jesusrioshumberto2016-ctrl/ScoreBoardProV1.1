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
fun TelaSelecaoGrupos(
    onVoltar: () -> Unit,
    onConfirmar: (List<ConfigGrupo>, Boolean) -> Unit
) {
    // Começamos com 2 grupos por padrão
    var qtdGrupos by remember { mutableIntStateOf(2) }
    val listaConfigs = remember { mutableStateListOf<ConfigGrupo>() }
    var modoReturno by remember { mutableStateOf(false) }

    // Atualiza a lista sempre que a quantidade de grupos mudar
    LaunchedEffect(qtdGrupos) {
        if (listaConfigs.size < qtdGrupos) {
            for (i in listaConfigs.size until qtdGrupos) {
                listaConfigs.add(ConfigGrupo(nome = "Grupo ${(i + 65).toChar()}"))
            }
        } else if (listaConfigs.size > qtdGrupos) {
            while (listaConfigs.size > qtdGrupos) {
                listaConfigs.removeLast()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuração por Grupo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // Seletor de quantidade total de grupos
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Total de Grupos: ")
            IconButton(onClick = { if (qtdGrupos > 1) qtdGrupos-- }) { Text("-", fontSize = 20.sp) }
            Text("$qtdGrupos", fontWeight = FontWeight.Bold)
            IconButton(onClick = { if (qtdGrupos < 12) qtdGrupos++ }) { Text("+", fontSize = 20.sp) }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Row(
                Modifier.padding(16.dp), 
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Fase de Grupos: Ida e Volta", fontWeight = FontWeight.Bold)
                    Text("Turno e returno nos grupos", style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = modoReturno, onCheckedChange = { modoReturno = it })
            }
        }

        Divider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(listaConfigs) { index, grupo ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(grupo.nome, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            // Controle de Times
                            ItemPersonalizado(
                                label = "Times",
                                valor = grupo.qtdTimes,
                                onMudar = { novaQtd ->
                                    listaConfigs[index] = listaConfigs[index].copy(qtdTimes = novaQtd)
                                    if (listaConfigs[index].qtdClassificados > novaQtd) {
                                        listaConfigs[index] = listaConfigs[index].copy(qtdClassificados = novaQtd)
                                    }
                                },
                                min = 2
                            )

                            // Controle de Classificados
                            ItemPersonalizado(
                                label = "Vagas",
                                valor = grupo.qtdClassificados,
                                onMudar = { novaQtd ->
                                    listaConfigs[index] = listaConfigs[index].copy(qtdClassificados = novaQtd)
                                },
                                min = 1,
                                max = listaConfigs[index].qtdTimes
                            )
                        }
                    }
                }
            }
        }

        val totalTimesNecessarios = listaConfigs.sumOf { it.qtdTimes }

        Button(
            onClick = { onConfirmar(listaConfigs.toList(), modoReturno) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("PRÓXIMO: ESCOLHER $totalTimesNecessarios TIMES")
        }

        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

@Composable
fun ItemPersonalizado(label: String, valor: Int, onMudar: (Int) -> Unit, min: Int, max: Int = 10) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (valor > min) onMudar(valor - 1) }) { Text("-") }
            Text("$valor", fontWeight = FontWeight.Bold)
            IconButton(onClick = { if (valor < max) onMudar(valor + 1) }) { Text("+") }
        }
    }
}
