package com.studiosrios.scoreboardpro

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesEquipe(equipe: EquipeExemplo, listaJogadores: SnapshotStateList<JogadorExemplo>, onAdicionar: () -> Unit, onVoltar: () -> Unit) {
    val elenco = listaJogadores.filter { it.equipeId == equipe.id }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var jogadorParaRemover by remember { mutableStateOf<JogadorExemplo?>(null) }

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
        Text("EQUIPE: ${equipe.nome}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAdicionar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("ADICIONAR JOGADOR")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("ELENCO ATUAL (${elenco.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(elenco) { j ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesJogador(jogador: JogadorExemplo, onSalvar: (String) -> Unit, onVoltar: () -> Unit) {
    var novaPosicao by remember { mutableStateOf(jogador.posicao) }
    var expanded by remember { mutableStateOf(false) }
    val posicoes = listOf("GOL", "ZAG", "LAT", "ALA", "VOL", "MEI", "MAT", "PT", "CA")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("DETALHES DO JOGADOR", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { onSalvar(novaPosicao) }, modifier = Modifier.fillMaxWidth().height(55.dp)) {
            Text("SALVAR ALTERAÇÕES")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("VOLTAR")
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
            color = if (selecionadas.size in 3..20) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Equipes selecionadas: ${selecionadas.size} (Mínimo 3, Máximo 20)",
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Bold,
                color = if (selecionadas.size in 3..20) Color(0xFF2E7D32) else Color(0xFFC62828)
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
                            if (selecionadas.size < 20) selecionadas.add(equipe.id)
                            else Toast.makeText(contexto, "Limite de 20 equipes atingido!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
            enabled = selecionadas.size in 3..20
        ) {
            Text("INICIAR CAMPEONATO")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("VOLTAR")
        }
    }
}

@Composable
fun TelaModeloCampeonato(onVoltar: () -> Unit, onSelecionarModelo: (String) -> Unit) {
    var modeloSelecionado by remember { mutableStateOf("") }
    val modelos = listOf("Brasileirão Série A", "Mata Mata", "Copa Libertadores")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("NOVO CAMPEONATO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Escolha o formato da competição", color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        modelos.forEach { modelo ->
            Button(
                onClick = { modeloSelecionado = modelo },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(60.dp),
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
            onClick = { onSelecionarModelo(modeloSelecionado) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            enabled = modeloSelecionado.isNotEmpty()
        ) {
            Text("SELECIONAR MODELO")
        }
        TextButton(onClick = onVoltar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("VOLTAR")
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
                        Column(Modifier.weight(1f)) {
                            Text(j.nome, fontWeight = FontWeight.Bold, color = if (ja) Color.Gray else Color.Black)
                            Text(if (ja) "Já possui equipe" else "Disponível", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val idx = listaTotal.indexOfFirst { it.id == j.id }
                                if (idx != -1) listaTotal[idx] = listaTotal[idx].copy(equipeId = equipeAlvo.id)
                            },
                            enabled = !ja
                        ) {
                            Text("ADICIONAR", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Button(onClick = onFinalizar, modifier = Modifier.fillMaxWidth()) { Text("CONCLUÍDO") }
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
                        Column(Modifier.weight(1f)) {
                            Text(e.nome, fontWeight = FontWeight.Bold)
                            Text("ID: ${e.identificacao}", color = Color.Gray)
                        }
                        Button(onClick = { onGerenciar(e) }) { Text("GERENCIAR") }
                    }
                }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
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
                        Column(Modifier.weight(1f)) {
                            Text(j.nome, fontWeight = FontWeight.Bold)
                            Text(j.posicao, color = MaterialTheme.colorScheme.primary)
                        }
                        Button(onClick = { onGerenciar(j) }) { Text("GERENCIAR") }
                    }
                }
            }
        }
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
    }
}
