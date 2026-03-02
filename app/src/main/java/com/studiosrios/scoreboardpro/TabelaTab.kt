package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabelaTab(equipes: List<EquipeExemplo>, partidas: List<Partida>) {
    // Chamando a lógica centralizada no MatchLogic (Cálculo puro)
    val classificacao = calcularClassificacao(equipes, partidas)

    Column(Modifier.fillMaxSize()) {
        // Cabeçalho da Tabela
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                "EQUIPE",
                Modifier.weight(2.5f),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            listOf("P", "J", "V", "E", "D", "GM", "GS", "SG").forEach {
                Text(
                    it,
                    Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Corpo da Tabela com a lista de classificação
        LazyColumn(Modifier.weight(1f)) {
            items(classificacao) { linha ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        linha.nome,
                        Modifier.weight(2.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    listOf(
                        linha.pontos,
                        linha.jogos,
                        linha.vitorias,
                        linha.empates,
                        linha.derrotas,
                        linha.gm,
                        linha.gs,
                        linha.sg
                    ).forEach {
                        Text(
                            it.toString(),
                            Modifier.weight(1f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Divider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}
