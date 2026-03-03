package com.studiosrios.scoreboardpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfigLibertadores(
    configs: ConfiguracoesCampeonato,
    onSalvar: (ConfiguracoesCampeonato) -> Unit
) {
    var modoReturno by remember { mutableStateOf(configs.modoReturno) }
    var exibirCartoes by remember { mutableStateOf(configs.exibirCartoesNaTabela) }
    val criterios = remember { mutableStateListOf<String>().apply { addAll(configs.criteriosDesempate) } }

    val opcoesCriterios = listOf("Selecionar", "Vitórias", "Saldo de Gols", "Gols Marcados", "Cartões Amarelos", "Cartões Vermelhos")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Configurações: Libertadores", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // --- OPÇÕES GERAIS ---
        Text("Geral", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Fase de Grupos: Ida e Volta", fontWeight = FontWeight.Bold)
                    Text("Os jogos do grupo terão returno?", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = modoReturno, onCheckedChange = { modoReturno = it })
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Exibir Cartões na Tabela", fontWeight = FontWeight.Bold)
                    Text("Mostrar disciplina nas tabelas dos grupos", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = exibirCartoes, onCheckedChange = { exibirCartoes = it })
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- CRITÉRIOS DE DESEMPATE ---
        Text("Critérios de Desempate (Ordem de Prioridade)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Arraste ou selecione a ordem de preferência", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        
        criterios.forEachIndexed { index, atual ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${index + 1}º", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.weight(1f)) {
                    SelectorCriterio(
                        selecionado = atual,
                        opcoes = opcoesCriterios,
                        onSelecionar = { novaOpcao ->
                            criterios[index] = novaOpcao
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                onSalvar(configs.copy(
                    modoReturno = modoReturno, 
                    exibirCartoesNaTabela = exibirCartoes,
                    criteriosDesempate = criterios.toList()
                )) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SelectorCriterio(selecionado: String, opcoes: List<String>, onSelecionar: (String) -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (selecionado == "Selecionar") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(selecionado)
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}
