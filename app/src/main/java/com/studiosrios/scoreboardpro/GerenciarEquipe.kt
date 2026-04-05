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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.studiosrios.scoreboardpro.data.repository.DataRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaEquipesGerenciar(
    listaE: List<EquipeExemplo>,
    onVoltar: () -> Unit,
    onGerenciar: (EquipeExemplo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val listaFiltrada = remember(searchQuery, listaE) {
        if (searchQuery.isBlank()) {
            listaE
        } else {
            listaE.filter { it.nome.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Equipes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                placeholder = { Text("Pesquisar equipe...") },
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
                items(listaFiltrada) { equipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onGerenciar(equipe) },
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = equipe.escudoUri.ifBlank { R.drawable.ic_launcher_background },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(equipe.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(equipe.city, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                
                if (listaFiltrada.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhuma equipe encontrada.", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciarEquipeAdmin(
    equipe: EquipeExemplo,
    listaGlobalEquipes: SnapshotStateList<EquipeExemplo>,
    listaGlobalJogadores: SnapshotStateList<JogadorExemplo>,
    repository: DataRepository,
    onAdicionarJogador: () -> Unit,
    onVoltar: () -> Unit
) {
    val equipeAtual = listaGlobalEquipes.find { it.id == equipe.id } ?: equipe
    val ctx = LocalContext.current
    
    // Elenco derivado da lista global para garantir que os dados apareçam
    val elenco = remember(equipeAtual, listaGlobalJogadores.size) {
        listaGlobalJogadores.filter { it.equipeId == equipeAtual.id }
    }

    var escudoUri by remember { mutableStateOf(equipeAtual.escudoUri) }
    var showDialogPatrocinador by remember { mutableStateOf(false) }
    var nomePatrocinador by remember { mutableStateOf("") }
    var fotoPatrocinadorUri by remember { mutableStateOf("") }

    var showConfirmDeleteEquipe by remember { mutableStateOf(false) }
    var patrocinadorParaExcluir by remember { mutableStateOf<Patrocinador?>(null) }
    
    var jogadorParaRemover by remember { mutableStateOf<JogadorExemplo?>(null) }

    val launcherEscudo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val novaUri = it.toString()
            escudoUri = novaUri
            val index = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
            if (index != -1) {
                val updated = listaGlobalEquipes[index].copy(escudoUri = novaUri)
                listaGlobalEquipes[index] = updated
                repository.salvarEquipe(updated)
            }
        }
    }

    val launcherPatrocinador = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoPatrocinadorUri = it.toString() }
    }

    if (showConfirmDeleteEquipe) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteEquipe = false },
            title = { Text("Excluir Equipe") },
            text = { Text("Tem certeza que deseja excluir permanentemente '${equipeAtual.nome}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
                        if (index != -1) {
                            listaGlobalEquipes.removeAt(index)
                            repository.deletarEquipe(equipeAtual)
                            onVoltar()
                        }
                        showConfirmDeleteEquipe = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("EXCLUIR") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteEquipe = false }) { Text("CANCELAR") }
            }
        )
    }

    jogadorParaRemover?.let { jogador ->
        AlertDialog(
            onDismissRequest = { jogadorParaRemover = null },
            title = { Text("Remover do Elenco") },
            text = { Text("Tem certeza que deseja remover '${jogador.apelido.ifBlank { jogador.nome }}' do elenco?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val jogIdx = listaGlobalJogadores.indexOfFirst { it.id == jogador.id }
                        if (jogIdx != -1) {
                            val updatedJog = listaGlobalJogadores[jogIdx].copy(equipeId = -1)
                            listaGlobalJogadores[jogIdx] = updatedJog
                            repository.salvarJogador(updatedJog)
                            
                            // Atualiza também o objeto Equipe na memória (embora agora usemos o filtro global)
                            val eqIdx = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
                            if (eqIdx != -1) {
                                val novoElencoInterno = equipeAtual.jogadores.filter { it.id != jogador.id }
                                val updatedEq = listaGlobalEquipes[eqIdx].copy(jogadores = novoElencoInterno)
                                listaGlobalEquipes[eqIdx] = updatedEq
                                repository.salvarEquipe(updatedEq)
                            }
                            
                            Toast.makeText(ctx, "Jogador removido com sucesso.", Toast.LENGTH_SHORT).show()
                        }
                        jogadorParaRemover = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("REMOVER") }
            },
            dismissButton = {
                TextButton(onClick = { jogadorParaRemover = null }) { Text("CANCELAR") }
            }
        )
    }

    patrocinadorParaExcluir?.let { pat ->
        AlertDialog(
            onDismissRequest = { patrocinadorParaExcluir = null },
            title = { Text("Excluir Patrocinador") },
            text = { Text("Tem certeza que deseja excluir permanentemente o patrocinador '${pat.nome}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
                        if (index != -1) {
                            val novosPatrocinadores = equipeAtual.patrocinadores.filter { it != pat }
                            val updated = listaGlobalEquipes[index].copy(patrocinadores = novosPatrocinadores)
                            listaGlobalEquipes[index] = updated
                            repository.salvarEquipe(updated)
                        }
                        patrocinadorParaExcluir = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("EXCLUIR") }
            },
            dismissButton = {
                TextButton(onClick = { patrocinadorParaExcluir = null }) { Text("CANCELAR") }
            }
        )
    }

    if (showDialogPatrocinador) {
        AlertDialog(
            onDismissRequest = { showDialogPatrocinador = false },
            title = { Text("Novo Patrocinador") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nomePatrocinador,
                        onValueChange = { nomePatrocinador = it },
                        label = { Text("Nome do Patrocinador") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { launcherPatrocinador.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (fotoPatrocinadorUri.isEmpty()) "Selecionar Logo" else "Logo Selecionado")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nomePatrocinador.isNotBlank()) {
                        val index = listaGlobalEquipes.indexOfFirst { it.id == equipeAtual.id }
                        if (index != -1) {
                            val novosPatrocinadores = equipeAtual.patrocinadores + Patrocinador(nomePatrocinador, fotoPatrocinadorUri)
                            val updated = listaGlobalEquipes[index].copy(patrocinadores = novosPatrocinadores)
                            listaGlobalEquipes[index] = updated
                            repository.salvarEquipe(updated)
                        }
                        nomePatrocinador = ""
                        fotoPatrocinadorUri = ""
                        showDialogPatrocinador = false
                    }
                }) { Text("Adicionar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialogPatrocinador = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dados da Equipe") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (listaGlobalJogadores.any { it.equipeId == -1 }) {
                FloatingActionButton(onClick = onAdicionarJogador, containerColor = Color(0xFF2E7D32), contentColor = Color.White) {
                    Icon(Icons.Default.Add, "Adicionar Jogador")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = escudoUri.ifBlank { R.drawable.ic_launcher_background },
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { launcherEscudo.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
                SmallFloatingActionButton(
                    onClick = { launcherEscudo.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape) {
                    Icon(Icons.Default.Edit, contentDescription = "Alterar Foto", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("INFORMAÇÕES DE CADASTRO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Nome: ${equipeAtual.nome}", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("Sigla: ${equipeAtual.identificacao}", fontSize = 14.sp)
                    Text("Cidade: ${equipeAtual.city}", fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "PATROCINADORES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showDialogPatrocinador = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Patrocinador", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                if (equipeAtual.patrocinadores.isEmpty()) {
                    Text("Nenhum patrocinador cadastrado.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(equipeAtual.patrocinadores) { pat ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    AsyncImage(
                                        model = pat.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                    IconButton(
                                        onClick = { patrocinadorParaExcluir = pat },
                                        modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-8).dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    pat.nome, 
                                    fontSize = 10.sp, 
                                    color = Color.Gray, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("ELENCO ATUAL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                Text("${elenco.size} Jogadores", fontSize = 12.sp, color = Color.Gray)
            }
            
            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            if (elenco.isEmpty()) {
                Text("Nenhum jogador cadastrado neste time.", modifier = Modifier.padding(24.dp), color = Color.Gray, fontSize = 14.sp)
            }

            elenco.forEach { jogador ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = jogador.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(45.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(jogador.apelido.ifBlank { jogador.nome }, fontWeight = FontWeight.Bold)
                            Text("${jogador.posicao} | ID: ${jogador.id}", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = {
                            jogadorParaRemover = jogador
                        }) {
                            Icon(Icons.Default.Delete, "Remover", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { showConfirmDeleteEquipe = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(8.dp))
                Text("EXCLUIR EQUIPE")
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
