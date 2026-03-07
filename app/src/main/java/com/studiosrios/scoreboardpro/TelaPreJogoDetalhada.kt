package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun TelaPreJogoDetalhada(
    partida: Partida,
    equipes: List<EquipeExemplo>,
    todosJogadores: List<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("CAMPO", "ÁRBITROS")

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVoltar) { Icon(Icons.Default.ArrowBack, "Voltar") }
            Text(
                text = "INFORMAÇÕES DA PARTIDA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Seletor de Abas
        TabRow(selectedTabIndex = abaSelecionada, containerColor = Color(0xFFF5F5F5)) {
            titulos.forEachIndexed { index, titulo ->
                Tab(
                    selected = abaSelecionada == index,
                    onClick = { abaSelecionada = index },
                    text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (abaSelecionada) {
                0 -> {
                    // Reutiliza o componente de campo tático
                    CampoVisualEstiloSofa(partida, equipes, todosJogadores)
                }
                1 -> {
                    // Visualização de Arbitragem em modo leitura
                    Column(Modifier.fillMaxSize().padding(24.dp)) {
                        Text(
                            text = "EQUIPE DE ARBITRAGEM",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        InfoItemLeitura("Árbitro Principal", partida.arbitroPrincipal)
                        InfoItemLeitura("Assistente 1", partida.assistente1)
                        InfoItemLeitura("Assistente 2", partida.assistente2)
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            text = "COMISSÃO TÉCNICA",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        InfoItemLeitura("Técnico (Mandante)", partida.tecnicoMandante)
                        InfoItemLeitura("Técnico (Visitante)", partida.tecnicoVisitante)
                    }
                }
            }
        }

        Button(
            onClick = onVoltar, 
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
        ) {
            Text("VOLTAR PARA PARTIDAS")
        }
    }
}

@Composable
fun InfoItemLeitura(label: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(
                text = valor.ifBlank { "A definir" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (valor.isBlank()) Color.LightGray else Color.Black
            )
        }
    }
}
