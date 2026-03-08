package com.studiosrios.scoreboardpro

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroJogador(
    listaGlobalJogadores: SnapshotStateList<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf("") }
    var dataNasc by remember { mutableStateOf("Selecionar") }
    var idadeS by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val posicoes = listOf("GOL", "ZAG", "LAT", "ALA", "VOL", "MEI", "MAT", "PT", "CA")
    var posSel by remember { mutableStateOf(posicoes[0]) }
    val ctx = LocalContext.current

    val launcherFoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { fotoUri = it.toString() }
    }

    val dpd = DatePickerDialog(ctx, { _, y, m, d ->
        dataNasc = "$d/${m+1}/$y"
        val h = Calendar.getInstance()
        var c = h.get(Calendar.YEAR) - y
        if(h.get(Calendar.DAY_OF_YEAR) < Calendar.getInstance().apply{set(y,m,d)}.get(Calendar.DAY_OF_YEAR)) c--
        idadeS = "$c anos"
    }, 2000, 0, 1)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("CADASTRAR JOGADOR", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcherFoto.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Foto", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
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
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = posSel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Posição") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                posicoes.forEach { p ->
                    DropdownMenuItem(text = { Text(p) }, onClick = { posSel = p; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = altura, onValueChange = { altura = it }, label = { Text("Altura (ex: 1.80)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { dpd.show() }) { Text("Data de Nascimento: $dataNasc") }
        if (idadeS.isNotBlank()) Text("Idade: $idadeS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                if (nome.isNotBlank()) {
                    val novoId = (listaGlobalJogadores.maxOfOrNull { it.id } ?: 0) + 1
                    listaGlobalJogadores.add(JogadorExemplo(novoId, nome, posSel, altura, idadeS, -1, 0, fotoUri))
                    Toast.makeText(ctx, "Jogador salvo!", Toast.LENGTH_SHORT).show()
                    onVoltar()
                }
            }, 
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) { Text("SALVAR") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("VOLTAR / SAIR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroEquipe(
    listaGlobalEquipes: SnapshotStateList<EquipeExemplo>,
    onVoltar: () -> Unit
) {
    var iden by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cid by remember { mutableStateOf("") }
    var escudoUri by remember { mutableStateOf("") }
    var expC by remember { mutableStateOf(false) }
    
    val listaPatrocinadores = remember { mutableStateListOf(Patrocinador("", "")) }
    
    val cids = listOf("Belo Horizonte", "Brasília", "Curitiba", "Rio de Janeiro", "Salvador", "São Paulo")
    val ctx = LocalContext.current

    val launcherEscudo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { escudoUri = it.toString() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("CADASTRAR EQUIPE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcherEscudo.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (escudoUri.isBlank()) {
                    Icon(Icons.Default.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary)
                } else {
                    AsyncImage(
                        model = escudoUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(value = iden, onValueChange = { iden = it.uppercase().filter { c -> c.isLetterOrDigit() } }, label = { Text("Identificação (Sigla)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = nome, onValueChange = { input -> nome = input.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } }, label = { Text("Nome da Equipe") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expC, onExpandedChange = { expC = !expC }) {
            OutlinedTextField(
                value = cid,
                onValueChange = {},
                readOnly = true,
                label = { Text("Cidade") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expC) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expC, onDismissRequest = { expC = false }) {
                cids.forEach { c ->
                    DropdownMenuItem(text = { Text(c) }, onClick = { cid = c; expC = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("PATROCINADORES", fontWeight = FontWeight.Bold)
        
        listaPatrocinadores.forEachIndexed { index, patrocinador ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val launcherPat = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        uri?.let { listaPatrocinadores[index] = listaPatrocinadores[index].copy(fotoUri = it.toString()) }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable { launcherPat.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (patrocinador.fotoUri.isBlank()) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        } else {
                            AsyncImage(model = patrocinador.fotoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = patrocinador.nome,
                        onValueChange = { listaPatrocinadores[index] = listaPatrocinadores[index].copy(nome = it) },
                        label = { Text("Nome do Patrocinador") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    
                    IconButton(onClick = { if (listaPatrocinadores.size > 1) listaPatrocinadores.removeAt(index) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
        }
        
        Button(
            onClick = { listaPatrocinadores.add(Patrocinador("", "")) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("ADICIONAR PATROCINADOR")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (nome.isNotBlank() && iden.isNotBlank()) {
                    val novoId = (listaGlobalEquipes.maxOfOrNull { it.id } ?: 0) + 1
                    listaGlobalEquipes.add(EquipeExemplo(novoId, iden, nome, cid, emptyList(), listaPatrocinadores.toList(), escudoUri))
                    Toast.makeText(ctx, "Equipe salva!", Toast.LENGTH_SHORT).show()
                    onVoltar()
                }
            }, 
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) { Text("SALVAR EQUIPE") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("VOLTAR / SAIR") }
    }
}
