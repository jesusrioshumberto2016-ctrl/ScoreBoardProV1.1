package com.studiosrios.scoreboardpro

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

// --- MODELOS DE DADOS ---
data class JogadorExemplo(val id: Int, val nome: String, var posicao: String, val altura: String, val idade: String, var equipeId: Int = -1)
data class EquipeExemplo(val id: Int, val identificacao: String, val nome: String, val city: String)

data class EventoSumula(
    val id: String = UUID.randomUUID().toString(),
    val jogadorNome: String,
    val equipeNome: String,
    val tipo: String,
    val minuto: String = ""
)

data class Partida(
    val id: Int,
    val mandanteId: Int,
    val visitanteId: Int,
    var golsMandante: String = "0",
    var golsVisitante: String = "0",
    var finalizada: Boolean = false,
    var data: String = "00/00",
    var horario: String = "00:00",
    var local: String = "A definir",
    var melhorJogador: String = "Não definido",
    val eventos: MutableList<EventoSumula> = mutableStateListOf()
)

data class LinhaTabela(val nome: String, val pontos: Int = 0, val jogos: Int = 0, val vitorias: Int = 0, val empates: Int = 0, val derrotas: Int = 0, val gm: Int = 0, val gs: Int = 0, val sg: Int = 0)

data class CampeonatoSalvo(
    val id: Int,
    val nomeExibicao: String,
    val modelo: String,
    val equipes: List<EquipeExemplo>,
    val partidas: List<Partida>
)

class MainActivity : ComponentActivity() {
    private val listaGlobalJogadores = mutableStateListOf(
        JogadorExemplo(1, "Neymar Jr", "PT", "1.75", "32 anos"),
        JogadorExemplo(2, "Lionel Messi", "MAT", "1.70", "36 anos")
    )
    private val listaGlobalEquipes = mutableStateListOf(
        EquipeExemplo(1, "FLA2026", "Flamengo", "Rio de Janeiro"),
        EquipeExemplo(2, "SEP1914", "Palmeiras", "São Paulo"),
        EquipeExemplo(3, "COR1910", "Corinthians", "São Paulo"),
        EquipeExemplo(4, "SPA1930", "São Paulo", "São Paulo")
    )
    private val listaGlobalCampeonatos = mutableStateListOf<CampeonatoSalvo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ScoreBoardNavigation(listaGlobalJogadores, listaGlobalEquipes, listaGlobalCampeonatos)
                }
            }
        }
    }
}

@Composable
fun ScoreBoardNavigation(
    listaJ: SnapshotStateList<JogadorExemplo>,
    listaE: SnapshotStateList<EquipeExemplo>,
    listaC: SnapshotStateList<CampeonatoSalvo>
) {
    var telaAtual by remember { mutableStateOf("menu") }
    var jogadorSelecionado by remember { mutableStateOf<JogadorExemplo?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }
    var modeloCampeonatoEscolhido by remember { mutableStateOf("") }

    var modoEdicaoCampeonato by remember { mutableStateOf(false) }

    val listaPartidasCampeonato = remember { mutableStateListOf<Partida>() }
    val equipesNoCampeonato = remember { mutableStateListOf<EquipeExemplo>() }

    when (telaAtual) {
        "menu" -> TelaInicialMenu(onNavegar = { telaAtual = it })

        "gerenciar_campeonato" -> TelaListaCampeonatos(
            lista = listaC,
            onVoltar = { telaAtual = "menu" },
            onAbrir = { camp ->
                equipesNoCampeonato.clear()
                equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear()
                listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo
                modoEdicaoCampeonato = true
                telaAtual = "painel_campeonato"
            }
        )

        "cadastrar_campeonato" -> TelaModeloCampeonato(
            onVoltar = { telaAtual = "menu" },
            onSelecionarModelo = { modelo ->
                modeloCampeonatoEscolhido = modelo
                if (modelo == "Brasileirão Série A") {
                    modoEdicaoCampeonato = false
                    telaAtual = "selecao_equipes_campeonato"
                }
            }
        )
        "selecao_equipes_campeonato" -> {
            TelaSelecaoEquipesCampeonato(
                listaEquipes = listaE,
                onVoltar = { telaAtual = "cadastrar_campeonato" },
                onFinalizar = { selecionadasIds ->
                    equipesNoCampeonato.clear()
                    equipesNoCampeonato.addAll(listaE.filter { selecionadasIds.contains(it.id) })
                    listaPartidasCampeonato.clear()
                    var idP = 1
                    for (i in equipesNoCampeonato.indices) {
                        for (j in i + 1 until equipesNoCampeonato.size) {
                            listaPartidasCampeonato.add(Partida(idP++, equipesNoCampeonato[i].id, equipesNoCampeonato[j].id))
                        }
                    }
                    telaAtual = "painel_campeonato"
                }
            )
        }
        "painel_campeonato" -> {
            TelaPainelCampeonato(
                equipes = equipesNoCampeonato,
                partidas = listaPartidasCampeonato,
                modelo = modeloCampeonatoEscolhido,
                listaGlobalJogadores = listaJ,
                exibirBotaoSalvar = !modoEdicaoCampeonato,
                onSalvarGeral = {
                    val novo = CampeonatoSalvo(
                        id = (listaC.size + 1),
                        nomeExibicao = "Torneio ${listaC.size + 1}",
                        modelo = modeloCampeonatoEscolhido,
                        equipes = equipesNoCampeonato.toList(),
                        partidas = listaPartidasCampeonato.toList()
                    )
                    listaC.add(novo)
                },
                onVoltar = { telaAtual = "menu" }
            )
        }
        "cadastrar_jogador" -> TelaCadastroJogador(onVoltar = { telaAtual = "menu" })
        "gerenciar_jogador" -> TelaListaJogadores(lista = listaJ, onVoltar = { telaAtual = "menu" }, onGerenciar = { jogador -> jogadorSelecionado = jogador; telaAtual = "detalhes_jogador" })
        "detalhes_jogador" -> {
            jogadorSelecionado?.let { jogador ->
                TelaDetalhesJogador(jogador = jogador, onSalvar = { novaPos ->
                    val index = listaJ.indexOfFirst { it.id == jogador.id }
                    if (index != -1) listaJ[index] = listaJ[index].copy(posicao = novaPos)
                    telaAtual = "gerenciar_jogador"
                }, onVoltar = { telaAtual = "gerenciar_jogador" })
            }
        }
        "cadastrar_equipe" -> TelaCadastroEquipe(onVoltar = { telaAtual = "menu" })
        "gerenciar_equipe" -> TelaListaEquipes(lista = listaE, onVoltar = { telaAtual = "menu" }, onGerenciar = { equipe -> equipeSelecionada = equipe; telaAtual = "detalhes_equipe" })
        "detalhes_equipe" -> {
            equipeSelecionada?.let { equipe ->
                TelaDetalhesEquipe(equipe = equipe, listaJogadores = listaJ, onAdicionar = { telaAtual = "selecionar_jogador_para_equipe" }, onVoltar = { telaAtual = "gerenciar_equipe" })
            }
        }
        "selecionar_jogador_para_equipe" -> {
            equipeSelecionada?.let { equipe ->
                TelaSelecaoJogador(equipeAlvo = equipe, listaTotal = listaJ, onFinalizar = { telaAtual = "detalhes_equipe" })
            }
        }
    }
}

@Composable
fun TelaPainelCampeonato(
    equipes: List<EquipeExemplo>,
    partidas: SnapshotStateList<Partida>,
    modelo: String,
    listaGlobalJogadores: List<JogadorExemplo>,
    exibirBotaoSalvar: Boolean,
    onSalvarGeral: () -> Unit,
    onVoltar: () -> Unit
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("TABELA", "PARTIDAS", "RESULTADOS", "SÚMULA", "CONFIGURAR")
    val contexto = LocalContext.current

    var partidaSelecionadaParaSumula by remember { mutableStateOf<Partida?>(null) }

    fun obterPartidasOrdenadas(): List<Partida> {
        return partidas.sortedWith(compareBy(
            { it.data.split("/").reversed().joinToString("") },
            { it.horario }
        ))
    }

    if (partidaSelecionadaParaSumula != null) {
        TelaSumulaDetalhada(
            partida = partidaSelecionadaParaSumula!!,
            equipes = equipes,
            onVoltar = { partidaSelecionadaParaSumula = null }
        )
    } else {
        val classificacao = equipes.map { equipe ->
            var p = 0; var j = 0; var v = 0; var e = 0; var d = 0; var gm = 0; var gs = 0
            partidas.filter { it.finalizada && (it.mandanteId == equipe.id || it.visitanteId == equipe.id) }.forEach { part ->
                j++
                val gM = part.golsMandante.toIntOrNull() ?: 0
                val gV = part.golsVisitante.toIntOrNull() ?: 0
                if (part.mandanteId == equipe.id) {
                    gm += gM; gs += gV
                    if (gM > gV) { p += 3; v++ } else if (gM == gV) { p += 1; e++ } else d++
                } else {
                    gm += gV; gs += gM
                    if (gV > gM) { p += 3; v++ } else if (gV == gM) { p += 1; e++ } else d++
                }
            }
            LinhaTabela(equipe.nome, p, j, v, e, d, gm, gs, gm - gs)
        }.sortedByDescending { it.pontos * 1000 + it.vitorias * 100 + it.sg }

        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = abaSelecionada,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {}
            ) {
                titulos.forEachIndexed { index, titulo ->
                    Tab(
                        selected = abaSelecionada == index,
                        onClick = { abaSelecionada = index },
                        text = { Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (abaSelecionada) {
                    0 -> {
                        Column(Modifier.fillMaxSize()) {
                            Row(Modifier.fillMaxWidth().background(Color.DarkGray).padding(vertical = 8.dp, horizontal = 4.dp)) {
                                Text("EQUIPE", Modifier.weight(2.5f), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                listOf("P", "J", "V", "E", "D", "GM", "GS", "SG").forEach { Text(it, Modifier.weight(1f), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                            }
                            LazyColumn(Modifier.weight(1f)) {
                                items(classificacao) { linha ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(linha.nome, Modifier.weight(2.5f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        listOf(linha.pontos, linha.jogos, linha.vitorias, linha.empates, linha.derrotas, linha.gm, linha.gs, linha.sg).forEach { Text(it.toString(), Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center) }
                                    }
                                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                                }
                            }
                            if (exibirBotaoSalvar) {
                                Button(onClick = { onSalvarGeral(); Toast.makeText(contexto, "Campeonato Salvo!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().padding(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                                    Text("SALVAR CAMPEONATO")
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(Modifier.padding(16.dp)) {
                            items(obterPartidasOrdenadas()) { partida ->
                                val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
                                val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { partidaSelecionadaParaSumula = partida }
                                ) {
                                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${partida.data} - ${partida.horario}", fontSize = 10.sp, color = Color.Gray)
                                        Text("LOCAL: ${partida.local}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(mandante, Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                                            Text(if(partida.finalizada) " ${partida.golsMandante} x ${partida.golsVisitante} " else " VS ", Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.Black, color = if(partida.finalizada) Color.Blue else Color.Gray)
                                            Text(visitante, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                        }
                                        if (partida.finalizada) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                                Icon(Icons.Default.Star, null, tint = Color(0xFFB79400), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Melhor: ${partida.melhorJogador}", fontSize = 10.sp, color = Color(0xFFB79400), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text("(Clique para ver detalhes)", fontSize = 8.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(Modifier.padding(16.dp)) {
                            items(obterPartidasOrdenadas()) { partida ->
                                val m = equipes.find { it.id == partida.mandanteId }?.nome ?: "M"
                                val v = equipes.find { it.id == partida.visitanteId }?.nome ?: "V"

                                var mostrarDialogoLocal by remember { mutableStateOf(false) }
                                var localTemporario by remember { mutableStateOf(partida.local) }

                                if (mostrarDialogoLocal) {
                                    AlertDialog(
                                        onDismissRequest = { mostrarDialogoLocal = false },
                                        title = { Text("Definir Local") },
                                        text = { OutlinedTextField(value = localTemporario, onValueChange = { localTemporario = it }, label = { Text("Nome do Local/Estádio") }) },
                                        confirmButton = { Button(onClick = { val idx = partidas.indexOf(partida); if(idx != -1) partidas[idx] = partidas[idx].copy(local = localTemporario); mostrarDialogoLocal = false }) { Text("OK") } }
                                    )
                                }

                                Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = if(partida.finalizada) Color(0xFFE3F2FD) else Color.White)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            TextButton(onClick = { if(!partida.finalizada){ val c = Calendar.getInstance(); DatePickerDialog(contexto, { _, y, month, d -> val idx = partidas.indexOf(partida); partidas[idx] = partidas[idx].copy(data = String.format("%02d/%02d", d, month + 1)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() } }) { Text("DATA: ${partida.data}", fontSize = 10.sp) }
                                            TextButton(onClick = { if(!partida.finalizada){ TimePickerDialog(contexto, { _, h, min -> val idx = partidas.indexOf(partida); partidas[idx] = partidas[idx].copy(horario = String.format("%02d:%02d", h, min)) }, 15, 0, true).show() } }) { Text("HORA: ${partida.horario}", fontSize = 10.sp) }
                                            TextButton(onClick = { if(!partida.finalizada) mostrarDialogoLocal = true }) { Text("LOCAL: ${partida.local}", fontSize = 10.sp) }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(m, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                            OutlinedTextField(value = partida.golsMandante, onValueChange = { if(!partida.finalizada) { val idx = partidas.indexOf(partida); if(idx != -1) { partidas[idx] = partidas[idx].copy(golsMandante = it.filter { c -> c.isDigit() }) } } }, modifier = Modifier.width(50.dp), enabled = !partida.finalizada, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center))
                                            Text(" X ", fontWeight = FontWeight.Black)
                                            OutlinedTextField(value = partida.golsVisitante, onValueChange = { if(!partida.finalizada) { val idx = partidas.indexOf(partida); if(idx != -1) { partidas[idx] = partidas[idx].copy(golsVisitante = it.filter { c -> c.isDigit() }) } } }, modifier = Modifier.width(50.dp), enabled = !partida.finalizada, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center))
                                            Text(v, Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                        }
                                        Button(onClick = { val idx = partidas.indexOf(partida); if(idx != -1) { partidas[idx] = partidas[idx].copy(finalizada = !partida.finalizada) } }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = if(partida.finalizada) Color.Red else Color.DarkGray)) {
                                            Text(if(partida.finalizada) "CORRIGIR / CANCELAR" else "CONFIRMAR RESULTADO")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        SubTelaSumulaEvento(partidas, equipes, listaGlobalJogadores)
                    }
                    4 -> {
                        SubTelaConfigurarTorneio(modelo, equipes, partidas)
                    }
                }
            }
            Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("VOLTAR AO MENU") }
        }
    }
}

@Composable
fun SubTelaConfigurarTorneio(modelo: String, equipes: List<EquipeExemplo>, partidas: SnapshotStateList<Partida>) {
    val jaIniciou = partidas.any { it.finalizada }

    val totalEsperadoTurnoUnico = (equipes.size * (equipes.size - 1)) / 2
    val modoReturnoAtual = partidas.size > totalEsperadoTurnoUnico

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("CONFIGURAÇÕES DO TORNEIO", fontSize = 18.sp, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(24.dp))

        Text("SISTEMA DE DISPUTA", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        if (jaIniciou) {
            Surface(
                color = Color(0xFFFFEBEE),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    "O sistema de disputa não pode ser alterado pois já existem partidas finalizadas.",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = !modoReturnoAtual,
                onClick = {
                    if (!jaIniciou && modoReturnoAtual) {
                        val listaOriginal = partidas.take(totalEsperadoTurnoUnico)
                        partidas.clear()
                        partidas.addAll(listaOriginal)
                    }
                },
                enabled = !jaIniciou
            )
            Text("Turno Único")
            Spacer(Modifier.width(20.dp))
            RadioButton(
                selected = modoReturnoAtual,
                onClick = {
                    if (!jaIniciou && !modoReturnoAtual) {
                        val returno = mutableListOf<Partida>()
                        var novoId = partidas.maxByOrNull { it.id }?.id ?: 0
                        partidas.forEach { p ->
                            returno.add(Partida(++novoId, p.visitanteId, p.mandanteId))
                        }
                        partidas.addAll(returno)
                    }
                },
                enabled = !jaIniciou
            )
            Text("Turno e Returno")
        }

        Spacer(Modifier.height(24.dp))
        Text("INFORMAÇÕES GERAIS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        Text("Modelo: $modelo", fontWeight = FontWeight.Medium)
        Text("Total de Equipes: ${equipes.size}", fontWeight = FontWeight.Medium)
        Text("Total de Jogos: ${partidas.size}", fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(24.dp))

        Text("OPÇÕES DE EXIBIÇÃO", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mostrar Saldo de Gols na Tabela")
            Checkbox(checked = true, onCheckedChange = {})
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Destacar zona de rebaixamento")
            Checkbox(checked = false, onCheckedChange = {})
        }

        Spacer(Modifier.height(24.dp))
        Text("EQUIPES PARTICIPANTES", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
        Divider(Modifier.padding(vertical = 8.dp))

        equipes.forEach { equipe ->
            Text("• ${equipe.nome}", modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

@Composable
fun TelaSumulaDetalhada(partida: Partida, equipes: List<EquipeExemplo>, onVoltar: () -> Unit) {
    val mandante = equipes.find { it.id == partida.mandanteId }?.nome ?: "Time"
    val visitante = equipes.find { it.id == partida.visitanteId }?.nome ?: "Time"

    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Text("DETALHES DA PARTIDA", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${partida.data} às ${partida.horario}", fontWeight = FontWeight.Bold)
                Text("Local: ${partida.local}")
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mandante, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${partida.golsMandante} X ${partida.golsVisitante}", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(visitante, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                    Spacer(Modifier.width(8.dp))
                    Text("MELHOR JOGADOR: ${partida.melhorJogador}", fontWeight = FontWeight.Bold, color = Color(0xFFB79400))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("EVENTOS DA PARTIDA", fontWeight = FontWeight.Bold, color = Color.Gray)
        Divider()

        if (partida.eventos.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Nenhum evento registrado nesta súmula.", color = Color.LightGray)
            }
        } else {
            LazyColumn(Modifier.weight(1f).padding(top = 8.dp)) {
                items(partida.eventos) { ev ->
                    val corEvento = when(ev.tipo) {
                        "YELLOW CARD" -> Color(0xFFB79400)
                        "RED CARD" -> Color.Red
                        "GOL CONTRA" -> Color(0xFFD32F2F)
                        "PÊNALTI PERDIDO" -> Color(0xFF757575)
                        "PÊNALTI DEFENDIDO" -> Color(0xFF1976D2)
                        else -> Color(0xFF2E7D32)
                    }

                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = corEvento,
                            modifier = Modifier.size(12.dp),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {}
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(ev.tipo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = corEvento)
                            Text("${ev.jogadorNome} (${ev.equipeNome})", fontSize = 14.sp)
                        }
                    }
                    Divider(Modifier.padding(start = 24.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }
        }

        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Text("VOLTAR PARA PARTIDAS")
        }
    }
}

@Composable
fun SubTelaSumulaEvento(partidas: SnapshotStateList<Partida>, equipes: List<EquipeExemplo>, todosJogadores: List<JogadorExemplo>) {
    var partidaFocada by remember { mutableStateOf<Partida?>(null) }
    val ctx = LocalContext.current

    if (partidaFocada == null) {
        LazyColumn(Modifier.padding(16.dp)) {
            items(partidas) { p ->
                val mandante = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
                val visitante = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$mandante vs $visitante", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Button(onClick = { partidaFocada = p }) { Text("ABRIR SÚMULA") }
                    }
                }
            }
        }
    } else {
        val p = partidaFocada!!
        val mNome = equipes.find { it.id == p.mandanteId }?.nome ?: "M"
        val vNome = equipes.find { it.id == p.visitanteId }?.nome ?: "V"
        val jogadoresDaPartida = todosJogadores.filter { it.equipeId == p.mandanteId || it.equipeId == p.visitanteId }

        var showDialog by remember { mutableStateOf(false) }
        var tipoAtual by remember { mutableStateOf("GOL") }
        var showDialogMelhor by remember { mutableStateOf(false) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Registrar $tipoAtual") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        val listaFiltrada = if (tipoAtual == "PÊNALTI DEFENDIDO") {
                            jogadoresDaPartida.filter { it.posicao == "GOL" }
                        } else {
                            jogadoresDaPartida
                        }

                        if (listaFiltrada.isEmpty()) {
                            Text("Nenhum jogador apto para esta ação.", Modifier.padding(8.dp), color = Color.Gray)
                        } else {
                            listaFiltrada.forEach { jog ->
                                val nomeEquipeJogador = equipes.find { it.id == jog.equipeId }?.nome ?: "Sem Time"
                                TextButton(onClick = {
                                    val idx = partidas.indexOfFirst { it.id == p.id }
                                    if (idx != -1) {
                                        val partidaEmEdicao = partidas[idx]

                                        // Lógica de Gols (Atualiza Placar Automaticamente)
                                        if (tipoAtual == "GOL") {
                                            if (jog.equipeId == partidaEmEdicao.mandanteId) {
                                                val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) + 1
                                                partidaEmEdicao.golsMandante = gols.toString()
                                            } else {
                                                val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) + 1
                                                partidaEmEdicao.golsVisitante = gols.toString()
                                            }
                                        } else if (tipoAtual == "GOL CONTRA") {
                                            if (jog.equipeId == partidaEmEdicao.mandanteId) {
                                                val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) + 1
                                                partidaEmEdicao.golsVisitante = gols.toString()
                                            } else {
                                                val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) + 1
                                                partidaEmEdicao.golsMandante = gols.toString()
                                            }
                                        }

                                        // Registro do Evento
                                        if (tipoAtual == "YELLOW CARD") {
                                            val jaTemAmarelo = partidaEmEdicao.eventos.any { it.jogadorNome == jog.nome && it.tipo == "YELLOW CARD" }
                                            partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = "YELLOW CARD"))
                                            if (jaTemAmarelo) {
                                                partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = "RED CARD"))
                                                Toast.makeText(ctx, "${jog.nome} recebeu o 2º YELLOW CARD e foi expulso!", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            partidaEmEdicao.eventos.add(EventoSumula(jogadorNome = jog.nome, equipeNome = nomeEquipeJogador, tipo = tipoAtual))
                                        }

                                        // Força atualização da UI no Compose
                                        partidas[idx] = partidaEmEdicao.copy()
                                        partidaFocada = partidas[idx]
                                    }
                                    showDialog = false
                                }) {
                                    Text("${jog.nome} (${jog.posicao} - $nomeEquipeJogador)")
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showDialog = false }) { Text("FECHAR") } }
            )
        }

        if (showDialogMelhor) {
            AlertDialog(
                onDismissRequest = { showDialogMelhor = false },
                title = { Text("Eleger Melhor Jogador") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        jogadoresDaPartida.forEach { jog ->
                            TextButton(onClick = {
                                val idx = partidas.indexOfFirst { it.id == p.id }
                                if (idx != -1) {
                                    partidas[idx] = partidas[idx].copy(melhorJogador = jog.nome)
                                    partidaFocada = partidas[idx]
                                }
                                showDialogMelhor = false
                            }) {
                                Text(jog.nome)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showDialogMelhor = false }) { Text("CANCELAR") } }
            )
        }

        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("SÚMULA: $mNome x $vNome", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("Placar: ${p.golsMandante} x ${p.golsVisitante}", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            Card(Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFB79400))
                    Spacer(Modifier.width(8.dp))
                    Text("Melhor da Partida: ${p.melhorJogador}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Button(onClick = { showDialogMelhor = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB79400))) {
                        Text("DEFINIR", fontSize = 10.sp)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Button(onClick = { tipoAtual = "GOL"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("GOL", fontSize = 10.sp) }
                    Button(onClick = { tipoAtual = "GOL CONTRA"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) { Text("GOL CONTRA", fontSize = 10.sp) }
                    Button(onClick = { tipoAtual = "YELLOW CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)) { Text("YELLOW CARD", fontSize = 8.sp) }
                    Button(onClick = { tipoAtual = "RED CARD"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("RED CARD", fontSize = 8.sp) }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    Button(onClick = { tipoAtual = "COMETEU PÊNALTI"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) { Text("COMETEU PÊN.", fontSize = 8.sp) }
                    Button(onClick = { tipoAtual = "SOFREU PÊNALTI"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("SOFREU PÊN.", fontSize = 9.sp) }
                    Button(onClick = { tipoAtual = "PERDEU PÊNALTI"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("PERDEU PÊN.", fontSize = 9.sp) }
                    Button(onClick = { tipoAtual = "PÊNALTI DEFENDIDO"; showDialog = true }, modifier = Modifier.weight(1f).padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("PÊN. DEFEND.", fontSize = 9.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("HISTÓRICO DE EVENTOS:", fontWeight = FontWeight.Bold)
            Divider()

            p.eventos.forEach { ev ->
                val cor = when(ev.tipo) {
                    "YELLOW CARD" -> Color(0xFFB79400)
                    "RED CARD" -> Color.Red
                    "GOL CONTRA" -> Color(0xFFD32F2F)
                    "PERDEU PÊNALTI" -> Color.Gray
                    "PÊNALTI DEFENDIDO" -> Color(0xFF1976D2)
                    else -> Color(0xFF2E7D32)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${ev.tipo}: ${ev.jogadorNome} (${ev.equipeNome})", color = cor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val idx = partidas.indexOfFirst { it.id == p.id }
                        if (idx != -1) {
                            val partidaEmEdicao = partidas[idx]

                            // Ao excluir um GOL, subtrai do placar
                            if (ev.tipo == "GOL") {
                                val equipeDoJogador = todosJogadores.find { it.nome == ev.jogadorNome }?.equipeId
                                if (equipeDoJogador == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsMandante = if (gols < 0) "0" else gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsVisitante = if (gols < 0) "0" else gols.toString()
                                }
                            } else if (ev.tipo == "GOL CONTRA") {
                                val equipeDoJogador = todosJogadores.find { it.nome == ev.jogadorNome }?.equipeId
                                if (equipeDoJogador == partidaEmEdicao.mandanteId) {
                                    val gols = (partidaEmEdicao.golsVisitante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsVisitante = if (gols < 0) "0" else gols.toString()
                                } else {
                                    val gols = (partidaEmEdicao.golsMandante.toIntOrNull() ?: 0) - 1
                                    partidaEmEdicao.golsMandante = if (gols < 0) "0" else gols.toString()
                                }
                            }

                            partidaEmEdicao.eventos.remove(ev)
                            partidas[idx] = partidaEmEdicao.copy()
                            partidaFocada = partidas[idx]
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(onClick = { partidaFocada = null }, modifier = Modifier.fillMaxWidth()) { Text("VOLTAR") }
        }
    }
}

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

@Composable
fun TelaInicialMenu(onNavegar: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ScoreBoard Pro", fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(30.dp))

        BotaoMenu("Cadastrar campeonato") { onNavegar("cadastrar_campeonato") }
        BotaoMenu("Gerenciar campeonato") { onNavegar("gerenciar_campeonato") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar equipe") { onNavegar("cadastrar_equipe") }
        BotaoMenu("Gerenciar equipe") { onNavegar("gerenciar_equipe") }

        Spacer(modifier = Modifier.height(16.dp))

        BotaoMenu("Cadastrar jogador") { onNavegar("cadastrar_jogador") }
        BotaoMenu("Gerenciar jogador") { onNavegar("gerenciar_jogador") }
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
fun TelaCadastroJogador(onVoltar: () -> Unit) {
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
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("SALVAR") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastroEquipe(onVoltar: () -> Unit) {
    var iden by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cid by remember { mutableStateOf("") }
    var expC by remember { mutableStateOf(false) }
    val pat = remember { mutableStateListOf("", "", "", "", "") }
    val cids = listOf("Belo Horizonte", "Brasília", "Curitiba", "Rio de Janeiro", "Salvador", "São Paulo")

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
        Button(onClick = onVoltar, modifier = Modifier.fillMaxWidth().height(55.dp)) { Text("SALVAR EQUIPE") }
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

@Composable
fun BotaoMenu(texto: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(50.dp)) {
        Text(texto.uppercase(), fontWeight = FontWeight.Bold)
    }
}
