package com.studiosrios.scoreboardpro

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("prefs_exibicao", Context.MODE_PRIVATE) }
    
    // Estados para favoritos
    val idsFavoritos = remember { 
        mutableStateListOf<Int>().apply {
            addAll(prefs.getStringSet("ids_favoritos", emptySet())?.map { it.toInt() } ?: emptyList())
        }
    }

    var busca by remember { mutableStateOf("") }

    // Lógica de reordenação: Favoritos > Restante
    val listaOrdenada = remember(listaC, idsFavoritos.size, busca) {
        val filtrados = if (busca.isEmpty()) {
            listaC.sortedByDescending { it.id }
        } else {
            listaC.filter { it.nomeExibicao.contains(busca, ignoreCase = true) }
        }
        
        val favoritos = filtrados.filter { idsFavoritos.contains(it.id) }
        val outros = filtrados.filter { !idsFavoritos.contains(it.id) }
        
        favoritos + outros
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
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ORGANIZAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Buscar competições...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
            }
        }
    ) { padding ->
        if (listaC.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum campeonato ativo.", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(listaOrdenada, key = { it.id }) { camp ->
                    val isFavorito = idsFavoritos.contains(camp.id)

                    CardCampeonatoInterativo(
                        camp = camp,
                        isFavorito = isFavorito,
                        onToggleFavorito = {
                            if (isFavorito) {
                                idsFavoritos.remove(camp.id)
                                prefs.edit().putStringSet("ids_favoritos", idsFavoritos.map { it.toString() }.toSet()).apply()
                            } else if (idsFavoritos.size < 5) {
                                idsFavoritos.add(camp.id)
                                prefs.edit().putStringSet("ids_favoritos", idsFavoritos.map { it.toString() }.toSet()).apply()
                            } else {
                                Toast.makeText(context, "Limite de 5 favoritos atingido", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClick = { onAbrirCampeonato(camp) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardCampeonatoInterativo(
    camp: CampeonatoSalvo,
    isFavorito: Boolean,
    onToggleFavorito: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            AsyncImage(
                model = camp.fotoUri.ifBlank { R.drawable.logo_login },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))

            // Ícones de Ação (Topo)
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleFavorito, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isFavorito) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = if (isFavorito) Color.Cyan else Color.White
                    )
                }
            }

            // Info (Base)
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(camp.nomeExibicao, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${camp.equipes.size} Equipes • ${camp.modelo}", color = Color.LightGray, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaInicialMenu(
    listaC: List<CampeonatoSalvo>,
    currentUserId: String, // Adicionado para filtrar
    onAbrirCamp: (CampeonatoSalvo) -> Unit,
    onNavegar: (String) -> Unit
) {
    // Filtra a lista para mostrar apenas os campeonatos que o usuário criou
    val meusCampeonatos = remember(listaC, currentUserId) {
        listaC.filter { it.ownerId == currentUserId }
    }

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

            // Passamos a lista filtrada para a tela de gerenciamento quando navegada
            CardGerenciamento("Meus Campeonatos", "Ver e editar torneios salvos (${meusCampeonatos.size})", Icons.Default.EmojiEvents) { 
                onNavegar("gerenciar_campeonato") 
            }
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
