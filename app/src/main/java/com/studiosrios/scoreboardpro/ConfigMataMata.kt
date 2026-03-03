package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConfigMataMata(
    configs: ConfiguracoesCampeonato,
    onSalvar: (ConfiguracoesCampeonato) -> Unit
) {
    var idaEVolta by remember { mutableStateOf(configs.modoReturno) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configurações: Mata-Mata", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Confrontos: Ida e Volta", fontWeight = FontWeight.Bold)
                    Text("Aplicar em todas as fases eliminatórias", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = idaEVolta, onCheckedChange = { idaEVolta = it })
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onSalvar(configs.copy(modoReturno = idaEVolta)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SALVAR ALTERAÇÕES")
        }
    }
}
