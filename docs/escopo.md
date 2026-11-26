# Escopo — Eixo, versão faculdade

| | |
|---|---|
| **Entrega** | Aplicativo Android, só front-end, dados em memória |
| **Tecnologia** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Prazo** | menos de 1 mês |
| **Base visual** | Design system Eixo (21 telas) — a fonte de verdade de como cada tela deve parecer |

Esta é uma versão reduzida do MVP comercial. O que ficou de fora está listado no fim, para
ninguém gastar tempo construindo o que não vai ser avaliado.

---

## O que entra

| # | Funcionalidade | Telas | Módulo |
|---|---|---|---|
| 1 | Login | Entrar, Conta | ✅ pronto |
| 2 | Cadastro de motorista e administrador | Pessoas, Cadastro de pessoa | ✅ pronto |
| 3 | Cadastro de cargas | Cargas, Cadastro de carga | ✅ pronto (referência) |
| 4 | Cadastro de viagens | Viagens, Nova viagem, Detalhe da viagem | ✅ pronto |
| 5 | Dashboard | Painel | ✅ pronto |
| 6 | Cadastro de combustível (registro de abastecimento) | Combustível, Novo abastecimento | ✅ pronto |

Caminhões vêm prontos nos dados de exemplo — não há tela de cadastro de caminhão.

---

## Critérios de aceite

Uma tela só está pronta quando cumpre **todos** os itens dela e os gerais.

### Gerais — valem para toda tela

- [x] Usa `EixoTela` + `EixoCabecalho`; nenhuma cor, fonte ou espaçamento literal
- [ ] Funciona nos temas claro e escuro (teste pelo catálogo e pelo sistema)
- [x] Lista vazia mostra `EixoEstadoVazio` com uma explicação
- [x] Formulário só mostra erro depois da primeira tentativa de salvar
- [x] Mensagem de erro diz o que fazer ("Informe o peso em quilos"), não só que errou
- [x] Excluir pede confirmação com `EixoDialogoConfirmacao`
- [x] Girar a tela não perde o que foi digitado (`rememberSaveable`)

### 1 · Login — pronto

- [x] Entra com e-mail (admin) ou matrícula (motorista) e senha
- [x] Credencial errada e acesso inativo mostram mensagens diferentes
- [x] Administrador cai no Painel; motorista, em Viagens
- [x] Visual refinado conforme a tela "Entrar" do design system
- [x] Conta mostra os dados do usuário e permite sair

### 2 · Pessoas — pronto

- [x] Lista com filtro Motoristas / Administradores (`EixoSegmentado`)
- [x] Inativo aparece com etiqueta "Inativo"
- [x] Cadastro com perfil; campos de motorista (CPF, CNH, categoria, validade, telefone) **só** quando o perfil é Motorista
- [x] Login não pode repetir outro já cadastrado
- [x] CNH vencida ou vencendo em 30 dias aparece em destaque
- [x] Só administrador acessa

### 3 · Cargas — pronto

- [x] Lista com busca por descrição, cliente ou nota fiscal
- [x] Situação derivada das viagens ("Em viagem", "Agendada")
- [x] Cadastro e edição com validação
- [x] Exclusão bloqueada quando a carga está vinculada a uma viagem

### 4 · Viagens — pronto

- [x] Lista com placa, rota, motorista e etiqueta de status; filtro por status
- [x] Motorista vê **só** as viagens dele
- [x] Nova viagem (só admin): caminhão, motorista e carga por `EixoSeletor`; origem, destino, saída com `EixoCampoDataHora`
- [x] Número gerado com `Dados.proximoNumeroViagem()`
- [x] Um caminhão ou motorista não pode ter duas viagens em andamento
- [x] Detalhe com rota, status, KM e os abastecimentos da viagem
- [x] Iniciar viagem pede o KM inicial (pré-preenchido com `Dados.ultimoKm`); finalizar pede o KM final, que não pode ser menor

### 5 · Painel — pronto

- [x] Números de `calcularIndicadores(...)` — **não recalcule na tela**
- [x] Caminhões por situação (`EixoResumoNumeros`)
- [x] KM rodados (`EixoOdometro`), gasto, litros, km/L médio e R$/km (`EixoKpi`)
- [x] Alertas de consumo fora da faixa com a placa (`EixoAlerta`)
- [x] Eficiência por caminhão (`EixoCartaoEficiencia`), do pior para o melhor
- [x] Alerta quando houver abastecimento sem viagem

### 6 · Combustível — pronto

- [x] Histórico com data, placa, KM, litros e valor; motorista vê só os dele
- [x] KM pré-preenchido com o último conhecido (`EixoCampoKm` com `sugerido`)
- [x] Litros e valor com máscara; preço por litro calculado (`EixoCampoCalculado`)
- [x] Combustível, posto e "Tanque cheio?" obrigatórios
- [x] ARLA opcional: só mostra os campos quando a resposta é Sim
- [x] KM menor que o último registrado é recusado com mensagem clara
- [x] Registro pode ser feito sem viagem (avulso)

---

## Fora deste escopo

Existem no MVP comercial e **não** fazem parte do trabalho:

- Fotos (cupom, tacógrafo, veículo no posto) e check-in/check-out com foto
- Identificação do posto pela localização
- Funcionamento offline e fila de envio
- Senha temporária e troca obrigatória no primeiro acesso
- Relatório da viagem em PDF
- Banco de dados real — o Firebase entra depois, trocando só a camada `dados/`

---

## Cronograma — 4 semanas

| Semana | Base (Pedro) | Módulos |
|---|---|---|
| 1 | Base entregue ✅ · revisar dúvidas · ajustes de componente | Rodar o app, estudar Cargas e o catálogo, criar a branch, **construir as listas** |
| 2 | Revisar Pull Requests das listas | **Formulários** |
| 3 | Revisar Pull Requests · ensaiar a navegação completa | **Detalhe da viagem e Painel** · ligar telas entre módulos |
| 4 | Integração final · revisão visual contra o design system | Correções · roteiro da apresentação |

**Marco da semana 2:** todas as listas funcionando na `main`. Se algum módulo não chegou aqui,
é a hora de redistribuir — não na véspera da entrega.
