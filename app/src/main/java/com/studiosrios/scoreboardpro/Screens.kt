package com.studiosrios.scoreboardpro

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val listaDisponivel = remember(searchQuery, listaTotal, equipeAtual) {
        listaTotal.filter { jogador ->
            val jaEstaNaEquipe = equipeAtual.jogadores.any { it.id == jogador.id }
            val matchesSearch = jogador.nome.contains(searchQuery, ignoreCase = true) || 
                                jogador.apelido.contains(searchQuery, ignoreCase = true)
            !jaEstaNaEquipe && matchesSearch
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
                placeholder = { Text("Buscar jogador disponível...") },
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
                items(listaDisponivel) { jogador ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
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
                                    fontWeight = FontWeight.Bold
                                )
                                Text(jogador.posicao, fontSize = 12.sp, color = Color.Gray)
                            }
                            
                            if (jogador.equipeId != -1 && jogador.equipeId != equipeAtual.id) {
                                Text("Já tem time", fontSize = 10.sp, color = Color.Red.copy(alpha = 0.6f))
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                if (listaDisponivel.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum jogador disponível para adicionar.", color = Color.Gray)
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
