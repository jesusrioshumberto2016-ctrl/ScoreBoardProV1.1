package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaArtilharia(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>
) {
    var subAbaSelecionada by remember { mutableIntStateOf(0) }
    val titulosSubAbas = listOf("Artilheiros", "Assistentes", "Gols Pênalti", "Amarelos", "Vermelhos", "Gols Contra")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = subAbaSelecionada,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            titulosSubAbas.forEachIndexed { index, titulo ->
                Tab(
                    selected = subAbaSelecionada == index,
                    onClick = { subAbaSelecionada = index },
                    text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            val estatisticas = processarEstatisticas(partidas, listaGlobalJogadores)
            
            val listaExibicao = when (subAbaSelecionada) {
                0 -> estatisticas.artilheiros
                1 -> estatisticas.assistentes
                2 -> estatisticas.golsPenalti
                3 -> estatisticas.amarelos
                4 -> estatisticas.vermelhos
                5 -> estatisticas.golsContra
                else -> emptyList()
            }

            val labelUnidade = when (subAbaSelecionada) {
                0 -> "Gols"
                1 -> "Assist."
                2 -> "Gols"
                3 -> "Cartões"
                4 -> "Cartões"
                5 -> "Gols"
                else -> ""
            }

            if (listaExibicao.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(listaExibicao) { item ->
                        CardEstatistica(item, equipes, labelUnidade)
                    }
                }
            }
        }
    }
}

@Composable
fun CardEstatistica(item: ItemEstatistica, equipes: List<EquipeExemplo>, unidade: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(item.nomeJogador, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                val nomeEquipe = equipes.find { it.nome == item.nomeEquipe }?.nome ?: item.nomeEquipe
                Text(nomeEquipe, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Text("${item.quantidade} $unidade", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

data class ItemEstatistica(val nomeJogador: String, val nomeEquipe: String, val quantidade: Int)
data class EstatisticasGerais(
    val artilheiros: List<ItemEstatistica>,
    val assistentes: List<ItemEstatistica>,
    val golsPenalti: List<ItemEstatistica>,
    val amarelos: List<ItemEstatistica>,
    val vermelhos: List<ItemEstatistica>,
    val golsContra: List<ItemEstatistica>
)

fun processarEstatisticas(partidas: List<Partida>, jogadores: List<JogadorExemplo>): EstatisticasGerais {
    val mapaGols = mutableMapOf<String, Int>()
    val mapaAssist = mutableMapOf<String, Int>()
    val mapaPenalti = mutableMapOf<String, Int>()
    val mapaAmarelos = mutableMapOf<String, Int>()
    val mapaVermelhos = mutableMapOf<String, Int>()
    val mapaGolsContra = mutableMapOf<String, Int>()
    
    val mapaEquipe = mutableMapOf<String, String>()

    partidas.forEach { p ->
        p.eventos.forEach { ev ->
            val chave = ev.jogadorNome
            if (chave.isNotBlank() && chave != "Partida") {
                mapaEquipe[chave] = ev.equipeNome
                when (ev.tipo) {
                    "GOL" -> mapaGols[chave] = (mapaGols[chave] ?: 0) + 1
                    "GOL (PÊNALTI)" -> {
                        mapaGols[chave] = (mapaGols[chave] ?: 0) + 1
                        mapaPenalti[chave] = (mapaPenalti[chave] ?: 0) + 1
                    }
                    "ASSISTÊNCIA" -> mapaAssist[chave] = (mapaAssist[chave] ?: 0) + 1
                    "YELLOW CARD" -> mapaAmarelos[chave] = (mapaAmarelos[chave] ?: 0) + 1
                    "RED CARD" -> mapaVermelhos[chave] = (mapaVermelhos[chave] ?: 0) + 1
                    "GOL CONTRA" -> mapaGolsContra[chave] = (mapaGolsContra[chave] ?: 0) + 1
                }
            }
        }
    }

    fun converter(mapa: Map<String, Int>) = mapa.map { 
        ItemEstatistica(it.key, mapaEquipe[it.key] ?: "S/E", it.value) 
    }.sortedByDescending { it.quantidade }

    return EstatisticasGerais(
        artilheiros = converter(mapaGols),
        assistentes = converter(mapaAssist),
        golsPenalti = converter(mapaPenalti),
        amarelos = converter(mapaAmarelos),
        vermelhos = converter(mapaVermelhos),
        golsContra = converter(mapaGolsContra)
    )
}
