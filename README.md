# Eixo — app de gestão de frota (versão faculdade)

Aplicativo Android em **Kotlin + Jetpack Compose** para controle de viagens, cargas e
abastecimento de uma frota de caminhões.

- **Banco local (Room):** os cadastros ficam gravados no aparelho e sobrevivem a fechar o app.
- **Login (Firebase Authentication):** e-mail e senha conferidos no Firebase, quando o projeto
  está configurado (veja *Firebase* abaixo). Sem ele, vale a senha de demonstração.
- **Requisição HTTP (Retrofit + OkHttp):** UF e cidade de origem e destino da viagem vêm da API
  pública de localidades do IBGE.

> Escopo, critérios de aceite e cronograma: [`docs/escopo.md`](docs/escopo.md)
> Referência de cada componente: [`docs/componentes.md`](docs/componentes.md)

---

## Rodar

**Android Studio:** *File → Open* → esta pasta → aguarde o Gradle → ▶ Run em um emulador.

**Terminal:**
```bash
./gradlew assembleDebug      # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest  # roda os testes das regras de cálculo, do login e do repositório
```

O app precisa de internet para o login com Firebase e para a lista de cidades. Sem rede, a
origem e o destino viram campo de texto livre no formato `Cidade/UF`.

### Entrar no app

Senha de todo mundo: **`123456`**

| Perfil | Login | O que vê |
|---|---|---|
| Administrador | `admin@eixo.app` | Painel, Viagens, Cargas, Combustível, Pessoas |
| Motorista | `1001` (João), `1002`, `1003`, `1004` | Viagens, Combustível, Conta |
| Motorista inativo | `1005` | é barrado no login |

A tela de login também tem **atalhos de demonstração** ("Administrador" / "Motorista")
e o botão **Catálogo de componentes** — abra o catálogo antes de construir qualquer tela.

### Banco local (Room)

Na primeira abertura o banco nasce com os dados de exemplo (`dados/Semente.kt`). Depois disso,
tudo que for cadastrado, editado ou excluído fica gravado. Para voltar aos dados de exemplo:
*Configurações do Android → Apps → Eixo → Armazenamento → Limpar dados*.

Ver as tabelas com o app rodando: *View → Tool Windows → App Inspection → Database Inspector*.
Tabelas: `usuarios`, `caminhoes`, `cargas`, `viagens`, `abastecimentos` (`dados/Banco.kt`).

### Firebase (login)

O login usa o projeto Firebase **`appeixo`**, já configurado em `app/google-services.json`, com o
provedor **E-mail/senha** ativo. Os usuários ficam em *Authentication → Users* no
[console do Firebase](https://console.firebase.google.com).

Sem esse arquivo, o plugin `google-services` não é aplicado e o app entra com a senha de
demonstração — útil para rodar sem internet.

Com o Firebase ligado, o e-mail do administrador é o próprio login e o do motorista é a
matrícula + `@eixo.app` (`1001` → `1001@eixo.app`). Quem existe no app mas ainda não tem conta
no Firebase ganha a conta no primeiro acesso, com a senha inicial `123456` — não é preciso criar
usuário no console. Perfil e situação (ativo/inativo) continuam no banco local.

---

## O que já está pronto

| Parte | Onde | Situação |
|---|---|---|
| Design system (cores, tipografia, medidas, tema claro/escuro) | `ui/design/` | ✅ pronto |
| 33 componentes reutilizáveis | `ui/componentes/` | ✅ pronto |
| Catálogo vivo de componentes | `ui/catalogo/` | ✅ pronto |
| Banco local (Room), login (Firebase) e API do IBGE (Retrofit) | `dados/` | ✅ pronto |
| Cálculos do painel (consumo, custo/km) | `dominio/` | ✅ pronto e testado |
| Navegação e barra inferior por perfil | `ui/navegacao/` | ✅ pronto |
| Login e Conta | `ui/telas/acesso/` | ✅ pronto |
| **Cargas (lista + formulário)** | `ui/telas/cargas/` | ✅ **tela de referência** |
| Pessoas (motoristas e admins) | `ui/telas/usuarios/` | ✅ pronto |
| Viagens (lista, formulário, detalhe) | `ui/telas/viagens/` | ✅ pronto |
| Combustível (lista, detalhe, formulário) | `ui/telas/combustivel/` | ✅ pronto |
| Painel | `ui/telas/painel/` | ✅ pronto |

Todas as telas do escopo estão construídas. As telas só conhecem `Dados` e `Sessao`: trocar onde
os dados ficam guardados mexe apenas na camada `dados/`.

---

## Divisão do trabalho

Cada módulo mexe **só na própria pasta**. Assim dois membros nunca editam o mesmo arquivo e o
merge não vira briga.

| Módulo | Pasta | Telas | Figma | Responsável |
|---|---|---|---|---|
| Base | `ui/design`, `ui/componentes`, `ui/navegacao`, `dados`, `dominio` | — | — | Pedro |
| Pessoas | `ui/telas/usuarios` | Lista de pessoas, Cadastro de pessoa | `03`, `04` | Victor Hugo |
| Viagens | `ui/telas/viagens` | Viagens, Nova viagem, Detalhe (iniciar/finalizar) | `07`, `08`, `09` | Kayque Vêncio |
| Combustível | `ui/telas/combustivel` | Abastecimentos, Novo abastecimento | `10`, `11` | Luccas |
| Painel e acesso | `ui/telas/painel`, `ui/telas/acesso` | Painel, refino do Login e da Conta | `01`, `02`, `12` | Matheus Souza |

Viagens é o módulo mais pesado. Com só três pessoas além da base, junte **Combustível + Painel**.
As telas de referência visual estão em [`design/figma/`](design/figma/README.md).

### Fluxo no git

```bash
git checkout -b modulo/viagens       # uma branch por módulo
# ...trabalho...
git push -u origin modulo/viagens    # abre Pull Request para main
```

O Pull Request é revisado por quem cuida da base antes de entrar na `main`.
**Precisa de um componente novo ou de mudar um existente?** Peça para quem cuida da base —
não edite `ui/componentes` por conta própria: um ajuste ali muda a tela de todo mundo.

---

## Regras de ouro

1. **Toda tela começa com `EixoTela`.** Ela cuida de fundo, margens, teclado e barras do sistema.
2. **Nenhuma cor, fonte ou espaçamento literal.** Nada de `Color(0xFF…)`, `16.sp` ou `13.dp` solto —
   use `Eixo.cores`, `EixoTipo` e `EixoEspaco`. É o que mantém as telas de quatro pessoas com cara de um app só.
3. **Todo número usa fonte de dado** (`EixoTipo.dado*` ou `numerico = true` no campo).
4. **Estado nunca só por cor.** Etiqueta sempre tem texto; alerta sempre tem ícone.
5. **Um botão primário por tela.** E botão desabilitado explica o porquê logo abaixo.
6. **A tela não conhece o `NavController`.** Recebe callbacks (`aoVoltar`, `aoAbrir`) — veja `EixoApp.kt`.

---

## Construindo a sua tela em 5 passos

1. Abra o **Catálogo** no app e encontre os componentes que a sua tela usa.
2. Abra `ui/telas/cargas/CargasTela.kt` e `CargaFormTela.kt` — são o modelo.
3. No arquivo do seu módulo, monte a tela com `EixoTela` e os componentes do catálogo.
4. Leia dados com `Dados.xxx.itens.collectAsState()` e grave com `Dados.xxx.salvar(...)`.
5. Compare com a tela correspondente no design system antes de abrir o Pull Request.

```kotlin
@Composable
fun ViagensTela(barra: @Composable () -> Unit, aoNova: () -> Unit, aoAbrir: (String) -> Unit) {
    val viagens by Dados.viagens.itens.collectAsState()
    EixoTela(
        cabecalho = { EixoCabecalho("Viagens") },
        rodape = { EixoRodapeAcao { EixoBotao("Nova viagem", aoClicar = aoNova) } },
        barraInferior = barra,
        rolavel = false,
    ) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EixoEspaco.s2)) {
            items(viagens) { v ->
                EixoLinhaLista(titulo = v.rota, subtitulo = "#${v.numero}", aoClicar = { aoAbrir(v.id) })
            }
        }
    }
}
```

---

## Estrutura

```
app/src/main/java/app/eixo/
├── EixoAplicacao.kt  Abre o banco e prepara o login ao iniciar o app
├── MainActivity.kt
├── dados/          Modelos (tabelas), banco Room, repositórios, dados de exemplo, login
│   └── remoto/     API de localidades do IBGE (Retrofit)
├── dominio/        Regras de cálculo (consumo, classificação, indicadores do painel)
├── util/           Formatação de número, dinheiro e data
└── ui/
    ├── design/       Cores, tipografia, medidas, tema
    ├── componentes/  Os componentes Eixo*
    ├── catalogo/     Vitrine dos componentes
    ├── navegacao/    Rotas e NavHost
    └── telas/        Uma pasta por módulo
```

---

## Problemas conhecidos

**Windows com acento no nome do usuário** (ex.: `C:\Users\Fulano Teó`):

- *"Unable to establish loopback connection"* ao rodar o Gradle pelo terminal — o JDK 17 não abre o
  socket local numa pasta temporária com acento. Rode antes de `./gradlew`:
  ```bash
  export TMP='C:\uma\pasta\sem\acento' TEMP="$TMP" GRADLE_OPTS="-Djava.io.tmpdir=$TMP -Djdk.net.unixdomain.tmpdir=$TMP"
  ```
- *"ClassNotFoundException: GradleWorkerMain"* nos testes — causado por `-Dfile.encoding=UTF-8` em
  `org.gradle.jvmargs`. Por isso o `gradle.properties` deste projeto **não** tem essa opção. Não adicione.

**Emulador aparece como `unauthorized` no adb:** no Android Studio, *Device Manager → ⋮ → Wipe Data*
no dispositivo virtual e inicie de novo com *Cold Boot*.

---
