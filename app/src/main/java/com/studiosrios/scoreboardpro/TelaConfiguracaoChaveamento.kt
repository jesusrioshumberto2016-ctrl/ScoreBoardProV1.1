package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaConfiguracaoChaveamento(
    totalVagas: Int,
    onVoltar: () -> Unit,
    onConfirmar: (Boolean) -> Unit
) {
    var idaEVolta by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Configuração do Chaveamento", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Total de classificados dos grupos: $totalVagas", style = MaterialTheme.typography.bodyLarge)
        Text("O chaveamento será gerado automaticamente (Ex: 1º vs 16º).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Jogos de Ida e Volta", fontWeight = FontWeight.Bold)
                    Text("No Mata-Mata", style = MaterialTheme.typography.labelSmall)
                }
                Switch(checked = idaEVolta, onCheckedChange = { idaEVolta = it })
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { onConfirmar(idaEVolta) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("PRÓXIMO: SELECIONAR EQUIPES")
        }
        
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("VOLTAR")
        }
    }
}
