package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TelaGerenciarEquipes(
    listaEquipes: List<EquipeExemplo>,
    onEquipeSalva: (EquipeExemplo) -> Unit
) {
    var nomeEquipe by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("CADASTRAR EQUIPE", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = nomeEquipe, onValueChange = { nomeEquipe = it }, label = { Text("Nome do Time") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cidade, onValueChange = { cidade = it }, label = { Text("Cidade") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if(nomeEquipe.isNotEmpty()) {
                    onEquipeSalva(EquipeExemplo(id = (listaEquipes.maxOfOrNull { it.id } ?: 0) + 1, identificacao = "", nome = nomeEquipe, city = cidade))
                    nomeEquipe = ""; cidade = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) { Text("CADASTRAR TIME") }

        LazyColumn {
            items(listaEquipes) { equipe ->
                ListItem(
                    headlineContent = { Text(equipe.nome) },
                    supportingContent = { Text(equipe.city) }
                )
            }
        }
    }
}
