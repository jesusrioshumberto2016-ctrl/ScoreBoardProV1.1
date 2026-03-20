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
        
        if (listaGlobalEquipes.isEmpty()) {
            val cidades = listOf("Rio de Janeiro", "São Paulo", "Belo Horizonte", "Porto Alegre", "Curitiba", "Salvador", "Fortaleza", "Brasília")
            val posicoes = listOf("GOL", "ZAG", "ZAG", "LAT", "LAT", "VOL", "VOL", "MEI", "MEI", "MAT", "PT", "PT", "CA", "CA", "ALA", "ALA")
            
            var jogadorIdContador = 1
            for (i in 1..32) {
                val sigla = "EQP$i"
                val cidade = cidades[(i - 1) % cidades.size]
                
                // Placeholder para escudo da equipe
                val escudoUrl = "https://ui-avatars.com/api/?name=$sigla&background=0D47A1&color=fff&size=256"
                
                val jogadoresDestaEquipe = mutableListOf<JogadorExemplo>()
                for (j in 1..16) {
                    val apelido = "J$j-$sigla"
                    // Placeholder para foto do jogador
                    val fotoUrl = "https://ui-avatars.com/api/?name=${apelido.replace("-", "+")}&background=random&color=fff&size=256"
                    
                    val jog = JogadorExemplo(
                        id = jogadorIdContador++,
                        nome = "Jogador $j - E$i",
                        posicao = posicoes[j - 1],
                        altura = "1.${75 + (j % 15)}",
                        idade = "${18 + (j % 15)} anos",
                        equipeId = i,
                        apelido = apelido,
                        fotoUri = fotoUrl
                    )
                    jogadoresDestaEquipe.add(jog)
                    listaGlobalJogadores.add(jog)
                }
                
                listaGlobalEquipes.add(EquipeExemplo(
                    id = i,
                    identificacao = sigla,
                    nome = "Equipe de Teste $i",
                    city = cidade,
                    jogadores = jogadoresDestaEquipe,
                    escudoUri = escudoUrl
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
    var telaAtual by remember { mutableStateOf("telespectador") }
    var isOrganizador by remember { mutableStateOf(false) }
    
    var jogadorSelecionado by remember { mutableStateOf<JogadorExemplo?>(null) }
    var equipeSelecionada by remember { mutableStateOf<EquipeExemplo?>(null) }
    var modeloCampeonatoEscolhido by remember { mutableStateOf("") }
    var nomeCampeonatoEscolhido by remember { mutableStateOf("") }
    var fotoCampeonatoEscolhida by remember { mutableStateOf("") }
    var idCampeonatoAtual by remember { mutableIntStateOf(-1) }
    var configuracaoFinalGrupos by remember { mutableStateOf<List<ConfigGrupo>>(emptyList()) }
    var confrontosDefinidos by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val listaPartidasCampeonato = remember { mutableStateListOf<Partida>() }
    val equipesNoCampeonato = remember { mutableStateListOf<EquipeExemplo>() }
    var configsCampeonatoAtual by remember { mutableStateOf(ConfiguracoesCampeonato()) }

    BackHandler(enabled = telaAtual != "telespectador") {
        telaAtual = when (telaAtual) {
            "menu" -> "telespectador"
            "gerenciar_campeonato" -> "menu"
            "cadastrar_campeonato" -> "menu"
            "selecao_grupos" -> "cadastrar_campeonato"
            "config_chaveamento" -> "selecao_grupos"
            "selecao_equipes_campeonato" -> {
                if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento"
                else "cadastrar_campeonato"
            }
            "distribuicao_grupos" -> "selecao_equipes_campeonato"
            "painel_campeonato" -> {
                if (isOrganizador) "menu" else "telespectador"
            }
            "cadastrar_jogador" -> "menu"
            "gerenciar_jogador" -> "menu"
            "detalhes_jogador" -> "gerenciar_jogador"
            "cadastrar_equipe" -> "menu"
            "gerenciar_equipe" -> "menu"
            "detalhes_equipe" -> "gerenciar_equipe"
            "selecionar_jogador_para_equipe" -> "detalhes_equipe"
            else -> "telespectador"
        }
    }

    when (telaAtual) {
        "telespectador" -> TelaInicialTelespectador(
            listaC = listaC,
            onAbrirCampeonato = { camp ->
                isOrganizador = false
                equipesNoCampeonato.clear()
                equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear()
                listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo
                nomeCampeonatoEscolhido = camp.nomeExibicao 
                fotoCampeonatoEscolhida = camp.fotoUri
                idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy()
                configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onEntrarComoOrganizador = { telaAtual = "menu" }
        )

        "menu" -> TelaInicialMenu(
            listaC = listaC,
            onAbrirCamp = { camp: CampeonatoSalvo ->
                isOrganizador = true
                equipesNoCampeonato.clear()
                equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear()
                listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo
                nomeCampeonatoEscolhido = camp.nomeExibicao
                fotoCampeonatoEscolhida = camp.fotoUri
                idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy()
                configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            },
            onNavegar = { destino: String -> 
                if (destino == "cadastrar_campeonato") isOrganizador = true
                telaAtual = destino 
            }
        )

        "gerenciar_campeonato" -> TelaListaCampeonatos(
            lista = listaC,
            onVoltar = { telaAtual = "menu" },
            onAbrir = { camp: CampeonatoSalvo ->
                isOrganizador = true
                equipesNoCampeonato.clear()
                equipesNoCampeonato.addAll(camp.equipes)
                listaPartidasCampeonato.clear()
                listaPartidasCampeonato.addAll(camp.partidas)
                modeloCampeonatoEscolhido = camp.modelo
                nomeCampeonatoEscolhido = camp.nomeExibicao
                fotoCampeonatoEscolhida = camp.fotoUri
                idCampeonatoAtual = camp.id
                configsCampeonatoAtual = camp.configs.copy()
                configuracaoFinalGrupos = camp.gruposConfig
                telaAtual = "painel_campeonato"
            }
        )

        "cadastrar_campeonato" -> TelaModeloCampeonato(
            onVoltar = { telaAtual = "menu" },
            onSelecionar = { nome: String, foto: String, modelo: String ->
                modeloCampeonatoEscolhido = modelo
                nomeCampeonatoEscolhido = nome
                fotoCampeonatoEscolhida = foto
                isOrganizador = true
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
                onConfirmar = { idaEVolta: Boolean, finalIdaEVolta: Boolean, confrontos: List<Pair<String, String>> ->
                    confrontosDefinidos = confrontos
                    configsCampeonatoAtual = configsCampeonatoAtual.copy(
                        modoIdaEVoltaMataMata = idaEVolta,
                        modoIdaEVoltaFinal = finalIdaEVolta
                    )
                    telaAtual = "selecao_equipes_campeonato"
                }
            )
        }

        "selecao_equipes_campeonato" -> {
            // Resolvendo ambiguidade de sobrecarga usando parâmetros nomeados
            TelaSelecaoEquipesParaCampeonato(
                listaTotal = listaE,
                onFinalizar = { selecionadas: List<EquipeExemplo> ->
                    equipesNoCampeonato.clear()
                    equipesNoCampeonato.addAll(selecionadas)

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
                            idaEVoltaMataMata = configsCampeonatoAtual.modoIdaEVoltaMataMata,
                            idaEVoltaFinal = configsCampeonatoAtual.modoIdaEVoltaFinal
                        )
                        listaPartidasCampeonato.addAll(partidasGeradas)
                        
                        if (idCampeonatoAtual == -1) {
                            val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                            idCampeonatoAtual = novoId
                            listaC.add(CampeonatoSalvo(
                                id = novoId,
                                nomeExibicao = nomeCampeonatoEscolhido,
                                modelo = modeloCampeonatoEscolhido,
                                equipes = equipesNoCampeonato.toList(),
                                partidas = listaPartidasCampeonato.toList(),
                                configs = configsCampeonatoAtual,
                                gruposConfig = configuracaoFinalGrupos,
                                fotoUri = fotoCampeonatoEscolhida
                            ))
                        }
                        
                        telaAtual = "painel_campeonato"
                    }
                },
                onVoltar = { 
                    telaAtual = if (modeloCampeonatoEscolhido.contains("Libertadores", ignoreCase = true)) "config_chaveamento"
                    else "cadastrar_campeonato"
                }
            )
        }
        "distribuicao_grupos" -> {
            TelaDistribuicaoGrupos(
                equipesNoCampeonato.toList(),
                configuracaoFinalGrupos,
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
                        idaEVoltaMataMata = configsCampeonatoAtual.modoIdaEVoltaMataMata,
                        idaEVoltaFinal = configsCampeonatoAtual.modoIdaEVoltaFinal
                    )
                    listaPartidasCampeonato.addAll(partidasGeradas)

                    if (idCampeonatoAtual == -1) {
                        val novoId = (listaC.maxOfOrNull { it.id } ?: 0) + 1
                        idCampeonatoAtual = novoId
                        listaC.add(CampeonatoSalvo(
                            id = novoId,
                            nomeExibicao = nomeCampeonatoEscolhido,
                            modelo = modeloCampeonatoEscolhido,
                            equipes = equipesNoCampeonato.toList(),
                            partidas = listaPartidasCampeonato.toList(),
                            configs = configsCampeonatoAtual,
                            gruposConfig = configuracaoFinalGrupos,
                            fotoUri = fotoCampeonatoEscolhida
                        ))
                    }

                    telaAtual = "painel_campeonato"
                }
            )
        }
        "painel_campeonato" -> {
            TelaPainelCampeonato(
                idCamp = idCampeonatoAtual,
                nomeCamp = nomeCampeonatoEscolhido, 
                fotoCamp = fotoCampeonatoEscolhida,
                equipes = equipesNoCampeonato,
                partidas = listaPartidasCampeonato,
                modelo = modeloCampeonatoEscolhido,
                listaGlobalJogadores = listaJ,
                configsIniciais = configsCampeonatoAtual,
                listaGruposConfig = configuracaoFinalGrupos,
                isOrganizador = isOrganizador,
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
                    telaAtual = if (isOrganizador) "menu" else "telespectador"
                }
            )
        }
        "cadastrar_jogador" -> TelaCadastroJogador(listaGlobalJogadores = listaJ, onVoltar = { telaAtual = "menu" })
        "gerenciar_jogador" -> TelaListaJogadoresGerenciar(
            listaJ = listaJ, 
            onVoltar = { telaAtual = "menu" }, 
            onGerenciar = { jogador -> 
                jogadorSelecionado = jogador
                telaAtual = "detalhes_jogador" 
            }
        )
        "detalhes_jogador" -> {
            jogadorSelecionado?.let { jogador ->
                TelaGerenciarJogadorAdmin(
                    jogador = jogador, 
                    listaGlobalJogadores = listaJ,
                    onVoltar = { telaAtual = "gerenciar_jogador" }
                )
            }
        }
        "cadastrar_equipe" -> TelaCadastroEquipe(listaGlobalEquipes = listaE, onVoltar = { telaAtual = "menu" })
        "gerenciar_equipe" -> {
            TelaListaEquipesGerenciar(
                listaE = listaE, 
                onVoltar = { telaAtual = "menu" }, 
                onGerenciar = { equipe -> 
                    equipeSelecionada = equipe
                    telaAtual = "detalhes_equipe"
                }
            )
        }
        "detalhes_equipe" -> {
            equipeSelecionada?.let { equipe ->
                TelaGerenciarEquipeAdmin(
                    equipe = equipe, 
                    listaGlobalEquipes = listaE,
                    listaGlobalJogadores = listaJ,
                    onAdicionarJogador = { telaAtual = "selecionar_jogador_para_equipe" },
                    onVoltar = { telaAtual = "gerenciar_equipe" }
                )
            }
        }
        "selecionar_jogador_para_equipe" -> {
            equipeSelecionada?.let { equipe ->
                TelaSelecaoJogador(
                    equipeAlvo = equipe, 
                    listaTotal = listaJ, 
                    listaGlobalEquipes = listaE,
                    onFinalizar = { telaAtual = "detalhes_equipe" }
                )
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
