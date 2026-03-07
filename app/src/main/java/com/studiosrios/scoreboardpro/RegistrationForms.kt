package com.studiosrios.scoreboardpro

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroJogador(
    listaGlobalJogadores: SnapshotStateList<JogadorExemplo>,
    onVoltar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var dataNasc by remember { mutableStateOf("Selecionar") }
    var idadeS by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val posicoes = listOf("GOL", "ZAG", "LAT", "ALA", "VOL", "MEI", "MAT", "PT", "CA")
    var posSel by remember { mutableStateOf(posicoes[0]) }
    val ctx = LocalContext.current

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
                    listaGlobalJogadores.add(JogadorExemplo(novoId, nome, posSel, altura, idadeS))
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
    var expC by remember { mutableStateOf(false) }
    val pat = remember { mutableStateListOf("", "", "", "", "") }
    val cids = listOf("Belo Horizonte", "Brasília", "Curitiba", "Rio de Janeiro", "Salvador", "São Paulo")
    val ctx = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("CADASTRAR EQUIPE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
        pat.forEachIndexed { i, p ->
            OutlinedTextField(value = p, onValueChange = { pat[i] = it }, label = { Text("Patrocínio ${i+1}") }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (nome.isNotBlank() && iden.isNotBlank()) {
                    val novoId = (listaGlobalEquipes.maxOfOrNull { it.id } ?: 0) + 1
                    listaGlobalEquipes.add(EquipeExemplo(novoId, iden, nome, cid))
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
