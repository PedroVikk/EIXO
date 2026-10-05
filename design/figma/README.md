# Telas do Eixo para o Figma

12 telas do escopo da faculdade, em SVG, no tamanho Android (360 px de largura).

| Arquivo | Tela |
|---|---|
| `01-login.svg` | Entrar |
| `02-painel.svg` | Painel do administrador (rolável, 1180 px) |
| `03-pessoas.svg` | Lista de motoristas e administradores |
| `04-cadastro-pessoa.svg` | Cadastro de pessoa (motorista) |
| `05-cargas.svg` | Lista de cargas |
| `06-cadastro-carga.svg` | Editar carga |
| `07-viagens.svg` | Lista de viagens |
| `08-nova-viagem.svg` | Nova viagem |
| `09-detalhe-viagem.svg` | Detalhe da viagem |
| `10-combustivel.svg` | Lista de abastecimentos |
| `11-novo-abastecimento.svg` | Novo abastecimento |
| `12-conta.svg` | Conta do motorista |

## Importar

1. No Explorador de Arquivos, abra `design/figma/telas/` e selecione os 12 arquivos.
2. Arraste todos de uma vez para o canvas do Figma.
3. Cada tela entra como um grupo. Para virar frame: selecione e use **Ctrl+Alt+G** (*Frame selection*).

As camadas já vêm com nome (`cabecalho`, `campo-descricao`, `barra-inferior`, `botao-salvar`…).
Texto continua texto editável; ícones viram vetores.

**Fontes:** Archivo, IBM Plex Sans e IBM Plex Mono — todas já existem no Figma.

## O que o SVG não carrega

- **Auto Layout e componentes.** O Figma importa posições fixas. Se o grupo for editar muito,
  vale transformar botão, campo e linha de lista em componentes.
- **A largura variável da Archivo.** Os títulos chegam na largura normal.

## Mudar as telas

As telas são geradas por `gerar_telas.py`, que usa os mesmos tokens do app:

```bash
python gerar_telas.py
```

Edite o script para mudar todas as telas de uma vez. Se o grupo preferir editar direto no Figma,
o Figma passa a ser a fonte de verdade — e o script deixa de ser usado.
