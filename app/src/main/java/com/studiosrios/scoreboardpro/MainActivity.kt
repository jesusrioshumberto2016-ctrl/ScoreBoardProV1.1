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
    var idCampeonatoAtual by remember { mutableIntStateOf(-1) }
    var configuracaoFinalGrupos by remember { mutableStateOf<List<ConfigGrupo>>(emptyList()) }

    val listaPartidasCampeonato = remember { mutableStateListOf<Partida>() }
    val equipesNoCampeonato = remember { mutableStateListOf<EquipeExemplo>() }
    var configsCampeonatoAtual by remember { mutableStateOf(ConfiguracoesCampeonato()) }

    // Intercepta o botão físico de voltar para voltar exatamente "uma casa"
    BackHandler(enabled = telaAtual != "menu") {
        telaAtual = when (telaAtual) {
            "gerenciar_campeonato" -> "menu"
            "cadastrar_campeonato" -> "menu"
            "selecao_grupos" -> "cadastrar_campeonato" // Adicionado aqui!
            "selecao_equipes_campeonato" -> {
                if (modeloCampeonatoEscolhido == "Libertadores") "selecao_grupos"
                else "cadastrar_campeonato"
            }
            "painel_campeonato" -> "gerenciar_campeonato"

            // Fluxo de Jogadores
            "cadastrar_jogador" -> "menu"
            "gerenciar_jogador" -> "menu"
            "detalhes_jogador" -> "gerenciar_jogador"

            // Fluxo de Equipes
            "cadastrar_equipe" -> "menu"
            "gerenciar_equipe" -> "menu"
            "detalhes_equipe" -> "gerenciar_equipe"
            "selecionar_jogador_para_equipe" -> "detalhes_equipe"

            else -> "menu"
        }
    }

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
                idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy()
                telaAtual = "painel_campeonato"
            }
        )

        "cadastrar_campeonato" -> TelaModeloCampeonato(
            onVoltar = { telaAtual = "menu" },
            onSelecionarModelo = { modelo ->
                modeloCampeonatoEscolhido = modelo
                idCampeonatoAtual = -1
                configsCampeonatoAtual = ConfiguracoesCampeonato()

                // Verificamos se a palavra "Libertadores" existe no nome do modelo escolhido
                if (modelo.contains("Libertadores", ignoreCase = true)) {
                    telaAtual = "selecao_grupos"
                } else {
                    telaAtual = "selecao_equipes_campeonato"
                }
            }
        )
        "selecao_grupos" -> {
            TelaSelecaoGrupos(
                onVoltar = { telaAtual = "cadastrar_campeonato" },
                onConfirmar = { lista ->
                    configuracaoFinalGrupos = lista
                    telaAtual = "selecao_equipes_campeonato"
                }
            )
        }

        "selecao_equipes_campeonato" -> {
            TelaSelecaoEquipesCampeonato(
                listaEquipes = listaE,
                onVoltar = { telaAtual = "cadastrar_campeonato" },
                onFinalizar = { selecionadasIds ->
                    // 1. Mantemos a sua lógica de filtrar as equipes
                    equipesNoCampeonato.clear()
                    equipesNoCampeonato.addAll(listaE.filter { selecionadasIds.contains(it.id) })

                    // 2. Limpamos a lista de partidas
                    listaPartidasCampeonato.clear()

                    // 3. Chamamos o formato escolhido (Brasileirão, Mata-Mata, etc.)
                    val formato = obterFormato(modeloCampeonatoEscolhido)

                    // 4. O formato gera as partidas.
                    // Como o seu modelo Partida tem valores padrão (""),
                    // elas nascerão com as gavetas de árbitros e técnicos prontas, mas vazias.
                    val partidasGeradas = formato.gerarCalendario(
                        equipes = equipesNoCampeonato.toList(),
                        turnoEReturno = configsCampeonatoAtual.modoReturno,
                        configsGrupos = configuracaoFinalGrupos // <--- Aqui o dado sai da tela e vai pro motor!
                    )

                    // 5. Adicionamos as novas partidas na lista
                    listaPartidasCampeonato.addAll(partidasGeradas)

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
                listaGruposConfig = configuracaoFinalGrupos, // Envia a lista de grupos personalizada
                onSalvarGeral = { idExistente, novasConfigs ->
                    // Atualiza as configurações no estado atual
                    configsCampeonatoAtual = novasConfigs

                    // Procura o campeonato na lista global para atualizar o objeto salvo
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

// VEJA A DIFERENÇA: Tirei o ".kt" de todos os nomes abaixo
fun obterFormato(modelo: String): FormatoCampeonato {
    return when (modelo) {
        "Brasileirão Série A" -> BrasileiraoSerieA()
        "Mata-Mata" -> MataMata()
        "Libertadores" -> CopaLibertadores()
        else -> BrasileiraoSerieA()
    }
}
