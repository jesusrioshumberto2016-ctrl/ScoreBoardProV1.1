package com.studiosrios.scoreboardpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier


class MainActivity : ComponentActivity() {
    private val listaGlobalJogadores = mutableStateListOf<JogadorExemplo>()
    private val listaGlobalEquipes = mutableStateListOf<EquipeExemplo>()
    private val listaGlobalCampeonatos = mutableStateListOf<CampeonatoSalvo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- PRÉ-CADASTRO DE TESTE (32 EQUIPES X 16 JOGADORES) ---
        if (listaGlobalEquipes.isEmpty()) {
            val cidades = listOf("Rio de Janeiro", "São Paulo", "Belo Horizonte", "Porto Alegre", "Curitiba", "Salvador", "Fortaleza", "Brasília")
            val posicoes = listOf("GOL", "ZAG", "ZAG", "LAT", "LAT", "VOL", "VOL", "MEI", "MEI", "MAT", "PT", "PT", "CA", "CA", "ALA", "ALA")
            
            var jogadorIdContador = 1
            for (i in 1..32) {
                val equipeId = i
                val nomeEquipe = "Equipe de Teste $i"
                val sigla = "EQP$i"
                val cidade = cidades[(i - 1) % cidades.size]
                
                val jogadoresDestaEquipe = mutableListOf<JogadorExemplo>()
                for (j in 1..16) {
                    val jog = JogadorExemplo(
                        id = jogadorIdContador++,
                        nome = "Jogador $j - E$i",
                        posicao = posicoes[j - 1],
                        altura = "1.${75 + (j % 15)}",
                        idade = "${18 + (j % 15)} anos",
                        equipeId = equipeId
                    )
                    jogadoresDestaEquipe.add(jog)
                    listaGlobalJogadores.add(jog)
                }
                
                listaGlobalEquipes.add(EquipeExemplo(
                    id = equipeId,
                    identificacao = sigla,
                    nome = nomeEquipe,
                    city = cidade,
                    jogadores = jogadoresDestaEquipe
                ))
            }
        }

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
    var idCampeonatoAtual by remember { mutableIntStateOf(-1) }
    var configuracaoFinalGrupos by remember { mutableStateOf<List<ConfigGrupo>>(emptyList()) }
    var confrontosDefinidos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val listaPartidasCampeonato = remember { mutableStateListOf<Partida>() }
    val equipesNoCampeonato = remember { mutableStateListOf<EquipeExemplo>() }
    var configsCampeonatoAtual by remember { mutableStateOf(ConfiguracoesCampeonato()) }

    BackHandler(enabled = telaAtual != "menu") {
        telaAtual = when (telaAtual) {
            "gerenciar_campeonato" -> "menu"
            "cadastrar_campeonato" -> "menu"
            "selecao_grupos" -> "cadastrar_campeonato"
            "config_chaveamento" -> "selecao_grupos"
            "selecao_equipes_campeonato" -> {
                if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento"
                else "cadastrar_campeonato"
            }
            "distribuicao_grupos" -> "selecao_equipes_campeonato"
            "painel_campeonato" -> "gerenciar_campeonato"
            "cadastrar_jogador" -> "menu"
            "gerenciar_jogador" -> "menu"
            "detalhes_jogador" -> "gerenciar_jogador"
            "cadastrar_equipe" -> "menu"
            "gerenciar_equipe" -> "menu"
            "detalhes_equipe" -> "gerenciar_equipe"
            "selecionar_jogador_para_equipe" -> "detalhes_equipe"
            else -> "menu"
        }
    }

    when (telaAtual) {
        "menu" -> TelaInicialMenu(onNavegar = { destino: String -> telaAtual = destino })

        "gerenciar_campeonato" -> TelaListaCampeonatos(
            lista = listaC,
            onVoltar = { telaAtual = "menu" },
            onAbrir = { camp: CampeonatoSalvo ->
                equipesNoCampeonato.clear()
                equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear()
                listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo
                idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy()
                configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            }
        )

        "cadastrar_campeonato" -> TelaModeloCampeonato(
            onVoltar = { telaAtual = "menu" },
            onSelecionarModelo = { modelo: String ->
                modeloCampeonatoEscolhido = modelo
                idCampeonatoAtual = -1
                configsCampeonatoAtual = ConfiguracoesCampeonato()
                confrontosDefinidos = emptyList()
                configuracaoFinalGrupos = emptyList()

                telaAtual = if (modelo.contains("Libertadores", ignoreCase = true)) {
                    "selecao_grupos"
                } else {
                    "selecao_equipes_campeonato"
                }
            }
        )
        "selecao_grupos" -> {
            TelaSelecaoGrupos(
                onVoltar = { telaAtual = "cadastrar_campeonato" },
                onConfirmar = { lista: List<ConfigGrupo>, idaEVolta: Boolean ->
                    configuracaoFinalGrupos = lista
                    configsCampeonatoAtual = configsCampeonatoAtual.copy(modoReturno = idaEVolta)
                    telaAtual = "config_chaveamento"
                }
            )
        }
        "config_chaveamento" -> {
            TelaConfiguracaoChaveamento(
                listaGrupos = configuracaoFinalGrupos,
                onVoltar = { telaAtual = "selecao_grupos" },
                onConfirmar = { idaEVolta: Boolean, confrontos: List<Pair<String, String>> ->
                    confrontosDefinidos = confrontos
                    configsCampeonatoAtual = configsCampeonatoAtual.copy(modoIdaEVoltaMataMata = idaEVolta)
                    telaAtual = "selecao_equipes_campeonato"
                }
            )
        }

        "selecao_equipes_campeonato" -> {
            TelaSelecaoEquipesCampeonato(
                listaEquipes = listaE,
                onVoltar = { 
                    telaAtual = if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento"
                    else "cadastrar_campeonato"
                },
                onFinalizar = { selecionadasIds: List<Int> ->
                    equipesNoCampeonato.clear()
                    equipesNoCampeonato.addAll(listaE.filter { selecionadasIds.contains(it.id) })

                    if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) {
                        telaAtual = "distribuicao_grupos"
                    } else {
                        listaPartidasCampeonato.clear()
                        val formato = obterFormato(modeloCampeonatoEscolhido)
                        val partidasGeradas = formato.gerarCalendario(
                            equipes = equipesNoCampeonato.toList(),
                            turnoEReturno = configsCampeonatoAtual.modoReturno,
                            configsGrupos = configuracaoFinalGrupos,
                            confrontosMataMata = confrontosDefinidos,
                            idaEVoltaMataMata = configsCampeonatoAtual.modoIdaEVoltaMataMata
                        )
                        listaPartidasCampeonato.addAll(partidasGeradas)
                        
                        if (idCampeonatoAtual == -1) {
                            val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                            idCampeonatoAtual = novoId
                            listaC.add(CampeonatoSalvo(
                                id = novoId,
                                nomeExibicao = "Campeonato $novoId",
                                modelo = modeloCampeonatoEscolhido,
                                equipes = equipesNoCampeonato.toList(),
                                partidas = listaPartidasCampeonato.toList(),
                                configs = configsCampeonatoAtual,
                                gruposConfig = configuracaoFinalGrupos
                            ))
                        }
                        
                        telaAtual = "painel_campeonato"
                    }
                }
            )
        }
        "distribuicao_grupos" -> {
            TelaDistribuicaoGrupos(
                equipesSelecionadas = equipesNoCampeonato.toList(),
                configGrupos = configuracaoFinalGrupos,
                onVoltar = { telaAtual = "selecao_equipes_campeonato" },
                onFinalizar = { equipesDistribuidas: List<EquipeExemplo> ->
                    equipesNoCampeonato.clear()
                    equipesNoCampeonato.addAll(equipesDistribuidas)
                    
                    listaPartidasCampeonato.clear()
                    val formato = obterFormato(modeloCampeonatoEscolhido)
                    val partidasGeradas = formato.gerarCalendario(
                        equipes = equipesNoCampeonato.toList(),
                        turnoEReturno = configsCampeonatoAtual.modoReturno,
                        configsGrupos = configuracaoFinalGrupos,
                        confrontosMataMata = confrontosDefinidos,
                        idaEVoltaMataMata = configsCampeonatoAtual.modoIdaEVoltaMataMata
                    )
                    listaPartidasCampeonato.addAll(partidasGeradas)

                    if (idCampeonatoAtual == -1) {
                        val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                        idCampeonatoAtual = novoId
                        listaC.add(CampeonatoSalvo(
                            id = novoId,
                            nomeExibicao = "Campeonato $novoId",
                            modelo = modeloCampeonatoEscolhido,
                            equipes = equipesNoCampeonato.toList(),
                            partidas = listaPartidasCampeonato.toList(),
                            configs = configsCampeonatoAtual,
                            gruposConfig = configuracaoFinalGrupos
                        ))
                    }

                    telaAtual = "painel_campeonato"
                }
            )
        }
        "painel_campeonato" -> {
            TelaPainelCampeonato(
                idCamp = idCampeonatoAtual,
                equipes = equipesNoCampeonato,
                partidas = listaPartidasCampeonato,
                modelo = modeloCampeonatoEscolhido,
                listaGlobalJogadores = listaJ,
                configsIniciais = configsCampeonatoAtual,
                listaGruposConfig = configuracaoFinalGrupos,
                onSalvarGeral = { idExistente: Int, novasConfigs: ConfiguracoesCampeonato ->
                    configsCampeonatoAtual = novasConfigs

                    val index = listaC.indexOfFirst { it.id == idExistente }
                    if (index != -1) {
                        listaC[index] = listaC[index].copy(
                            configs = novasConfigs,
                            equipes = equipesNoCampeonato.toList(),
                            partidas = listaPartidasCampeonato.toList()
                        )
                    }
                },
                onVoltar = {
                    telaAtual = "menu"
                }
            )
        }
        "cadastrar_jogador" -> TelaCadastroJogador(listaGlobalJogadores = listaJ, onVoltar = { telaAtual = "menu" })
        "gerenciar_jogador" -> TelaListaJogadores(lista = listaJ, onVoltar = { telaAtual = "menu" }, onGerenciar = { jogador: JogadorExemplo -> jogadorSelecionado = jogador; telaAtual = "detalhes_jogador" })
        "detalhes_jogador" -> {
            jogadorSelecionado?.let { jogador ->
                TelaDetalhesJogador(jogador = jogador, onSalvar = { novaPos: String ->
                    val index = listaJ.indexOfFirst { it.id == jogador.id }
                    if (index != -1) listaJ[index] = listaJ[index].copy(posicao = novaPos)
                    telaAtual = "gerenciar_jogador"
                }, onVoltar = { telaAtual = "gerenciar_jogador" })
            }
        }
        "cadastrar_equipe" -> TelaCadastroEquipe(listaGlobalEquipes = listaE, onVoltar = { telaAtual = "menu" })
        "gerenciar_equipe" -> TelaListaEquipes(lista = listaE, onVoltar = { telaAtual = "menu" }, onGerenciar = { equipe: EquipeExemplo -> equipeSelecionada = equipe; telaAtual = "detalhes_equipe" })
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

fun obterFormato(modelo: String): FormatoCampeonato {
    return when {
        modelo.contains("Série A", ignoreCase = true) -> BrasileiraoSerieA()
        modelo.contains("Mata", ignoreCase = true) -> MataMata()
        modelo.contains("Libertadores", ignoreCase = true) -> CopaLibertadores()
        else -> BrasileiraoSerieA()
    }
}
