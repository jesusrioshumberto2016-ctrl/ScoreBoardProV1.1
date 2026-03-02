package com.studiosrios.scoreboardpro // 'p' minúsculo corrigido

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaTabelaRanking(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    configs: ConfiguracoesCampeonato
) {
    // Realiza o cálculo do ranking usando a lógica do Brasileirão
    val ranking = BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // CABEÇALHO DA TABELA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text("Equipe", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("P", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("J", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("V", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("SG", modifier = Modifier.width(35.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        // LINHAS DA TABELA
        ranking.forEach { linha ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 4.dp)
            ) {
                // Nome da Equipe (Ajustado para o seu Models.kt)
                Text(
                    text = linha.nome,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    maxLines = 1
                )

                // Pontos (P)
                Text(
                    text = "${linha.pontos}",
                    modifier = Modifier.width(30.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // Jogos (J)
                Text(
                    text = "${linha.jogos}",
                    modifier = Modifier.width(30.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                // Vitórias (V)
                Text(
                    text = "${linha.vitorias}",
                    modifier = Modifier.width(30.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                // Saldo de Gols (Ajustado para o seu Models.kt: linha.sg)
                Text(
                    text = "${linha.sg}",
                    modifier = Modifier.width(35.dp),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = if (linha.sg > 0) Color(0xFF2E7D32) else if (linha.sg < 0) Color.Red else Color.Unspecified
                )
            }
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
