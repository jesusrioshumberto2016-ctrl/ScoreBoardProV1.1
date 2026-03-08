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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun TelaListaCampeonatos(lista: List<CampeonatoSalvo>, onVoltar: () -> Unit, onAbrir: (CampeonatoSalvo) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("CAMPEONATOS SALVOS", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if(lista.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Nenhum campeonato salvo ainda.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(lista) { camp ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = camp.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(camp.nomeExibicao, fontWeight = FontWeight.Bold)
                                Text("${camp.modelo} - ${camp.equipes.size} Equipes", fontSize = 12.sp, color = Color.Gray)
                            }
                            Button(onClick = { onAbrir(camp) }) { Text("ABRIR") }
                        }
                    }
                }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("VOLTAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesEquipe(
    equipe: EquipeExemplo, 
    listaGlobalEquipes: SnapshotStateList<EquipeExemplo>,
    listaJogadores: SnapshotStateList<JogadorExemplo>, 
    onAdicionar: () -> Unit, 
    onVoltar: () -> Unit
) {
    val elenco = listaJogadores.filter { it.equipeId == equipe.id }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var jogadorParaRemover by remember { mutableStateOf<JogadorExemplo?>(null) }
    
    // Novo estado para o escudo editável
    var novoEscudoUri by remember { mutableStateOf(equipe.escudoUri) }
    val launcherEscudo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            novoEscudoUri = it.toString()
            // Atualiza na lista global
            val idx = listaGlobalEquipes.indexOfFirst { e -> e.id == equipe.id }
            if (idx != -1) {
                listaGlobalEquipes[idx] = listaGlobalEquipes[idx].copy(escudoUri = it.toString())
            }
        }
    }

    if (mostrarDialogo && jogadorParaRemover != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Remover do Elenco?") },
            text = { Text("Deseja remover ${jogadorParaRemover!!.nome} da equipe ${equipe.nome}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = listaJogadores.indexOfFirst { it.id == jogadorParaRemover!!.id }
                        if (idx != -1) {
                            listaJogadores[idx] = listaJogadores[idx].copy(equipeId = -1)
                        }
                        mostrarDialogo = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("REMOVER")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("GERENCIAR EQUIPE", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // CARD DE DADOS CADASTRAIS (Com Escudo Editável)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = novoEscudoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable { launcherEscudo.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp).offset(x = 4.dp, y = 4.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.padding(4.dp), tint = Color.White)
                        }
                    }

                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(equipe.nome, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("Sigla: ${equipe.identificacao}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Cidade: ${equipe.city}", fontSize = 14.sp, color = Color.Gray)
                    }
                }
                
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                
                Text("PATROCINADORES:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))
                
                if (equipe.patrocinadores.isEmpty()) {
                    Text("Nenhum cadastrado", fontSize = 12.sp, color = Color.LightGray)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(equipe.patrocinadores) { pat ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = pat.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp).clip(RoundedCornerShape(8.dp)).background(Color.White).border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(pat.nome.take(10), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAdicionar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("ADICIONAR JOGADOR AO ELENCO")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("ELENCO ATUAL (${elenco.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(elenco) { j ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = j.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(j.nome, fontWeight = FontWeight.Bold)
                            Text(j.posicao, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                jogadorParaRemover = j
                                mostrarDialogo = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("REMOVER", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onVoltar, 
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) { Text("VOLTAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesJogador(jogador: JogadorExemplo, onSalvar: (String, String) -> Unit, onVoltar: () -> Unit) {
    var novaPosicao by remember { mutableStateOf(jogador.posicao) }
    var novaFotoUri by remember { mutableStateOf(jogador.fotoUri) }
    var expanded by remember { mutableStateOf(false) }
    val posicoes = listOf("GOL", "ZAG", "LAT", "ALA", "VOL", "MEI", "MAT", "PT", "CA")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { novaFotoUri = it.toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("DETALHES DO JOGADOR", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (novaFotoUri.isBlank()) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.primary)
                } else {
                    AsyncImage(
                        model = novaFotoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.BottomEnd) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = jogador.nome, onValueChange = {}, label = { Text("Nome") }, readOnly = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = jogador.altura, onValueChange = {}, label = { Text("Altura") }, readOnly = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = jogador.idade, onValueChange = {}, label = { Text("Idade") }, readOnly = true, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Text("ALTERAR POSIÇÃO:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = novaPosicao,
                onValueChange = {},
                readOnly = true,
                label = { Text("Posição Atual") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                posicoes.forEach { pos ->
                    DropdownMenuItem(text = { Text(pos) }, onClick = { novaPosicao = pos; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onSalvar(novaPosicao, novaFotoUri) }, 
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("SALVAR ALTERAÇÕES")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
            Text("VOLTAR / CANCELAR")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSelecaoEquipesCampeonato(listaEquipes: List<EquipeExemplo>, onVoltar: () -> Unit, onFinalizar: (List<Int>) -> Unit) {
    val selecionadas = remember { mutableStateListOf<Int>() }
    val contexto = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("SELECIONE AS EQUIPES", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = if (selecionadas.size in 2..32) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Equipes selecionadas: ${selecionadas.size} (Mínimo 2, Máximo 32)",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = if (selecionadas.size in 2..32) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listaEquipes) { equipe ->
                val isSelected = selecionadas.contains(equipe.id)
                Card(
                    onClick = {
                        if (isSelected) selecionadas.remove(equipe.id)
                        else {
                            if (selecionadas.size < 32) selecionadas.add(equipe.id)
                            else Toast.makeText(contexto, "Limite de 32 equipes atingido!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = equipe.escudoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(equipe.nome, fontWeight = FontWeight.Bold)
                            Text(equipe.city, fontSize = 12.sp)
                        }
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Button(
            onClick = { onFinalizar(selecionadas.toList()) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
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
    val modelos = listOf("Brasileirão Série A", "Mata Mata", "Copa Libertadores")
    
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Adicionar Foto", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    AsyncImage(
                        model = fotoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nomeCamp,
            onValueChange = { nomeCamp = it },
            label = { Text("Nome do Campeonato") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Escolha o formato da competição", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        
        modelos.forEach { modelo ->
            Button(
                onClick = { modeloSelecionado = modelo },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (modeloSelecionado == modelo) MaterialTheme.colorScheme.primary else Color.LightGray.copy(0.3f),
                    contentColor = if (modeloSelecionado == modelo) Color.White else Color.Black
                )
            ) {
                Text(modelo.uppercase(), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSelecionar(modeloSelecionado, nomeCamp, fotoUri) },
            modifier = Modifier.fillMaxWidth().height(55.dp).padding(top = 16.dp),
            enabled = modeloSelecionado.isNotEmpty() && nomeCamp.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("PRÓXIMO PASSO")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
            Text("CANCELAR")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaSelecaoJogador(equipeAlvo: EquipeExemplo, listaTotal: SnapshotStateList<JogadorExemplo>, onFinalizar: () -> Unit) {
    var p by remember { mutableStateOf("") }
    val f = listaTotal.filter { it.nome.contains(p, true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = p,
            onValueChange = { p = it },
            label = { Text("Buscar jogador") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(f) { j ->
                val ja = j.equipeId != -1
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (ja) Color.LightGray.copy(0.4f) else Color.White)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = j.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(j.nome, fontWeight = FontWeight.Bold, color = if (ja) Color.Gray else Color.Black)
                            Text(if (ja) "Já possui equipe" else "Disponível", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val idx = listaTotal.indexOfFirst { it.id == j.id }
                                if (idx != -1) listaTotal[idx] = listaTotal[idx].copy(equipeId = equipeAlvo.id)
                            },
                            enabled = !ja,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("ADICIONAR", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Button(onClick = onFinalizar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("CONCLUÍDO") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaEquipes(lista: List<EquipeExemplo>, onVoltar: () -> Unit, onGerenciar: (EquipeExemplo) -> Unit) {
    var pesq by remember { mutableStateOf("") }
    val filt = lista.filter { it.nome.contains(pesq, true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("GERENCIAR EQUIPES", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = pesq, onValueChange = { pesq = it }, label = { Text("Procurar equipe") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) })

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filt) { e ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = e.escudoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.White).border(1.dp, Color.Gray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.nome, fontWeight = FontWeight.Bold)
                            Text("ID: ${e.identificacao}", color = Color.Gray)
                        }
                        Button(onClick = { onGerenciar(e) }) { Text("GERENCIAR") }
                    }
                }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("VOLTAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaJogadores(lista: List<JogadorExemplo>, onVoltar: () -> Unit, onGerenciar: (JogadorExemplo) -> Unit) {
    var p by remember { mutableStateOf("") }
    val f = lista.filter { it.nome.contains(p, true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("GERENCIAR JOGADORES", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Pesquisar jogador") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) })

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(f) { j ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = j.fotoUri.ifBlank { R.drawable.ic_launcher_background },
                            contentDescription = null,
                            modifier = Modifier.size(35.dp).clip(CircleShape).background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(j.nome, fontWeight = FontWeight.Bold)
                            Text(j.posicao, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(onClick = { onGerenciar(j) }) { Text("GERENCIAR") }
                    }
                }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("VOLTAR") }
    }
}
