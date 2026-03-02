package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaInicialMenu(onNavegar: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ScoreBoard Pro",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(30.dp))

        BotaoMenu("Cadastrar campeonato") { onNavegar("cadastrar_campeonato") }
        BotaoMenu("Gerenciar campeonato") { onNavegar("gerenciar_campeonato") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar equipe") { onNavegar("cadastrar_equipe") }
        BotaoMenu("Gerenciar equipe") { onNavegar("gerenciar_equipe") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar jogador") { onNavegar("cadastrar_jogador") }
        BotaoMenu("Gerenciar jogador") { onNavegar("gerenciar_jogador") }
    }
}
