package com.studiosrios.scoreboardpro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TelaGerenciarCampeonatos(
    listaCampeonatos: List<CampeonatoSalvo>,
    onSelecionarCampeonato: (CampeonatoSalvo) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("SEUS CAMPEONATOS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(listaCampeonatos) { camp ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onSelecionarCampeonato(camp) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(camp.nomeExibicao, fontWeight = FontWeight.Bold)
                        Text("${camp.equipes.size} Equipes | ${camp.partidas.size} Jogos", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
