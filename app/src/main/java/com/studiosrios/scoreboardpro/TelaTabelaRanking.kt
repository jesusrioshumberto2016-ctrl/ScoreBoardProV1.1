package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun TelaTabelaRanking(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    configs: ConfiguracoesCampeonato
) {
    // Realiza o cálculo do ranking usando a lógica do Brasileirão
    val ranking = BrasileiraoSerieA().calcularRanking(equipes, partidas, configs)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // ÁREA SCROLLÁVEL DA TABELA
        Column(modifier = Modifier.horizontalScroll(scrollState)) {
            // CABEÇALHO DA TABELA
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Equipe", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                ColunaCabecalho("P")
                ColunaCabecalho("J")
                ColunaCabecalho("V")
                ColunaCabecalho("E")
                ColunaCabecalho("D")
                ColunaCabecalho("GM")
                ColunaCabecalho("GS")
                ColunaCabecalho("SG")
                if (configs.exibirCartoesNaTabela) {
                    ColunaCabecalho("CA")
                    ColunaCabecalho("CV")
                }
            }

            // LINHAS DA TABELA
            ranking.forEachIndexed { index, linha ->
                val equipeOriginal = equipes.find { it.id == linha.equipeId }
                
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Posição
                    Text(
                        text = "${index + 1}º",
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(index) {
                            0 -> Color(0xFFB79400) // Ouro
                            1 -> Color(0xFF757575) // Prata
                            2 -> Color(0xFF8D6E63) // Bronze
                            else -> Color.DarkGray
                        }
                    )

                    // Escudo + Nome da Equipe
                    Row(
                        modifier = Modifier.width(150.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = equipeOriginal?.escudoUri?.ifBlank { R.drawable.ic_launcher_background } ?: R.drawable.ic_launcher_background,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(0.5.dp, Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = linha.nome,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    ColunaDados("${linha.pontos}", bold = true)
                    ColunaDados("${linha.jogos}")
                    ColunaDados("${linha.vitorias}")
                    ColunaDados("${linha.empates}")
                    ColunaDados("${linha.derrotas}")
                    ColunaDados("${linha.gm}")
                    ColunaDados("${linha.gs}")
                    ColunaDados("${linha.sg}", color = if (linha.sg > 0) Color(0xFF2E7D32) else if (linha.sg < 0) Color.Red else Color.Unspecified)
                    
                    if (configs.exibirCartoesNaTabela) {
                        ColunaDados("${linha.amarelos}", color = Color(0xFFB79400))
                        ColunaDados("${linha.vermelhos}", color = Color.Red)
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
fun ColunaCabecalho(texto: String) {
    Text(
        text = texto,
        modifier = Modifier.width(35.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ColunaDados(texto: String, bold: Boolean = false, color: Color = Color.Unspecified) {
    Text(
        text = texto,
        modifier = Modifier.width(35.dp),
        fontSize = 13.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        color = color
    )
}
