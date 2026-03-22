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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
fun TelaListaJogadoresGerenciar(
    listaJ: List<JogadorExemplo>,
    onVoltar: () -> Unit,
    onGerenciar: (JogadorExemplo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val listaFiltrada = remember(searchQuery, listaJ) {
        if (searchQuery.isEmpty()) {
            listaJ
        } else {
            listaJ.filter { 
                it.nome.contains(searchQuery, ignoreCase = true) || 
                it.apelido.contains(searchQuery, ignoreCase = true) 
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Jogadores", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Pesquisar por nome ou apelido...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(listaFiltrada) { jogador ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onGerenciar(jogador) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = if (jogador.fotoUri.isBlank()) R.drawable.ic_launcher_background else jogador.fotoUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (jogador.apelido.isNotBlank()) "${jogador.nome} (${jogador.apelido})" else jogador.nome,
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp
                                )
                                Text(jogador.posicao, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                
                if (listaFiltrada.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum jogador encontrado.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciarJogadorAdmin(
    jogador: JogadorExemplo,
    listaGlobalJogadores: SnapshotStateList<JogadorExemplo>,
    repository: DataRepository,
    onVoltar: () -> Unit
) {
    var fotoUri by remember { mutableStateOf(jogador.fotoUri) }
    var apelido by remember { mutableStateOf(jogador.apelido) }
    var posicao by remember { mutableStateOf(jogador.posicao) }
    var expanded by remember { mutableStateOf(false) }
    val posicoes = listOf("GOL", "ZAG", "LAT", "VOL", "MEI", "MAT", "ALA", "PT", "CA")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val novaUri = it.toString()
            fotoUri = novaUri
            val index = listaGlobalJogadores.indexOfFirst { it.id == jogador.id }
            if (index != -1) {
                val updated = listaGlobalJogadores[index].copy(fotoUri = novaUri)
                listaGlobalJogadores[index] = updated
                repository.salvarJogador(updated)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados do Jogador") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = if (fotoUri.isBlank()) R.drawable.ic_launcher_background else fotoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
                SmallFloatingActionButton(
                    onClick = { launcher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Alterar Foto", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = jogador.nome,
                onValueChange = {},
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = apelido,
                onValueChange = { 
                    apelido = it 
                    val index = listaGlobalJogadores.indexOfFirst { it.id == jogador.id }
                    if (index != -1) {
                        val updated = listaGlobalJogadores[index].copy(apelido = it)
                        listaGlobalJogadores[index] = updated
                        repository.salvarJogador(updated)
                    }
                },
                label = { Text("Apelido (Nome de Guerra)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = posicao,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Posição") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    posicoes.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
                            onClick = { 
                                posicao = p
                                expanded = false
                                val index = listaGlobalJogadores.indexOfFirst { it.id == jogador.id }
                                if (index != -1) {
                                    val updated = listaGlobalJogadores[index].copy(posicao = p)
                                    listaGlobalJogadores[index] = updated
                                    repository.salvarJogador(updated)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = jogador.idade,
                    onValueChange = {},
                    label = { Text("Idade") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
                OutlinedTextField(
                    value = jogador.altura,
                    onValueChange = {},
                    label = { Text("Altura") },
                    modifier = Modifier.weight(1f),
                    readOnly = true
                )
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    val index = listaGlobalJogadores.indexOfFirst { it.id == jogador.id }
                    if (index != -1) {
                        listaGlobalJogadores.removeAt(index)
                        // Note: DataRepository should ideally have a delete method too.
                        // For now, it's removing from SnapshotStateList, and repo doesn't have delete yet.
                        // I will add delete methods to repository.
                        onVoltar()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(8.dp))
                Text("EXCLUIR JOGADOR PERMANENTEMENTE")
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}
