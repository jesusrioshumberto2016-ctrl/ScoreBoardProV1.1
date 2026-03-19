package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun TelaArtilharia(
    equipes: List<EquipeExemplo>,
    partidas: List<Partida>,
    listaGlobalJogadores: List<JogadorExemplo>,
    subAbaSelecionada: Int = 0,
    onSubAbaSelecionadaChange: (Int) -> Unit = {},
    onEquipeClick: (EquipeExemplo) -> Unit = {},
    onJogadorClick: (JogadorExemplo) -> Unit = {}
) {
    val titulosSubAbas = listOf("Artilheiros", "Assistentes", "Pontuação", "Gols Pênalti", "Amarelos", "Vermelhos", "Gols Contra")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = subAbaSelecionada,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            titulosSubAbas.forEachIndexed { index, titulo ->
                Tab(
                    selected = subAbaSelecionada == index,
                    onClick = { onSubAbaSelecionadaChange(index) },
                    text = { Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            val estatisticas = processarEstatisticas(partidas, listaGlobalJogadores, equipes)
            
            val listaExibicao = when (subAbaSelecionada) {
                0 -> estatisticas.artilheiros
                1 -> estatisticas.assistentes
                2 -> estatisticas.rankingPontuacao
                3 -> estatisticas.golsPenalti
                4 -> estatisticas.amarelos
                5 -> estatisticas.vermelhos
                6 -> estatisticas.golsContra
                else -> emptyList()
            }

            val labelUnidade = when (subAbaSelecionada) {
                0 -> "Gols"
                1 -> "Assist."
                2 -> "Pts"
                3 -> "Gols"
                4 -> "Cartões"
                5 -> "Cartões"
                6 -> "Gols"
                else -> ""
            }

            if (listaExibicao.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum registro encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(listaExibicao) { item ->
                        CardEstatistica(item, equipes, listaGlobalJogadores, labelUnidade, onEquipeClick, onJogadorClick)
                    }
                }
            }
        }
    }
}

@Composable
fun CardEstatistica(
    item: ItemEstatistica, 
    equipes: List<EquipeExemplo>, 
    jogadores: List<JogadorExemplo>,
    unidade: String, 
    onEquipeClick: (EquipeExemplo) -> Unit = {},
    onJogadorClick: (JogadorExemplo) -> Unit = {}
) {
    // Busca robusta pelo jogador para evitar crash
    val jogadorObj = jogadores.find { it.nome == item.nomeJogador || (it.apelido.isNotBlank() && it.apelido == item.apelido) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = jogadorObj != null) { 
                jogadorObj?.let { onJogadorClick(it) } 
            },
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Foto do Jogador
                AsyncImage(
                    model = item.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    // PRIORIZA O APELIDO NA EXIBIÇÃO
                    Text(
                        text = item.apelido.ifBlank { item.nomeJogador }, 
                        style = MaterialTheme.typography.bodyLarge, 
                        fontWeight = FontWeight.Bold
                    )
                    val equipeObj = equipes.find { it.nome == item.nomeEquipe }
                    val nomeEquipe = equipeObj?.nome ?: item.nomeEquipe
                    Text(
                        text = nomeEquipe, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(enabled = equipeObj != null) { equipeObj?.let { onEquipeClick(it) } }
                    )
                }
            }
            Text(
                text = if(unidade == "Pts") String.format(Locale.US, "%.1f %s", item.quantidadeFloat, unidade) 
                       else "${item.quantidade} $unidade", 
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class ItemEstatistica(
    val nomeJogador: String, 
    val apelido: String, 
    val nomeEquipe: String, 
    val quantidade: Int, 
    val fotoUri: String,
    val quantidadeFloat: Double = 0.0 // Para pontuação
)

data class EstatisticasGerais(
    val artilheiros: List<ItemEstatistica>,
    val assistentes: List<ItemEstatistica>,
    val golsPenalti: List<ItemEstatistica>,
    val amarelos: List<ItemEstatistica>,
    val vermelhos: List<ItemEstatistica>,
    val golsContra: List<ItemEstatistica>,
    val rankingPontuacao: List<ItemEstatistica> // Nova categoria
)

fun processarEstatisticas(partidas: List<Partida>, jogadores: List<JogadorExemplo>, equipes: List<EquipeExemplo>): EstatisticasGerais {
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
        val jog = jogadores.find { j -> j.nome == it.key }
        val foto = jog?.fotoUri ?: ""
        val apelido = jog?.apelido ?: ""
        ItemEstatistica(it.key, apelido, mapaEquipe[it.key] ?: "S/E", it.value, foto)
    }.sortedByDescending { it.quantidade }

    // Cálculo do Ranking de Pontuação
    val rankingPontuacao = jogadores.map { jogador ->
        // GARANTE PASSAGEM DA LISTA DE EQUIPES PARA A FUNÇÃO DE CÁLCULO
        val pts = calcularPontuacaoJogador(jogador, partidas, equipes)
        ItemEstatistica(
            nomeJogador = jogador.nome,
            apelido = jogador.apelido,
            nomeEquipe = equipes.find { it.id == jogador.equipeId }?.nome ?: "Sem Time",
            quantidade = pts.total.toInt(),
            fotoUri = jogador.fotoUri,
            quantidadeFloat = pts.total
        )
    }.filter { it.quantidadeFloat != 0.0 }.sortedByDescending { it.quantidadeFloat }

    return EstatisticasGerais(
        artilheiros = converter(mapaGols),
        assistentes = converter(mapaAssist),
        golsPenalti = converter(mapaPenalti),
        amarelos = converter(mapaAmarelos),
        vermelhos = converter(mapaVermelhos),
        golsContra = converter(mapaGolsContra),
        rankingPontuacao = rankingPontuacao
    )
}
