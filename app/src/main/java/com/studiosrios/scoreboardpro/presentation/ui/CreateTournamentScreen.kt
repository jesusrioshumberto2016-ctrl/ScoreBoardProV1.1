package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
// Esse import só para de dar erro DEPOIS do Sync do Gradle que falei acima
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateTournamentScreen() {
    var tournamentName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configurar ScoreBoard Pro", fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = tournamentName,
            onValueChange = { tournamentName = it },
            label = { Text("Nome do Torneio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botão de ícone para foto
        IconButton(
            onClick = { /* Aqui vai abrir a galeria depois */ },
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Adicionar Foto",
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text("Adicionar Logo do Torneio", fontSize = 12.sp)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Salvar */ },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("GERAR CAMPEONATO")
        }
    }
}
