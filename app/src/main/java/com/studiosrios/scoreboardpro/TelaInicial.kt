package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicialTelespectador(
    listaC: List<CampeonatoSalvo>,
    onAbrirCampeonato: (CampeonatoSalvo) -> Unit,
    onEntrarComoOrganizador: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val campeonatosFiltrados = listaC.filter { 
        it.nomeExibicao.contains(busca, ignoreCase = true) || it.modelo.contains(busca, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsSoccer, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("ScoreBoard Pro", fontWeight = FontWeight.Black)
                        }
                    },
                    actions = {
                        TextButton(onClick = onEntrarComoOrganizador) {
                            Text("ORGANIZAR", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Buscar campeonato...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (listaC.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nenhum campeonato ativo", color = Color.Gray)
                        Text("Os organizadores ainda não criaram competições.", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            } else {
                Text(
                    "COMPETIÇÕES DISPONÍVEIS",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(campeonatosFiltrados) { camp ->
                        CardTelespectador(camp) { onAbrirCampeonato(camp) }
                    }
                }
            }
        }
    }
}

@Composable
fun CardTelespectador(camp: CampeonatoSalvo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = camp.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                // Badge de Modelo
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        camp.modelo.take(15),
                        color = Color.White,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = camp.nomeExibicao,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SportsSoccer, 
                        null, 
                        modifier = Modifier.size(12.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${camp.equipes.size} Equipes",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
