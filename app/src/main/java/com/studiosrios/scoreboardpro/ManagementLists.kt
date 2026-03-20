package com.studiosrios.scoreboardpro

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
fun TelaListaCampeonatos(
    lista: List<CampeonatoSalvo>,
    onVoltar: () -> Unit,
    onAbrir: (CampeonatoSalvo) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Campeonatos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum campeonato ativo.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                items(lista) { camp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onAbrir(camp) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = if (camp.fotoUri.isBlank()) R.drawable.ic_launcher_background else camp.fotoUri,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(camp.nomeExibicao, fontWeight = FontWeight.Bold)
                                Text("${camp.modelo} • ${camp.equipes.size} Equipes", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelaGerenciarEquipes(
    listaE: List<EquipeExemplo>,
    onVoltar: () -> Unit,
    onGerenciar: (EquipeExemplo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVoltar) { Text("Voltar") }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gerenciar Equipes", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(listaE) { equipe ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onGerenciar(equipe) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (equipe.escudoUri.isBlank()) R.drawable.ic_launcher_background else equipe.escudoUri,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(equipe.nome, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun TelaGerenciarJogadoresList(
    listaJ: List<JogadorExemplo>,
    onVoltar: () -> Unit,
    onGerenciar: (JogadorExemplo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVoltar) { Text("Voltar") }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gerenciar Jogadores", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(listaJ) { jogador ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onGerenciar(jogador) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (jogador.fotoUri.isBlank()) R.drawable.ic_launcher_background else jogador.fotoUri,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(jogador.nome, fontWeight = FontWeight.Bold)
                            Text(jogador.posicao, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// Renomeada para evitar conflito com Screens.kt
@Composable
fun TelaSelecaoEquipesLegado(
    listaTotal: List<EquipeExemplo>,
    onFinalizar: (List<EquipeExemplo>) -> Unit,
    onVoltar: () -> Unit
) {
    val selecionadas = remember { mutableStateListOf<EquipeExemplo>() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Selecionar Equipes", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("${selecionadas.size} selecionadas (mínimo 2)", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn {
                items(listaTotal) { equipe ->
                    val isSelected = selecionadas.any { it.id == equipe.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isSelected) selecionadas.removeAll { it.id == equipe.id }
                                else selecionadas.add(equipe)
                            }
                            .background(if (isSelected) Color(0xFFE8F5E9) else Color.Transparent)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = if (equipe.escudoUri.isBlank()) R.drawable.ic_launcher_background else equipe.escudoUri,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(equipe.nome, modifier = Modifier.weight(1f))
                        Checkbox(checked = isSelected, onCheckedChange = {
                            if (isSelected) selecionadas.removeAll { it.id == equipe.id }
                            else selecionadas.add(equipe)
                        })
                    }
                }
            }
        }

        Button(
            onClick = { onFinalizar(selecionadas.toList()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = selecionadas.size in 2..32,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("INICIAR CAMPEONATO")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
            Text("VOLTAR")
        }
    }
}

@Composable
fun TelaModeloCampeonato(onVoltar: () -> Unit, onSelecionar: (String, String, String) -> Unit) {
    var nomeCamp by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf("") }
    var modeloSelecionado by remember { mutableStateOf("") }
    // Apenas a Copa Libertadores estará disponível conforme solicitado
    val modelos = listOf("Copa Libertadores")
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoUri = it.toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("NOVO CAMPEONATO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Configure as informações básicas", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri.isNotEmpty()) {
                    AsyncImage(model = fotoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.AddAPhoto, "Foto", tint = Color.Gray, modifier = Modifier.size(40.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = nomeCamp,
            onValueChange = { nomeCamp = it },
            label = { Text("Nome da Competição") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Formato do Torneio", fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(8.dp))

        modelos.forEach { modelo ->
            val isSelected = modeloSelecionado == modelo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { modeloSelecionado = modelo },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2E7D32) else Color.LightGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(modelo, modifier = Modifier.weight(1f), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onSelecionar(nomeCamp, fotoUri, modeloSelecionado) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = nomeCamp.isNotBlank() && modeloSelecionado.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("CONTINUAR", fontWeight = FontWeight.Bold)
        }
        
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("CANCELAR", color = Color.Gray)
        }
    }
}
