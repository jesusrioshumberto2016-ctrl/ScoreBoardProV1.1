package com.studiosrios.scoreboardpro

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.studiosrios.scoreboardpro.data.repository.DataRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSelecaoJogador(
    equipeAlvo: EquipeExemplo,
    listaTotal: SnapshotStateList<JogadorExemplo>,
    listaGlobalEquipes: SnapshotStateList<EquipeExemplo>,
    repository: DataRepository,
    onFinalizar: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    // Pegamos sempre a versão mais atual da equipe da lista global
    val equipeAtual = listaGlobalEquipes.find { it.id == equipeAlvo.id } ?: equipeAlvo

    val listaExibicao = remember(searchQuery, listaTotal, equipeAtual) {
        listaTotal.filter { jogador ->
            // Filtra pela busca
            val matchesSearch = jogador.nome.contains(searchQuery, ignoreCase = true) || 
                                jogador.apelido.contains(searchQuery, ignoreCase = true)
            
            // O jogador não deve estar JÁ nesta equipe sendo editada
            val jaEstaNestaEquipe = equipeAtual.jogadores.any { it.id == jogador.id }
            
            !jaEstaNestaEquipe && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar ao Elenco") },
                navigationIcon = {
                    IconButton(onClick = onFinalizar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar jogador...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "Elenco atual: ${equipeAtual.jogadores.size}/50 jogadores",
                fontSize = 12.sp,
                color = if (equipeAtual.jogadores.size >= 50) Color.Red else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(listaExibicao) { jogador ->
                    val jaTemEquipe = jogador.equipeId != -1
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .alpha(if (jaTemEquipe) 0.5f else 1f) // Efeito visual de bloqueado
                            .clickable(enabled = !jaTemEquipe) { // Bloqueia o clique se já tiver equipe
                                val eqIdx = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
                                if (eqIdx != -1) {
                                    val equipeSendoEditada = listaGlobalEquipes[eqIdx]
                                    
                                    if (equipeSendoEditada.jogadores.size >= 50) {
                                        Toast.makeText(ctx, "Limite de 50 jogadores atingido!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val novoElenco = equipeSendoEditada.jogadores + jogador
                                        val equipeAtualizada = equipeSendoEditada.copy(jogadores = novoElenco)
                                        
                                        // Atualiza na memória
                                        listaGlobalEquipes[eqIdx] = equipeAtualizada
                                        
                                        // Vincula o jogador à equipe na lista global
                                        val jogIdx = listaTotal.indexOfFirst { it.id == jogador.id }
                                        if (jogIdx != -1) {
                                            val jogadorAtualizado = listaTotal[jogIdx].copy(equipeId = equipeAtualizada.id)
                                            listaTotal[jogIdx] = jogadorAtualizado
                                            // Salva jogador atualizado
                                            repository.salvarJogador(jogadorAtualizado)
                                        }
                                        
                                        // Salva equipe atualizada (Offline-first + Sync)
                                        repository.salvarEquipe(equipeAtualizada)
                                        
                                        Toast.makeText(ctx, "${jogador.apelido.ifBlank { jogador.nome }} adicionado!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (jaTemEquipe) Color(0xFFF5F5F5) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(if (jaTemEquipe) 0.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = if (jogador.fotoUri.isBlank()) R.drawable.ic_launcher_background else jogador.fotoUri,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (jogador.apelido.isNotBlank()) "${jogador.nome} (${jogador.apelido})" else jogador.nome,
                                    fontWeight = FontWeight.Bold,
                                    color = if (jaTemEquipe) Color.Gray else Color.Black
                                )
                                Text(jogador.posicao, fontSize = 12.sp, color = Color.Gray)
                            }
                            
                            if (jaTemEquipe) {
                                // Tenta achar o nome da equipe atual para mostrar no aviso
                                val nomeEquipeDono = listaGlobalEquipes.find { it.id == jogador.equipeId }?.nome ?: "outro time"
                                Text("Ocupado: $nomeEquipeDono", fontSize = 10.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                if (listaExibicao.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum jogador encontrado.", color = Color.Gray)
                        }
                    }
                }
            }
            
            Button(
                onClick = onFinalizar,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("CONCLUIR")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSelecaoEquipesParaCampeonato(
    listaTotal: List<EquipeExemplo>,
    onVoltar: () -> Unit,
    onFinalizar: (List<EquipeExemplo>) -> Unit
) {
    val selecionadas = remember { mutableStateListOf<EquipeExemplo>() }
    var searchQuery by remember { mutableStateOf("") }

    val listaFiltrada = remember(searchQuery, listaTotal) {
        listaTotal.filter { it.nome.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selecionar Equipes") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar equipe...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(listaFiltrada) { equipe ->
                    val isSelected = selecionadas.any { it.id == equipe.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isSelected) {
                                    selecionadas.removeAll { it.id == equipe.id }
                                } else {
                                    selecionadas.add(equipe)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = if (equipe.escudoUri.isBlank()) R.drawable.ic_launcher_background else equipe.escudoUri,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(equipe.nome, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Checkbox(checked = isSelected, onCheckedChange = null)
                        }
                    }
                }
            }

            Button(
                onClick = { onFinalizar(selecionadas.toList()) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = selecionadas.isNotEmpty()
            ) {
                Text("CONFIRMAR (${selecionadas.size})")
            }
        }
    }
}
