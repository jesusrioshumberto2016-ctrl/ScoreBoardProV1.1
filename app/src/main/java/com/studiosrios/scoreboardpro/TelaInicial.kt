package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        it.nomeExibicao.contains(busca, ignoreCase = true)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicialMenu(
    listaC: List<CampeonatoSalvo>,
    onAbrirCamp: (CampeonatoSalvo) -> Unit,
    onNavegar: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Menu do Organizador", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("AÇÕES RÁPIDAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ItemMenuRapido("Novo Camp.", Icons.Default.Add, Color(0xFF1976D2)) { onNavegar("cadastrar_campeonato") }
                ItemMenuRapido("Novo Jogador", Icons.Default.PersonAdd, Color(0xFF388E3C)) { onNavegar("cadastrar_jogador") }
                ItemMenuRapido("Nova Equipe", Icons.Default.Groups, Color(0xFFFBC02D)) { onNavegar("cadastrar_equipe") }
            }

            Spacer(Modifier.height(32.dp))
            Text("GERENCIAMENTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            CardGerenciamento("Meus Campeonatos", "Ver e editar torneios salvos", Icons.Default.EmojiEvents) { onNavegar("gerenciar_campeonato") }
            CardGerenciamento("Banco de Jogadores", "Gerenciar todos os atletas", Icons.Default.PersonAdd) { onNavegar("gerenciar_jogador") }
            CardGerenciamento("Gestão de Equipes", "Editar elencos e informações", Icons.Default.Groups) { onNavegar("gerenciar_equipe") }
        }
    }
}

@Composable
fun ItemMenuRapido(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CardGerenciamento(titulo: String, sub: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(sub, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CardTelespectador(camp: CampeonatoSalvo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
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
                    .height(130.dp),
                contentScale = ContentScale.Crop
            )
            
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
