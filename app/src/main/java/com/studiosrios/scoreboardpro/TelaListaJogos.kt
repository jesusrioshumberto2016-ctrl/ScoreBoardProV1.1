package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaListaJogos(partidas: List<Partida>, equipes: List<EquipeExemplo>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Calendário de Jogos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(partidas) { partida ->
                // Busca os nomes das equipes pelos IDs
                val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Equipe A"
                val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Equipe B"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Nome Mandante
                        Text(
                            text = mandante,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        // Placar Input Mandante
                        PlacarInput(
                            valorInicial = partida.golsMandante,
                            onValueChange = { partida.golsMandante = it }
                        )

                        Text(
                            text = " x ",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )

                        // Placar Input Visitante
                        PlacarInput(
                            valorInicial = partida.golsVisitante,
                            onValueChange = { partida.golsVisitante = it }
                        )

                        // Nome Visitante
                        Text(
                            text = visitante,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlacarInput(valorInicial: Int?, onValueChange: (Int) -> Unit) {
    // Estado local para o texto do campo
    var texto by remember { mutableStateOf(valorInicial?.toString() ?: "") }

    OutlinedTextField(
        value = texto,
        onValueChange = { novoValor ->
            // Permite apenas números e no máximo 2 dígitos
            if (novoValor.length <= 2 && novoValor.all { it.isDigit() }) {
                texto = novoValor
                novoValor.toIntOrNull()?.let { onValueChange(it) }
            } else if (novoValor.isEmpty()) {
                texto = ""
            }
        },
        modifier = Modifier.width(55.dp),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}
