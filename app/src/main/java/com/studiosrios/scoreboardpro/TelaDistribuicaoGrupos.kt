package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaDistribuicaoGrupos(
    equipesSelecionadas: List<EquipeExemplo>,
    configGrupos: List<ConfigGrupo>,
    onVoltar: () -> Unit,
    onFinalizar: (List<EquipeExemplo>) -> Unit
) {
    // Mapa para armazenar qual equipe está em qual grupo
    // No início, ninguém tem grupo
    val distribuicao = remember { mutableStateMapOf<Int, String>() }
    
    // Lista de equipes que serão reordenadas com base nos grupos
    val equipesOrdenadas = remember { mutableStateListOf<EquipeExemplo>().apply { addAll(equipesSelecionadas) } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Distribuir Equipes nos Grupos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Selecione o grupo para cada equipe selecionada.", fontSize = 14.sp, color = Color.Gray)
        
        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            items(equipesSelecionadas) { equipe ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(equipe.nome, fontWeight = FontWeight.Bold)
                            Text(equipe.city, fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        var expanded by remember { mutableStateOf(false) }
                        val grupoAtual = distribuicao[equipe.id] ?: "Selecionar"
                        
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(grupoAtual)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                configGrupos.forEach { grupo ->
                                    val timesJaNoGrupo = distribuicao.values.count { it == grupo.nome }
                                    val estaCheio = timesJaNoGrupo >= grupo.qtdTimes && grupoAtual != grupo.nome
                                    
                                    DropdownMenuItem(
                                        text = { 
                                            Text("${grupo.nome} ($timesJaNoGrupo/${grupo.qtdTimes})") 
                                        },
                                        onClick = {
                                            distribuicao[equipe.id] = grupo.nome
                                            expanded = false
                                        },
                                        enabled = !estaCheio
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val todasDistribuidas = distribuicao.size == equipesSelecionadas.size

        Button(
            onClick = {
                // Reordenar a lista de equipes para que sigam a ordem dos grupos
                val novaLista = mutableListOf<EquipeExemplo>()
                configGrupos.forEach { grupo ->
                    val equipesDoGrupo = equipesSelecionadas.filter { distribuicao[it.id] == grupo.nome }
                    novaLista.addAll(equipesDoGrupo)
                }
                // Adiciona quem sobrou (caso a validação mude)
                val restantes = equipesSelecionadas.filter { !distribuicao.containsKey(it.id) }
                novaLista.addAll(restantes)
                
                onFinalizar(novaLista)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = todasDistribuidas
        ) {
            Text("FINALIZAR E GERAR TABELAS")
        }
        
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("VOLTAR")
        }
    }
}
