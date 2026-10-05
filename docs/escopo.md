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
| 1 | Login | Entrar, Conta | Acesso |
| 2 | Cadastro de motorista e administrador | Pessoas, Cadastro de pessoa | Acesso |
| 3 | Cadastro de cargas | Cargas, Cadastro de carga | ✅ pronto (referência) |
| 4 | Cadastro de viagens | Viagens, Nova viagem, Detalhe da viagem | Viagens |
| 5 | Dashboard | Painel | Painel |
| 6 | Cadastro de combustível (registro de abastecimento) | Combustível, Novo abastecimento | Combustível |

Caminhões vêm prontos nos dados de exemplo — não há tela de cadastro de caminhão.

---

## Critérios de aceite

Uma tela só está pronta quando cumpre **todos** os itens dela e os gerais.

### Gerais — valem para toda tela

- [ ] Usa `EixoTela` + `EixoCabecalho`; nenhuma cor, fonte ou espaçamento literal
- [ ] Funciona nos temas claro e escuro (teste pelo catálogo e pelo sistema)
- [ ] Lista vazia mostra `EixoEstadoVazio` com uma explicação
- [ ] Formulário só mostra erro depois da primeira tentativa de salvar
- [ ] Mensagem de erro diz o que fazer ("Informe o peso em quilos"), não só que errou
- [ ] Excluir pede confirmação com `EixoDialogoConfirmacao`
- [ ] Girar a tela não perde o que foi digitado (`rememberSaveable`)

### 1 · Login — já funcional

- [x] Entra com e-mail (admin) ou matrícula (motorista) e senha
- [x] Credencial errada e acesso inativo mostram mensagens diferentes
- [x] Administrador cai no Painel; motorista, em Viagens
- [ ] Visual refinado conforme a tela "Entrar" do design system
- [x] Conta mostra os dados do usuário e permite sair

### 2 · Pessoas

- [ ] Lista com filtro Motoristas / Administradores (`EixoSegmentado`)
- [ ] Inativo aparece com etiqueta "Inativo"
- [ ] Cadastro com perfil; campos de motorista (CPF, CNH, categoria, validade, telefone) **só** quando o perfil é Motorista
- [ ] Login não pode repetir outro já cadastrado
- [ ] CNH vencida ou vencendo em 30 dias aparece em destaque
- [ ] Só administrador acessa

### 3 · Cargas — pronto

- [x] Lista com busca por descrição, cliente ou nota fiscal
- [x] Situação derivada das viagens ("Em viagem", "Agendada")
- [x] Cadastro e edição com validação
- [x] Exclusão bloqueada quando a carga está vinculada a uma viagem

### 4 · Viagens

- [ ] Lista com placa, rota, motorista e etiqueta de status; filtro por status
- [ ] Motorista vê **só** as viagens dele
- [ ] Nova viagem (só admin): caminhão, motorista e carga por `EixoSeletor`; origem, destino, saída com `EixoCampoDataHora`
- [ ] Número gerado com `Dados.proximoNumeroViagem()`
- [ ] Um caminhão ou motorista não pode ter duas viagens em andamento
- [ ] Detalhe com rota, status, KM e os abastecimentos da viagem
- [ ] Iniciar viagem pede o KM inicial (pré-preenchido com `Dados.ultimoKm`); finalizar pede o KM final, que não pode ser menor

### 5 · Painel

- [ ] Números de `calcularIndicadores(...)` — **não recalcule na tela**
- [ ] Caminhões por situação (`EixoResumoNumeros`)
- [ ] KM rodados (`EixoOdometro`), gasto, litros, km/L médio e R$/km (`EixoKpi`)
- [ ] Alertas de consumo fora da faixa com a placa (`EixoAlerta`)
- [ ] Eficiência por caminhão (`EixoCartaoEficiencia`), do pior para o melhor
- [ ] Alerta quando houver abastecimento sem viagem

### 6 · Combustível

- [ ] Histórico com data, placa, KM, litros e valor; motorista vê só os dele
- [ ] KM pré-preenchido com o último conhecido (`EixoCampoKm` com `sugerido`)
- [ ] Litros e valor com máscara; preço por litro calculado (`EixoCampoCalculado`)
- [ ] Combustível, posto e "Tanque cheio?" obrigatórios
- [ ] ARLA opcional: só mostra os campos quando a resposta é Sim
- [ ] KM menor que o último registrado é recusado com mensagem clara
- [ ] Registro pode ser feito sem viagem (avulso)

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
