package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TelaConfiguracoesCampeonato(
    configs: ConfiguracoesCampeonato,
    onSalvar: (ConfiguracoesCampeonato) -> Unit
) {
    var modoReturno by remember { mutableStateOf(configs.modoReturno) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configurações do Campeonato",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Modo Turno e Returno",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "As equipes jogam duas vezes (casa e fora)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = modoReturno,
                onCheckedChange = { modoReturno = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSalvar(configs.copy(modoReturno = modoReturno)) },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SALVAR ALTERAÇÕES", fontWeight = FontWeight.Bold)
        }
    }
}
