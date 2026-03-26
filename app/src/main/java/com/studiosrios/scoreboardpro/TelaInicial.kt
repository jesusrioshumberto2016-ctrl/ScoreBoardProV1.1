package com.studiosrios.scoreboardpro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                            Text("ScoreBoard Pro", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    },
                    actions = {
                        Button(
                            onClick = onEntrarComoOrganizador,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("ORGANIZAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Buscar competições em andamento...") },
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
        if (listaC.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("Nenhum campeonato ativo", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text("Acompanhe aqui as melhores competições em breve.", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                // Se houver pelo menos um campeonato e nenhuma busca ativa, mostramos o primeiro como destaque
                if (campeonatosFiltrados.isNotEmpty() && busca.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        CardDestaqueTelespectador(campeonatosFiltrados.first()) { onAbrirCampeonato(campeonatosFiltrados.first()) }
                    }
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            "OUTRAS COMPETIÇÕES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                val listaRestante = if (busca.isEmpty() && campeonatosFiltrados.isNotEmpty()) {
                    campeonatosFiltrados.drop(1)
                } else {
                    campeonatosFiltrados
                }

                items(listaRestante) { camp ->
                    CardTelespectador(camp) { onAbrirCampeonato(camp) }
                }
            }
        }
    }
}

@Composable
fun CardDestaqueTelespectador(camp: CampeonatoSalvo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box {
            AsyncImage(
                model = camp.fotoUri.ifBlank { R.drawable.logo_login }, 
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradiente para leitura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "AO VIVO / EM ALTA",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = camp.nomeExibicao.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${camp.equipes.size} Equipes participando",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
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
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = camp.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(Modifier.padding(10.dp)) {
                Text(
                    text = camp.nomeExibicao,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SportsSoccer, 
                        null, 
                        modifier = Modifier.size(12.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = camp.modelo,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Ver detalhes",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
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
