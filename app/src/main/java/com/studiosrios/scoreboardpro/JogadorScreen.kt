package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TelaGerenciarJogadores(
    listaJogadores: List<JogadorExemplo>,
    equipes: List<EquipeExemplo>,
    onJogadorSalvo: (JogadorExemplo) -> Unit,
    onDeletar: (JogadorExemplo) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var posicao by remember { mutableStateOf("") }
    var equipeSelecionadaId by remember { mutableIntStateOf(-1) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("CADASTRAR JOGADOR", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Jogador") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = posicao, onValueChange = { posicao = it }, label = { Text("Posição (Ex: ATA, GOL)") }, modifier = Modifier.fillMaxWidth())

        // Simples seletor de equipe (ID)
        Text("ID da Equipe: $equipeSelecionadaId", modifier = Modifier.padding(top = 8.dp))

        Button(
            onClick = {
                if(nome.isNotEmpty()) {
                    onJogadorSalvo(JogadorExemplo(id = (listaJogadores.maxOfOrNull { it.id } ?: 0) + 1, nome = nome, posicao = posicao, altura = "", idade = "", equipeId = equipeSelecionadaId))
                    nome = ""; posicao = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) { Text("SALVAR JOGADOR") }

        Divider()

        LazyColumn(Modifier.weight(1f)) {
            items(listaJogadores) { jog ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(jog.nome, style = MaterialTheme.typography.bodyLarge)
                            Text(jog.posicao, color = Color.Gray)
                        }
                        IconButton(onClick = { onDeletar(jog) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
