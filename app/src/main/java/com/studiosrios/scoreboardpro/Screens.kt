package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicialMenu(
    listaC: List<CampeonatoSalvo>,
    onAbrirCamp: (CampeonatoSalvo) -> Unit,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavegar("cadastrar_campeonato") },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Novo Campeonato")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("ScoreBoard Pro", fontWeight = FontWeight.Black, fontSize = 22.sp) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                "MEUS CAMPEONATOS",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (listaC.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                        .background(Color.LightGray.copy(0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum campeonato registrado", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                ) {
                    items(listaC) { camp ->
                        ItemCardCampeonato(camp) { onAbrirCamp(camp) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "GERENCIAMENTO GERAL",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardMenuRapido("EQUIPES", Modifier.weight(1f)) { onNavegar("gerenciar_equipe") }
                    CardMenuRapido("JOGADORES", Modifier.weight(1f)) { onNavegar("gerenciar_jogador") }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardMenuRapido("NOVA EQUIPE", Modifier.weight(1f)) { onNavegar("cadastrar_equipe") }
                    CardMenuRapido("NOVO JOGADOR", Modifier.weight(1f)) { onNavegar("cadastrar_jogador") }
                }
            }
        }
    }
}

@Composable
fun ItemCardCampeonato(camp: CampeonatoSalvo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = camp.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = camp.nomeExibicao,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = camp.modelo,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${camp.equipes.size} TIMES",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CardMenuRapido(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}
