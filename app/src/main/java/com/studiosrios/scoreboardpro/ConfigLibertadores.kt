package com.studiosrios.scoreboardpro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfigLibertadores(
    configs: ConfiguracoesCampeonato,
    bloquearCriterios: Boolean = false,
    onSalvar: (ConfiguracoesCampeonato) -> Unit,
    onVoltar: () -> Unit,
    onAlteracao: () -> Unit = {} // Adicionado callback
) {
    // Usamos remember(configs) para que, se as configs mudarem (após salvar), o estado local resete
    var exibirCartoes by remember(configs) { mutableStateOf(configs.exibirCartoesNaTabela) }
    val criterios = remember(configs) { mutableStateListOf<String>().apply { addAll(configs.criteriosDesempate) } }

    // Controle de alterações: compara o estado atual da tela com o objeto 'configs' original/salvo
    val houveMudanca by remember(exibirCartoes, criterios.toList(), configs) {
        derivedStateOf {
            exibirCartoes != configs.exibirCartoesNaTabela || 
            criterios.toList() != configs.criteriosDesempate
        }
    }
    
    var mostrarConfirmacaoSair by remember { mutableStateOf(false) }

    val tentarVoltar = {
        if (houveMudanca) {
            mostrarConfirmacaoSair = true
        } else {
            onVoltar()
        }
    }

    // Botão voltar do celular
    BackHandler {
        tentarVoltar()
    }

    if (mostrarConfirmacaoSair) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoSair = false },
            title = { Text("Alterações pendentes") },
            text = { Text("Existem alterações nas configurações que não foram salvas. Deseja sair e descartar as mudanças?") },
            confirmButton = {
                Button(onClick = { mostrarConfirmacaoSair = false; onVoltar() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("DESCARTAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoSair = false }) { Text("CONTINUAR EDITANDO") }
            }
        )
    }

    val opcoesCriterios = listOf("Selecionar", "Confronto Direto", "Vitórias", "Saldo de Gols", "Gols Marcados", "Cartões Amarelos", "Cartões Vermelhos")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Configurações: Libertadores", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // --- OPÇÕES GERAIS ---
        Text("Geral", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Exibir Cartões na Tabela", fontWeight = FontWeight.Bold)
                    Text("Mostrar disciplina nas tabelas dos grupos", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = exibirCartoes, 
                    onCheckedChange = { 
                        exibirCartoes = it 
                        onAlteracao() // Notifica alteração
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- CRITÉRIOS DE DESEMPATE ---
        Text("Critérios de Desempate (Ordem de Prioridade)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        if (bloquearCriterios) {
            Text("Bloqueado: Partidas já finalizadas", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        } else {
            Text("Arraste ou selecione a ordem de preferência", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }
        
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
                        habilitado = !bloquearCriterios,
                        onSelecionar = { novaOpcao ->
                            criterios[index] = novaOpcao
                            onAlteracao() // Notifica alteração
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                onSalvar(configs.copy(
                    exibirCartoesNaTabela = exibirCartoes,
                    criteriosDesempate = criterios.toList()
                )) 
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SALVAR CONFIGURAÇÕES", fontWeight = FontWeight.Bold)
        }
        
        TextButton(
            onClick = tentarVoltar,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text("VOLTAR")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SelectorCriterio(selecionado: String, opcoes: List<String>, habilitado: Boolean = true, onSelecionar: (String) -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { if (habilitado) expandido = true },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (selecionado == "Selecionar") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(selecionado)
        }
        if (habilitado) {
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
}
