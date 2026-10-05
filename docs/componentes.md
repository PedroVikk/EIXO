# Componentes do Eixo

Todos em `app/src/main/java/app/eixo/ui/componentes/`. Veja cada um funcionando no
**Catálogo** (tela de login → "Catálogo de componentes"), nos temas claro e escuro.

## Estrutura de tela

| Componente | Para quê | Parâmetros que importam |
|---|---|---|
| `EixoTela` | Esqueleto de **toda** tela | `cabecalho`, `rodape`, `barraInferior`, `rolavel` (false para LazyColumn) |
| `EixoCabecalho` | Cabeçalho petróleo | `titulo`, `sobretitulo`, `aoVoltar`, `acoes`, `conteudo` (odômetro, rota) |
| `EixoBotaoCabecalho` | Ícone de ação no cabeçalho | `icone`, `descricao` (acessibilidade), `aoClicar` |
| `EixoRodapeAcao` | Faixa fixa com a ação principal | conteúdo: normalmente um `EixoBotao` |
| `EixoNotaRodape` | Texto pequeno sob o botão | ex.: por que o botão está desabilitado |
| `EixoBarraInferior` | Barra de navegação | já montada em `EixoApp.kt` — só repasse o parâmetro `barra` |
| `EixoEstadoVazio` | Lista vazia ou sem resultado | `icone`, `titulo`, `descricao`, `acao` |
| `EixoDialogoConfirmacao` | Antes de excluir ou cancelar | `textoConfirmar` diz a ação ("Excluir carga"), `perigoso` |

## Ação

| Componente | Para quê | Parâmetros que importam |
|---|---|---|
| `EixoBotao` | Qualquer botão | `variante` (Primario, Secundario, Fantasma, Sucesso, Perigo), `tamanho` (Pequeno, Normal, Grande), `icone`, `habilitado`, `carregando` |

## Entrada de dados

Todos têm **rótulo sempre visível**, `dica` e `erro`. Mostre `erro` só depois da primeira tentativa de salvar.

| Componente | Guarda | Mostra | Use para |
|---|---|---|---|
| `EixoCampo` | `String` | o texto | nome, cliente, origem; `linhas = 3` para observação |
| `EixoCampoSenha` | `String` | •••• com olho | senha |
| `EixoCampoKm` | dígitos `"152430"` | `152.430 km` | KM — com `sugerido` mostra "Pré-preenchido" e a diferença |
| `EixoCampoLitros` | dígitos `"39000"` | `390,00 L` | litros |
| `EixoCampoDinheiro` | centavos `"276900"` | `R$ 2.769,00` | qualquer valor em reais |
| `EixoCampoInteiro` | dígitos | `32.000 kg` | peso, quantidade |
| `EixoCampoCalculado` | — | valor só leitura | preço por litro calculado |
| `EixoSeletor` | o item escolhido | folha inferior com a lista | motorista, caminhão, carga, combustível |
| `EixoSegmentado` | índice | 2–3 opções lado a lado | filtros, perfil |
| `EixoSimNao` | `Boolean?` | Sim / Não | "Tanque cheio?", "Abasteceu ARLA?" |
| `EixoCampoData` | `LocalDate?` | `15/10/2027` | validade da CNH |
| `EixoCampoDataHora` | `LocalDateTime?` | `19/08/2026 06:30` | saída da viagem |

Converter os campos numéricos na hora de salvar (`util/Formatos.kt`):

```kotlin
digitosParaKm("152430")       // 152430L
digitosParaDecimal("276900")  // 2769.0
decimalParaDigitos(2769.0)    // "276900"  — para preencher o campo ao editar
```

## Exibição

| Componente | Para quê | Parâmetros que importam |
|---|---|---|
| `EixoCartao` | Agrupar conteúdo | `aoClicar`, `destaque` (Tom), `espacamento` |
| `EixoRotuloSecao` | Título pequeno de seção | `acao` + `aoClicarAcao` ("Ver todas") |
| `EixoLinhaLista` | Linha de **toda** lista | `titulo`, `subtitulo`, `apoio`, `inicio` (placa/ícone), `fim` (etiqueta), `destaque` |
| `EixoLinhaValor` | "Rótulo ...... valor" em resumo | `numerico`, `ultima` |
| `EixoEtiqueta` | Status | `tom`, `pulsando` (só em andamento), `compacta` |
| `EixoAlerta` | Aviso com faixa de severidade | `titulo`, `icone`, `descricao`, `tom` |
| `EixoPlaca` | Placa do caminhão | `sobreEscuro` dentro do cabeçalho |
| `EixoKpi` | Indicador com rótulo e valor | `apoio`, `tomValor`, `conteudoInferior` |
| `EixoResumoNumeros` | 2–4 números lado a lado | lista de `NumeroResumo(valor, rotulo, tom)` |
| `EixoOdometro` | KM em dígitos mecânicos | `pequeno` |
| `EixoMedidor` | Consumo real contra esperado | `real`, `esperado` |
| `EixoCartaoEficiencia` | Cartão pronto por caminhão | `placa`, `real`, `esperado`, `descricao`, `rodape` |

## Tom

Cor de estado sempre por `Tom` — nunca por cor direta:

| Tom | Quando |
|---|---|
| `Tom.Neutro` | planejada, cancelada, inativo, opcional |
| `Tom.Acao` | em andamento, selecionado, informação |
| `Tom.Sucesso` | finalizada, consumo normal, salvo |
| `Tom.Atencao` | consumo entre 90% e 99% do esperado, pendência leve |
| `Tom.Critico` | consumo abaixo de 90%, erro, pendência que bloqueia |

Para status de viagem e classificação de consumo:

```kotlin
classificarConsumo(real, esperado).tom()   // Classificacao → Tom
```
