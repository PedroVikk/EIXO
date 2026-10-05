"""
Gera as telas do Eixo (versão faculdade) em SVG, prontas para importar no Figma.

No Figma: arraste os arquivos .svg para o canvas. Cada um vira um grupo com camadas
nomeadas (cabecalho, campo-descricao, botao-salvar...). Texto continua texto editável,
ícones viram vetores. Fontes usadas: Archivo, IBM Plex Sans, IBM Plex Mono — todas
disponíveis no Figma (Google Fonts).

Uso:  python gerar_telas.py
"""
from html import escape
from pathlib import Path

SAIDA = Path(__file__).parent / "telas"
W = 360  # largura de frame Android

# ---------------------------------------------------------------- tokens
CLARO = dict(fundo="#EFF2F7", sup="#FFFFFF", sup2="#F5F7FB", borda="#E1E7F1",
             texto="#0D1526", fraco="#5A6478", tenue="#8B95AC",
             acao="#2D5BE3", acaoS="#E8EEFD", sobreAcao="#FFFFFF",
             ok="#0F7A52", okS="#E3F3EB", warn="#A96C08", warnS="#FAEFDA",
             crit="#BE3229", critS="#FCEAE7")
ESCURO = dict(fundo="#080E1B", sup="#111B2E", sup2="#16223A", borda="#25324B",
              texto="#EAEFF9", fraco="#96A3BC", tenue="#6B7793",
              acao="#5B85F5", acaoS="#182747", sobreAcao="#050F1F",
              ok="#3EBE8B", okS="#122A22", warn="#E0A03A", warnS="#2B2213",
              crit="#F0736A", critS="#2E1614")
PETROLEO = "#0E1B3A"
CAB_TEXTO, CAB_FRACO, CAB_ROTULO = "#EAEFF9", "#C7D2E8", "#7E93C4"

SANS, DISPLAY, MONO = "IBM Plex Sans", "Archivo", "IBM Plex Mono"

# Ícones 24×24 de traço, os mesmos do design system.
ICONES = {
    "truck": '<path d="M2 16.5V6.5h11v10"/><path d="M13 10h3.6l3.4 3.6v2.9"/><circle cx="7" cy="17.6" r="2"/><circle cx="17" cy="17.6" r="2"/><path d="M9 17.6h6"/>',
    "fuel": '<path d="M4 20.5V5a2 2 0 0 1 2-2h5a2 2 0 0 1 2 2v15.5"/><path d="M2.8 20.5h11.4"/><path d="M13 8.5h3a2 2 0 0 1 2 2v6a1.6 1.6 0 0 0 3.2 0V10l-2.6-3"/><path d="M6.6 7.6h3.8"/>',
    "route": '<circle cx="6" cy="6" r="2.4"/><circle cx="18" cy="18" r="2.4"/><path d="M6 8.6v4.9a4 4 0 0 0 4 4h5.4"/>',
    "gauge": '<path d="M3.6 17.5a8.8 8.8 0 1 1 16.8 0"/><path d="M12 17.2l4.2-5.4"/>',
    "alert": '<path d="M12 4 2.9 20h18.2z"/><path d="M12 10.2v3.6"/><path d="M12 17h.01"/>',
    "check": '<path d="M4.5 12.6 9.8 18 19.5 6.8"/>',
    "chev": '<path d="M9 5.5l6.5 6.5L9 18.5"/>',
    "back": '<path d="M19 12H5"/><path d="M11 5.5 4.5 12 11 18.5"/>',
    "user": '<circle cx="12" cy="8" r="3.6"/><path d="M4.6 20a7.4 7.4 0 0 1 14.8 0"/>',
    "users": '<circle cx="9" cy="8.5" r="3.2"/><path d="M3 19.5a6 6 0 0 1 12 0"/><circle cx="17" cy="9.5" r="2.6"/><path d="M15.5 14.2a5 5 0 0 1 5.5 5.3"/>',
    "key": '<circle cx="8" cy="15.2" r="3.6"/><path d="M10.6 12.6 20 3.6"/><path d="M16.8 6.4l2.2 2.2"/>',
    "eye": '<path d="M2.4 12S6 5.6 12 5.6 21.6 12 21.6 12 18 18.4 12 18.4 2.4 12 2.4 12z"/><circle cx="12" cy="12" r="2.9"/>',
    "clock": '<circle cx="12" cy="12" r="8.6"/><path d="M12 6.9v5.4l3.4 2"/>',
    "plus": '<path d="M12 5v14M5 12h14"/>',
    "doc": '<path d="M14 3.6H7.6a2 2 0 0 0-2 2v12.8a2 2 0 0 0 2 2h8.8a2 2 0 0 0 2-2V8.2z"/><path d="M14 3.6v4.6h4.4"/><path d="M9 13.4h6M9 16.6h4"/>',
    "flag": '<path d="M6 21V3.6"/><path d="M6 4.8h11.4l-2.2 3.7 2.2 3.7H6"/>',
    "box": '<rect x="3.5" y="4" width="17" height="4.5" rx="1"/><path d="M5 8.5v10a1.5 1.5 0 0 0 1.5 1.5h11a1.5 1.5 0 0 0 1.5-1.5v-10"/><path d="M10 12.5h4"/>',
    "grid": '<rect x="3.5" y="3.5" width="17" height="17" rx="2"/><path d="M10 3.5v17M10 11h10.5"/>',
    "search": '<circle cx="10.5" cy="10.5" r="6"/><path d="M15 15l5 5"/>',
    "calendar": '<rect x="3.5" y="5" width="17" height="15.5" rx="2"/><path d="M3.5 10h17M8 3v4M16 3v4"/>',
    "logout": '<path d="M14 4.5H6.5a2 2 0 0 0-2 2v11a2 2 0 0 0 2 2H14"/><path d="M10 12h10.5"/><path d="M17 8.5l3.5 3.5-3.5 3.5"/>',
    "edit": '<path d="M4 20h4L19 9l-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>',
    "trash": '<path d="M4.5 6.5h15"/><path d="M9.5 6.5V4.5h5v2"/><path d="M6.5 6.5l1 13a1.5 1.5 0 0 0 1.5 1.4h6a1.5 1.5 0 0 0 1.5-1.4l1-13"/>',
    "updown": '<path d="M8 9.5 12 5.5l4 4"/><path d="M8 14.5l4 4 4-4"/>',
    "link": '<path d="M10 14a4 4 0 0 0 5.7 0l3-3a4 4 0 0 0-5.7-5.7l-1 1"/><path d="M14 10a4 4 0 0 0-5.7 0l-3 3a4 4 0 0 0 5.7 5.7l1-1"/>',
}


def icone(nome, x, y, cor, tam=20, traco=1.8):
    s = tam / 24
    return (f'<g transform="translate({x:.1f} {y:.1f}) scale({s:.4f})" fill="none" stroke="{cor}" '
            f'stroke-width="{traco}" stroke-linecap="round" stroke-linejoin="round">{ICONES[nome]}</g>')


# ------------------------------------------------- medida aproximada de texto
def largura(txt, tam, fam=SANS, peso=400, esp=0.0):
    fator = {SANS: 0.47 if peso < 600 else 0.5, DISPLAY: 0.7, MONO: 0.6}[fam]
    return len(txt) * (tam * fator + esp)


def caber(t, max_w, tam, fam=SANS, peso=400, esp=0.0):
    """Corta com reticências o que não cabe — SVG não quebra nem corta texto sozinho."""
    if largura(t, tam, fam, peso, esp) <= max_w:
        return t
    while t and largura(t + "…", tam, fam, peso, esp) > max_w:
        t = t[:-1]
    return t.rstrip(" ·") + "…"


def quebrar(t, max_w, tam, fam=SANS, peso=400):
    linhas, atual = [], ""
    for palavra in t.split():
        tentativa = (atual + " " + palavra).strip()
        if largura(tentativa, tam, fam, peso) <= max_w or not atual:
            atual = tentativa
        else:
            linhas.append(atual)
            atual = palavra
    return linhas + [atual]


def texto(x, y, t, tam, cor, fam=SANS, peso=400, esp=0.0, ancora="start", nome=None):
    a = f' text-anchor="{ancora}"' if ancora != "start" else ""
    ls = f' letter-spacing="{esp}"' if esp else ""
    i = f' id="{nome}"' if nome else ""
    return (f'<text{i} x="{x:.1f}" y="{y:.1f}" font-family="{fam}" font-size="{tam}" '
            f'font-weight="{peso}" fill="{cor}"{ls}{a}>{escape(t)}</text>')


def pintura(prop, cor):
    """#RRGGBBAA vira cor + opacidade: o importador de SVG do Figma não lê alfa no hex."""
    if cor.startswith("#") and len(cor) == 9:
        return f'{prop}="{cor[:7]}" {prop}-opacity="{int(cor[7:], 16) / 255:.2f}"'
    return f'{prop}="{cor}"'


def ret(x, y, w, h, fill, rx=0, stroke=None, sw=1.0, nome=None):
    s = f' {pintura("stroke", stroke)} stroke-width="{sw}"' if stroke else ""
    i = f' id="{nome}"' if nome else ""
    return f'<rect{i} x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" rx="{rx}" {pintura("fill", fill)}{s}/>'


def grupo(nome, *partes):
    return f'<g id="{nome}">' + "".join(partes) + "</g>"


# ------------------------------------------------------------- componentes
class Tela:
    def __init__(self, arquivo, titulo, altura=800, tema=CLARO):
        self.arquivo, self.titulo, self.h, self.c = arquivo, titulo, altura, tema
        self.partes = [ret(0, 0, W, altura, tema["fundo"], nome="fundo")]

    def add(self, *p):
        self.partes.extend(p)

    def svg(self):
        corpo = "".join(self.partes)
        return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{self.h}" '
                f'viewBox="0 0 {W} {self.h}"><title>{escape(self.titulo)}</title>'
                f'<g id="{self.arquivo}">{corpo}</g></svg>')


def barra_status(claro=True):
    cor = CAB_TEXTO if claro else "#0D1526"
    barras = "".join(ret(W - 62 + i * 4.5, 15 - i * 2.4, 3, 4 + i * 2.4, cor, 0.5) for i in range(3))
    return grupo("barra-status",
                 texto(16, 17, "9:41", 12, cor, peso=500),
                 barras,
                 ret(W - 40, 8.5, 21, 10.5, "none", 3, stroke=cor, sw=1.2),
                 ret(W - 38, 10.5, 14, 6.5, cor, 1.5),
                 ret(W - 18.5, 11.8, 1.6, 4, cor, 0.6))


def botao_cab(x, y, nome_icone, nome):
    return grupo(nome, ret(x, y, 36, 36, "none", 9, stroke="#FFFFFF29", sw=1),
                 icone(nome_icone, x + 8.5, y + 8.5, CAB_TEXTO, 19))


def cabecalho(tela, titulo, rotulo=None, voltar=False, acoes=(), altura=88, extra=""):
    p = [ret(0, 0, W, altura, PETROLEO), barra_status()]
    x = 16
    if voltar:
        p.append(botao_cab(16, 34, "back", "botao-voltar"))
        x = 64
    if rotulo:
        p.append(texto(x, 47, rotulo.upper(), 9.5, CAB_ROTULO, MONO, 500, 1.7))
    p.append(texto(x, 68 if rotulo else 62, titulo, 17 if voltar else 20, CAB_TEXTO, DISPLAY, 700, -0.2))
    for i, (ic, nome) in enumerate(acoes):
        p.append(botao_cab(W - 52 - i * 44, 34, ic, nome))
    tela.add(grupo("cabecalho", *p, extra))
    return altura


def rotulo_secao(x, y, t, c, acao=None):
    p = [texto(x, y, t.upper(), 9.5, c["tenue"], MONO, 500, 1.7)]
    if acao:
        p.append(texto(W - 16, y, acao, 12, c["acao"], peso=600, ancora="end"))
    return grupo("rotulo-" + slug(t), *p)


def slug(t):
    s = t.lower().translate(str.maketrans("áàâãéêíóôõúç", "aaaaeeiooouc"))
    return "".join(ch if ch.isalnum() else "-" for ch in s).strip("-")[:28]


TONS = {"neutro": ("fraco", "sup2", "borda"), "acao": ("acao", "acaoS", "acao"),
        "ok": ("ok", "okS", "ok"), "warn": ("warn", "warnS", "warn"), "crit": ("crit", "critS", "crit")}


def etiqueta(x, y, t, tom, c, compacta=False, alinhar_direita=False):
    fg, bg, bd = (c[k] for k in TONS[tom])
    tam = 9 if compacta else 9.5
    w = largura(t.upper(), tam, MONO, 500, 1.5) + (12 if compacta else 30)
    h = 16 if compacta else 22
    if alinhar_direita:
        x = x - w
    p = [ret(x, y, w, h, bg, 5 if compacta else 11, stroke=bd + "55", sw=1)]
    if compacta:
        p.append(texto(x + 6, y + 11.5, t.upper(), tam, fg, MONO, 500, 1.5))
    else:
        p.append(f'<circle cx="{x + 11:.1f}" cy="{y + 11:.1f}" r="3" fill="{fg}"/>')
        p.append(texto(x + 20, y + 14.8, t.upper(), tam, fg, MONO, 500, 1.5))
    return grupo("etiqueta-" + slug(t), *p), w


def campo(tela, y, rotulo, valor=None, placeholder=None, sufixo=None, prefixo=None, mono=False,
          tag=None, tom_tag="acao", dica=None, erro=None, seletor=False, foco=False, alto=52,
          icone_fim=None, detalhe=None, x=16, w=W - 32, nome=None):
    c = tela.c
    p = [texto(x, y + 12, rotulo, 12, c["fraco"], peso=600)]
    if tag:
        e, _ = etiqueta(x + largura(rotulo, 12, peso=600) + 8, y + 1, tag, tom_tag, c, compacta=True)
        p.append(e)
    by = y + 20
    borda = c["crit"] if erro else (c["acao"] if foco else c["borda"])
    p.append(ret(x, by, w, alto, c["sup"], 12, stroke=borda, sw=2 if foco else 1.5))
    tx = x + 14
    base = by + alto / 2 + 5.5
    if prefixo:
        p.append(texto(tx, base, prefixo, 13, c["fraco"], MONO, 500))
        tx += largura(prefixo, 13, MONO) + 8
    if valor is not None:
        if detalhe:
            p.append(texto(tx, by + 22, valor, 15, c["texto"], MONO if mono else SANS, 600 if mono else 400))
            p.append(texto(tx, by + 39, detalhe, 11.5, c["fraco"]))
        else:
            p.append(texto(tx, base, valor, 18 if mono else 16, c["texto"], MONO if mono else SANS, 600 if mono else 400))
    elif placeholder:
        p.append(texto(tx, base, placeholder, 16, c["tenue"]))
    if sufixo:
        p.append(texto(x + w - 14, base, sufixo, 13, c["fraco"], MONO, 500, ancora="end"))
    if seletor:
        p.append(icone("updown", x + w - 34, by + alto / 2 - 10, c["tenue"], 20))
    if icone_fim:
        p.append(icone(icone_fim, x + w - 36, by + alto / 2 - 11, c["tenue"], 22))
    fim = by + alto
    if erro:
        p.append(icone("alert", x, fim + 6, c["crit"], 14))
        p.append(texto(x + 19, fim + 17, erro, 12, c["crit"], peso=500))
        fim += 22
    elif dica:
        p.append(texto(x, fim + 17, dica, 12, c["tenue"]))
        fim += 22
    tela.add(grupo(nome or "campo-" + slug(rotulo), *p))
    return fim


def segmentado(tela, y, opcoes, sel, tom="acao", rotulo=None, x=16, w=W - 32):
    c = tela.c
    p = []
    if rotulo:
        p.append(texto(x, y + 12, rotulo, 12, c["fraco"], peso=600))
        y += 20
    gap = 8
    cw = (w - gap * (len(opcoes) - 1)) / len(opcoes)
    fg, bg, _ = (c[k] for k in TONS[tom])
    for i, o in enumerate(opcoes):
        ox = x + i * (cw + gap)
        ativo = i == sel
        p.append(ret(ox, y, cw, 48, bg if ativo else c["sup"], 12, stroke=fg if ativo else c["borda"], sw=1.5))
        tw = largura(o, 14, peso=600)
        if ativo:
            p.append(icone("check", ox + cw / 2 - tw / 2 - 12, y + 15, fg, 18, 2))
            p.append(texto(ox + cw / 2 + 10, y + 29, o, 14, fg, peso=600, ancora="middle"))
        else:
            p.append(texto(ox + cw / 2, y + 29, o, 14, c["fraco"], peso=600, ancora="middle"))
    tela.add(grupo("segmentado-" + slug(rotulo or opcoes[0]), *p))
    return y + 48


def botao(x, y, w, t, c, variante="primario", h=48, ic=None, nome=None):
    cores = {"primario": (c["acao"], c["sobreAcao"], None), "secundario": ("none", c["texto"], c["borda"]),
             "fantasma": (c["sup2"], c["texto"], c["borda"]), "sucesso": (c["ok"], "#FFFFFF", None),
             "perigo": (c["crit"], "#FFFFFF", None), "off": (c["sup2"], c["tenue"], c["borda"])}
    bg, fg, bd = cores[variante]
    rotulo = t.upper()
    tw = largura(rotulo, 13.5, DISPLAY, 700, 0.9)
    p = [ret(x, y, w, h, bg, 12, stroke=bd, sw=1)]
    cx = x + w / 2
    if ic:
        p.append(icone(ic, cx - tw / 2 - 14, y + h / 2 - 9, fg, 18, 2))
        cx += 12
    p.append(texto(cx, y + h / 2 + 5, rotulo, 13.5, fg, DISPLAY, 700, 0.9, ancora="middle"))
    return grupo(nome or "botao-" + slug(t), *p)


def rodape(tela, t, com_barra=True, ic=None, variante="primario", nota=None):
    c = tela.c
    topo = tela.h - (80 if com_barra else 16) - (88 if nota else 76)
    altura = (88 if nota else 76) + (0 if com_barra else 16)
    p = [ret(0, topo, W, altura, c["sup"]), ret(0, topo, W, 1, c["borda"]),
         botao(16, topo + 14, W - 32, t, c, variante, ic=ic)]
    if nota:
        p.append(texto(W / 2, topo + 78, nota.upper(), 9.5, c["tenue"], MONO, 500, 1.4, ancora="middle"))
    tela.add(grupo("rodape-acao", *p))
    if not com_barra:
        tela.add(indicador_gesto(tela))


def indicador_gesto(tela):
    return ret(W / 2 - 54, tela.h - 10, 108, 4, tela.c["texto"] + "33", 2, nome="indicador-gesto")


BARRA_ADMIN = [("grid", "Painel"), ("route", "Viagens"), ("box", "Cargas"), ("fuel", "Combustível"), ("users", "Pessoas")]
BARRA_MOTORISTA = [("route", "Viagens"), ("fuel", "Combustível"), ("user", "Conta")]


def barra_inferior(tela, itens, ativo):
    c = tela.c
    y = tela.h - 80
    p = [ret(0, y, W, 80, c["sup"]), ret(0, y, W, 1, c["borda"])]
    iw = W / len(itens)
    for i, (ic, rot) in enumerate(itens):
        cor = c["acao"] if rot == ativo else c["tenue"]
        cx = iw * i + iw / 2
        p.append(icone(ic, cx - 12, y + 10, cor, 24))
        p.append(texto(cx, y + 50, rot, 10.5, cor, peso=600, ancora="middle"))
    p.append(indicador_gesto(tela))
    tela.add(grupo("barra-inferior", *p))


def placa(x, y, t, c, escuro=False):
    w = largura(t, 11.5, MONO, 600) + 14
    bg = "#FFFFFF14" if escuro else c["sup2"]
    bd = "#FFFFFF29" if escuro else c["borda"]
    fg = CAB_TEXTO if escuro else c["texto"]
    return grupo("placa-" + t.lower(), ret(x, y, w, 22, bg, 6, stroke=bd, sw=1),
                 texto(x + 7, y + 15.5, t, 11.5, fg, MONO, 600)), w


def linha_lista(tela, y, titulo, sub=None, apoio=None, tom_apoio=None, inicio=None, fim=None,
                destaque=None, alto=None, nome=None):
    c = tela.c
    linhas = 1 + bool(sub) + bool(apoio)
    h = alto or {1: 56, 2: 64, 3: 80}[linhas]
    bd = c[TONS[destaque][0]] + "55" if destaque else c["borda"]
    p = [ret(16, y, W - 32, h, c["sup"], 12, stroke=bd, sw=1)]
    tx = 28
    if inicio and inicio[0] == "placa":
        e, pw = placa(28, y + h / 2 - 11, inicio[1], c)
        p.append(e)
        tx = 28 + pw + 11
    elif inicio and inicio[0] == "icone":
        p.append(ret(28, y + h / 2 - 19, 38, 38, c["sup2"], 8))
        p.append(icone(inicio[1], 37, y + h / 2 - 10, c["fraco"], 20))
        tx = 77
    ty = y + h / 2 - (linhas - 1) * 9 + 5
    # Com três linhas, a etiqueta desce para a última: título e subtítulo ganham a largura toda.
    # (Com placa + etiqueta lado a lado, sobram ~126px — uma rota como "Goiânia → São Paulo" não cabe.)
    etiqueta_embaixo = bool(fim) and linhas == 3
    if fim:
        y_et = (ty + 35 - 15.5) if etiqueta_embaixo else (y + h / 2 - 11)
        e, ew = etiqueta(W - 28, y_et, fim[0], fim[1], c, alinhar_direita=True)
        lim_chip = W - 28 - ew - 10 - tx
        limite = W - 28 - tx if etiqueta_embaixo else lim_chip
    else:
        e = icone("chev", W - 50, y + h / 2 - 11, c["tenue"], 22)
        limite = lim_chip = W - 54 - tx
    p.append(texto(tx, ty, caber(titulo, limite, 14, peso=600), 14, c["texto"], peso=600))
    if sub:
        ty += 18
        p.append(texto(tx, ty, caber(sub, limite if linhas == 3 else lim_chip, 11.5), 11.5, c["fraco"]))
    if apoio:
        ty += 17
        p.append(texto(tx, ty, caber(apoio, lim_chip, 11.5), 11.5, c[TONS[tom_apoio][0]] if tom_apoio else c["fraco"]))
    p.append(e)
    tela.add(grupo(nome or "linha-" + slug(titulo), *p))
    return y + h


def cartao(x, y, w, h, c, nome, *conteudo, destaque=None):
    bd = c[TONS[destaque][0]] + "55" if destaque else c["borda"]
    return grupo(nome, ret(x, y, w, h, c["sup"], 14, stroke=bd, sw=1), *conteudo)


def kpi(x, y, w, rotulo, valor, apoio, c, tom=None, nome=None):
    fg = c[TONS[tom][0]] if tom else c["texto"]
    return cartao(x, y, w, 84, c, nome or "kpi-" + slug(rotulo),
                  texto(x + 12, y + 20, rotulo.upper(), 9.5, c["tenue"], MONO, 500, 1.7),
                  texto(x + 12, y + 48, valor, 18, fg, MONO, 600, -0.3),
                  texto(x + 12, y + 68, apoio, 11, c["fraco"]))


def resumo(tela, y, itens):
    c = tela.c
    gap = 7
    w = (W - 32 - gap * (len(itens) - 1)) / len(itens)
    p = []
    for i, (v, r, tom) in enumerate(itens):
        x = 16 + i * (w + gap)
        fg, bg, bd = (c[k] for k in TONS[tom]) if tom else (c["texto"], c["sup"], c["borda"])
        p.append(ret(x, y, w, 56, bg, 12, stroke=bd if not tom else bd + "55", sw=1))
        p.append(texto(x + w / 2, y + 27, v, 19, fg if tom else c["texto"], MONO, 600, ancora="middle"))
        p.append(texto(x + w / 2, y + 45, r, 10, fg if tom else c["fraco"], ancora="middle"))
    tela.add(grupo("resumo-numeros", *p))
    return y + 56


def odometro(x, y, numero, pequeno=True, unidade="KM"):
    tam = 21 if pequeno else 30
    dw, dh = (19, 31) if pequeno else (26, 44)
    p = []
    for ch in numero:
        if ch.isdigit():
            p.append(ret(x, y, dw, dh, "#141E33", 4, stroke="#2A3958", sw=1))
            p.append(ret(x, y + dh / 2, dw, 1, "#0B1424"))
            p.append(texto(x + dw / 2, y + dh / 2 + tam * 0.36, ch, tam, "#F2F6FF", MONO, 600, ancora="middle"))
            x += dw + 3
        else:
            p.append(f'<circle cx="{x + 4:.1f}" cy="{y + dh - 6:.1f}" r="{2.2 if pequeno else 3}" fill="#7E93C4"/>')
            x += 9
    p.append(texto(x + 6, y + dh - 4, unidade, 10, CAB_ROTULO, MONO, 500, 1.6))
    return grupo("odometro", *p)


def medidor(x, y, w, real, esperado, c, nome="medidor"):
    razao = real / esperado
    tom = "ok" if razao >= 1 else ("warn" if razao >= 0.9 else "crit")
    maximo = esperado / 0.8
    fill = min(real / maximo, 1) * w
    desvio = (razao - 1) * 100
    sinal = "+" if desvio > 0 else ""
    return grupo(nome,
                 ret(x, y, w, 9, c["sup2"], 4.5, stroke=c["borda"], sw=1),
                 ret(x, y, fill, 9, c[TONS[tom][0]], 4.5),
                 ret(x + w * 0.8 - 1, y - 4, 2, 17, c["texto"] + "99", 1),
                 texto(x, y + 25, f"{sinal}{desvio:.1f}%".replace(".", ","), 9.5, c["tenue"], MONO, 500),
                 texto(x + w, y + 25, "meta", 9.5, c["tenue"], MONO, 500, ancora="end")), tom


def cartao_eficiencia(tela, y, placa_t, modelo, real, esperado, rodape_t=None):
    c = tela.c
    h = 118 if rodape_t else 100
    e_placa, pw = placa(28, y + 14, placa_t, c)
    m, tom = medidor(28, y + 70, W - 56, real, esperado, c, nome="medidor-" + placa_t.lower())
    nome_tom = {"ok": "Normal", "warn": "Atenção", "crit": "Crítico"}[tom]
    e_tom, _ = etiqueta(W - 28, y + 14, nome_tom, tom, c, alinhar_direita=True)
    conteudo = [e_placa, texto(28 + pw + 8, y + 29, modelo, 12.5, c["fraco"]), e_tom,
                texto(28, y + 58, f"{real:.2f}".replace(".", ","), 22, c[TONS[tom][0]], MONO, 600),
                texto(28 + 66, y + 58, "km/L", 12.5, c["fraco"]),
                texto(W - 28, y + 58, f"esperado {esperado:.2f}".replace(".", ","), 11.5, c["fraco"], MONO, 500, ancora="end"),
                m]
    if rodape_t:
        conteudo.append(texto(28, y + h - 10, rodape_t, 11.5, c["fraco"]))
    tela.add(cartao(16, y, W - 32, h, c, "eficiencia-" + placa_t.lower(), *conteudo))
    return y + h


def alerta(tela, y, titulo, desc, tom, ic, x=16, w=W - 32):
    c = tela.c
    fg, bg, _ = (c[k] for k in TONS[tom])
    max_w = w - 52 - 14
    linhas = quebrar(desc, max_w, 12)[:2] if desc else []
    h = 46 + 16 * len(linhas)
    partes = [ret(x, y, w, h, c["sup"], 12, stroke=c["borda"], sw=1),
              ret(x, y + 1, 3, h - 2, fg, 1.5),
              ret(x + 13, y + 12, 28, 28, bg, 8),
              icone(ic, x + 19, y + 18, fg, 16),
              texto(x + 52, y + 24, caber(titulo, max_w, 13.5, peso=600), 13.5, c["texto"], peso=600)]
    partes += [texto(x + 52, y + 43 + i * 16, l, 12, c["fraco"]) for i, l in enumerate(linhas)]
    tela.add(grupo("alerta-" + slug(titulo), *partes))
    return y + h


def linha_valor(x, y, w, rot, val, c, mono=True, ultima=False):
    p = [texto(x, y + 22, rot, 13, c["fraco"]),
         texto(x + w, y + 22, val, 13 if mono else 13, c["texto"], MONO if mono else SANS, 500 if mono else 400, ancora="end")]
    if not ultima:
        p.append(ret(x, y + 35, w, 1, c["borda"]))
    return grupo("valor-" + slug(rot), *p)


def estado_vazio(tela, y, ic, titulo, desc):
    c = tela.c
    tela.add(grupo("estado-vazio",
                   f'<circle cx="{W / 2}" cy="{y + 30}" r="30" fill="{c["sup2"]}" stroke="{c["borda"]}"/>',
                   icone(ic, W / 2 - 14, y + 16, c["tenue"], 28),
                   texto(W / 2, y + 88, titulo, 17, c["texto"], DISPLAY, 700, ancora="middle"),
                   texto(W / 2, y + 110, desc, 13, c["fraco"], ancora="middle")))


# ================================================================== telas
def tela_login():
    t = Tela("01-login", "Entrar", tema=ESCURO)
    c = t.c
    t.partes[0] = ret(0, 0, W, 800, PETROLEO, nome="fundo")
    t.add('<defs><radialGradient id="brilho" cx="0.85" cy="0" r="0.9"><stop offset="0" stop-color="#2D5BE3" stop-opacity="0.35"/>'
          '<stop offset="1" stop-color="#2D5BE3" stop-opacity="0"/></radialGradient>'
          '<linearGradient id="marca" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#3D6BF0"/><stop offset="1" stop-color="#12275C"/></linearGradient></defs>',
          ret(0, 0, W, 800, "url(#brilho)", nome="brilho"), barra_status())
    t.add(grupo("marca", ret(20, 104, 56, 56, "url(#marca)", 14), icone("truck", 34, 118, "#FFFFFF", 28),
                texto(20, 214, "Eixo", 40, c["texto"], DISPLAY, 800, -1.2),
                texto(20, 238, "Gestão de frota", 15, c["fraco"])))
    y = campo(t, 268, "E-mail ou matrícula", placeholder="admin@eixo.app ou 1001", x=20, w=W - 40)
    y = campo(t, y + 14, "Senha", valor="••••••", foco=True, icone_fim="eye", x=20, w=W - 40)
    t.add(botao(20, y + 18, W - 40, "Entrar", c, h=56))
    alerta(t, y + 88, "Esqueceu a senha?", "Fale com o administrador da empresa.", "acao", "key", x=20, w=W - 40)
    t.add(rotulo_secao(20, 700, "Modo demonstração · senha 123456", c))
    t.add(botao(20, 712, (W - 48) / 2, "Administrador", c, "secundario", h=42),
          botao(28 + (W - 48) / 2, 712, (W - 48) / 2, "Motorista", c, "secundario", h=42))
    t.add(ret(W / 2 - 54, 790, 108, 4, "#FFFFFF33", 2, nome="indicador-gesto"))
    return t


def tela_painel():
    t = Tela("02-painel", "Painel", altura=1180)
    c = t.c
    cab = cabecalho(t, "Painel", "Controle da frota", acoes=[("logout", "botao-sair")], altura=158,
                    extra=texto(16, 104, "KM RODADOS · OUTUBRO", 9.5, CAB_ROTULO, MONO, 500, 1.7) + odometro(16, 114, "5.176"))
    y = resumo(t, cab + 16, [("5", "Caminhões", None), ("1", "Em viagem", "acao"), ("4", "Livres", None), ("1", "Sem viagem", "warn")])
    t.add(rotulo_secao(16, y + 28, "Indicadores do mês", c))
    kw = (W - 42) / 2
    t.add(kpi(16, y + 40, kw, "Combustível", "R$ 28.942", "diesel + ARLA", c),
          kpi(26 + kw, y + 40, kw, "Litros", "4.500 L", "preço médio R$ 6,33/L", c),
          kpi(16, y + 134, kw, "Média", "1,92 km/L", "ponderada", c, tom="warn"),
          kpi(26 + kw, y + 134, kw, "Custo por km", "R$ 3,37", "combustível", c))
    y += 230
    t.add(rotulo_secao(16, y + 26, "Precisa de você", c))
    y = alerta(t, y + 38, "3 caminhões abaixo do esperado", "RTA4B12 · KJH5E31 · PLM2D88", "warn", "gauge")
    y = alerta(t, y + 10, "1 abastecimento sem viagem", "QXZ7C45 · ontem, 07:15", "warn", "route")
    t.add(rotulo_secao(16, y + 30, "Eficiência por caminhão", c, acao="Ver viagens"))
    y = cartao_eficiencia(t, y + 42, "PLM2D88", "Actros 2651", 1.75, 1.80, "2 trechos · 1.386 km")
    y = cartao_eficiencia(t, y + 10, "KJH5E31", "DAF XF 480", 2.00, 2.05, "2 trechos · 2.240 km")
    cartao_eficiencia(t, y + 10, "RTA4B12", "Scania R450", 1.96, 2.00, "2 trechos · 1.550 km")
    barra_inferior(t, BARRA_ADMIN, "Painel")
    return t


def tela_pessoas():
    t = Tela("03-pessoas", "Pessoas")
    c = t.c
    y = cabecalho(t, "Pessoas", "Cadastro")
    y = segmentado(t, y + 16, ["Motoristas", "Administradores"], 0)
    t.add(rotulo_secao(16, y + 28, "5 motoristas", c))
    y += 40
    for nome, sub, apoio, fim, tom_apoio in [
        ("João da Silva", "Matrícula 1001 · CNH E", "CNH válida até 15/10/2027", None, None),
        ("Carlos Nunes", "Matrícula 1002 · CNH E", "CNH vence em 03/12/2026", ("Vence logo", "warn"), "warn"),
        ("Ana Prado", "Matrícula 1003 · CNH E", "CNH válida até 22/04/2028", None, None),
        ("Pedro Alves", "Matrícula 1004 · CNH D", "CNH válida até 09/01/2027", None, None),
        ("Edson Lima", "Matrícula 1005 · CNH E", "CNH vencida em 30/11/2025", ("Inativo", "neutro"), "crit"),
    ]:
        y = linha_lista(t, y, nome, sub, apoio, tom_apoio, inicio=("icone", "user"), fim=fim) + 8
    rodape(t, "Novo cadastro", ic="plus")
    barra_inferior(t, BARRA_ADMIN, "Pessoas")
    return t


def tela_pessoa_form():
    t = Tela("04-cadastro-pessoa", "Cadastro de pessoa", altura=1010)
    c = t.c
    y = cabecalho(t, "Novo cadastro", "Pessoas", voltar=True)
    y = segmentado(t, y + 16, ["Motorista", "Administrador"], 0, rotulo="Perfil")
    y = campo(t, y + 16, "Nome completo", "João da Silva")
    y = campo(t, y + 14, "Matrícula", "1001", mono=True, dica="É o login do motorista no app.")
    t.add(rotulo_secao(16, y + 30, "Dados do motorista", c))
    y = campo(t, y + 42, "CPF", "000.000.000-01", mono=True)
    hw = (W - 42) / 2
    campo(t, y + 14, "CNH", "00000000001", mono=True, w=hw)
    y = campo(t, y + 14, "Categoria", "E", seletor=True, x=26 + hw, w=hw)
    y = campo(t, y + 14, "Validade da CNH", "15/10/2027", mono=True, icone_fim="calendar")
    y = campo(t, y + 14, "Telefone", "(62) 90000-0001", mono=True)
    segmentado(t, y + 16, ["Ativo", "Inativo"], 0, tom="ok", rotulo="Situação")
    rodape(t, "Cadastrar pessoa", com_barra=False)
    return t


def tela_cargas():
    t = Tela("05-cargas", "Cargas")
    c = t.c
    y = cabecalho(t, "Cargas", "Cadastro")
    y = campo(t, y + 16, "Buscar", placeholder="Descrição, cliente ou nota fiscal", icone_fim="search")
    t.add(rotulo_secao(16, y + 30, "4 cargas cadastradas", c))
    y += 42
    for titulo, sub, apoio, fim in [
        ("Bebidas paletizadas", "Distribuidora Planalto · NF 128877", "Carga geral · 24.500 kg", ("Agendada", "neutro")),
        ("Carne bovina congelada", "Frigorífico Serra Azul · NF 130112", "Refrigerada · 26.000 kg", None),
        ("Fertilizante NPK", "AgroCentro · NF 131540", "Carga geral · 30.000 kg", None),
        ("Soja em grão", "Cooperativa Vale Verde · NF 123456", "Grãos · 32.000 kg", ("Em viagem", "acao")),
    ]:
        y = linha_lista(t, y, titulo, sub, apoio, inicio=("icone", "box"), fim=fim) + 8
    rodape(t, "Nova carga", ic="plus")
    barra_inferior(t, BARRA_ADMIN, "Cargas")
    return t


def tela_carga_form():
    t = Tela("06-cadastro-carga", "Cadastro de carga")
    c = t.c
    y = cabecalho(t, "Editar carga", "Cargas", voltar=True)
    y = alerta(t, y + 16, "Vinculada à viagem #000125", "Goiânia/GO → São Paulo/SP · não pode ser excluída", "acao", "link")
    y = campo(t, y + 14, "Descrição", "Soja em grão")
    y = campo(t, y + 14, "Tipo de carga", "Grãos", seletor=True)
    y = campo(t, y + 14, "Peso", "32.000", sufixo="kg", mono=True)
    y = campo(t, y + 14, "Cliente", "Cooperativa Vale Verde")
    campo(t, y + 14, "Nota fiscal", "123456", mono=True, tag="Opcional", tom_tag="neutro")
    rodape(t, "Salvar alterações", com_barra=False)
    return t


def tela_viagens():
    t = Tela("07-viagens", "Viagens")
    c = t.c
    y = cabecalho(t, "Viagens", "Operação")
    y = segmentado(t, y + 16, ["Todas", "Em andamento", "Planejadas"], 0)
    t.add(rotulo_secao(16, y + 28, "5 viagens", c))
    y += 40
    for titulo, sub, apoio, pl, fim, dest, tom_apoio in [
        ("Goiânia → São Paulo", "#000125 · João da Silva", "1.550 km · 3 abastecimentos", "RTA4B12", ("Em viagem", "acao"), None, None),
        ("Anápolis → Uberlândia", "#000126 · Carlos Nunes", "Saída hoje, 14:00", "QXZ7C45", ("Planejada", "neutro"), None, None),
        ("Goiânia → Brasília", "#000127 · Ana Prado", "Saída amanhã, 05:00", "BRA2E19", ("Planejada", "neutro"), None, None),
        ("Rio Verde → Campinas", "#000124 · Ana Prado", "1.386 km · 1,75 km/L", "PLM2D88", ("Finalizada", "ok"), None, None),
        ("Jataí → Santos", "#000123 · Pedro Alves", "2.240 km · 2,00 km/L", "KJH5E31", ("Finalizada", "ok"), None, None),
    ]:
        y = linha_lista(t, y, titulo, sub, apoio, tom_apoio, inicio=("placa", pl), fim=fim, destaque=dest) + 8
    rodape(t, "Nova viagem", ic="plus")
    barra_inferior(t, BARRA_ADMIN, "Viagens")
    return t


def tela_viagem_form():
    t = Tela("08-nova-viagem", "Nova viagem", altura=880)
    c = t.c
    y = cabecalho(t, "Nova viagem", "Viagem #000128", voltar=True)
    y = campo(t, y + 16, "Caminhão", "BRA2E19", detalhe="Scania R500 · disponível", mono=True, seletor=True, alto=58)
    y = campo(t, y + 14, "Motorista", "Pedro Alves", detalhe="CNH D · sem viagem em aberto", seletor=True, alto=58)
    y = campo(t, y + 14, "Carga", "Fertilizante NPK", detalhe="AgroCentro · 30.000 kg", seletor=True, alto=58,
              tag="Opcional", tom_tag="neutro")
    hw = (W - 42) / 2
    campo(t, y + 14, "Origem", "Goiânia/GO", w=hw)
    y = campo(t, y + 14, "Destino", "Cuiabá/MT", x=26 + hw, w=hw)
    y = campo(t, y + 14, "Saída prevista", "07/10/2026 05:30", mono=True, icone_fim="calendar")
    alerta(t, y + 16, "KM inicial vem na saída", "O motorista informa ao iniciar a viagem.", "acao", "gauge")
    rodape(t, "Criar viagem", com_barra=False)
    return t


def tela_viagem_detalhe():
    t = Tela("09-detalhe-viagem", "Detalhe da viagem", altura=960)
    c = t.c
    pl, pw = placa(64, 84, "RTA4B12", c, escuro=True)
    et, _ = etiqueta(64 + pw + 8, 84, "Em andamento", "acao", {**c, "acao": "#7EA0FF", "acaoS": "#2D5BE333"})
    extra = (pl + et + texto(16, 134, "KM ATUAL · TACÓGRAFO", 9.5, CAB_ROTULO, MONO, 500, 1.7)
             + odometro(16, 144, "153.980"))
    cab = cabecalho(t, "Goiânia → São Paulo", "#000125 · João da Silva", voltar=True,
                    acoes=[("edit", "botao-editar")], altura=192, extra=extra)
    y = resumo(t, cab + 16, [("1.550", "km rodados", None), ("3", "abastec.", None), ("1.190", "litros", None), ("1,96", "km/L", "warn")])
    y = cartao_eficiencia(t, y + 12, "RTA4B12", "Scania R450", 1.96, 2.00)
    t.add(rotulo_secao(16, y + 28, "Abastecimentos", c, acao="Registrar"))
    y += 40
    for km, info, val in [("152.430 km", "há 3 dias · 400 L · cheio", "R$ 2.560"),
                          ("153.310 km", "há 2 dias · 450 L · cheio", "R$ 2.853"),
                          ("153.980 km", "ontem · 340 L · cheio · ARLA 25 L", "R$ 2.176")]:
        t.add(grupo("abastecimento-" + slug(km),
                    ret(16, y, W - 32, 60, c["sup"], 12, stroke=c["borda"], sw=1),
                    ret(28, y + 12, 36, 36, c["sup2"], 8), icone("fuel", 36, y + 20, c["fraco"], 20),
                    texto(76, y + 27, km, 14, c["texto"], MONO, 600),
                    texto(76, y + 45, info, 11.5, c["fraco"]),
                    texto(W - 28, y + 35, val, 13, c["texto"], MONO, 600, ancora="end")))
        y += 68
    hw = (W - 42) / 2
    t.add(grupo("rodape-acao", ret(0, t.h - 92, W, 92, c["sup"]), ret(0, t.h - 92, W, 1, c["borda"]),
                botao(16, t.h - 78, hw, "Abastecer", c, ic="fuel"),
                botao(26 + hw, t.h - 78, hw, "Finalizar", c, "secundario", ic="flag")),
          indicador_gesto(t))
    return t


def tela_combustivel():
    t = Tela("10-combustivel", "Combustível")
    c = t.c
    y = cabecalho(t, "Combustível", "Abastecimentos")
    y = resumo(t, y + 16, [("R$ 28.942", "no mês", None), ("4.500 L", "litros", None), ("R$ 6,33", "média/L", None)])
    t.add(rotulo_secao(16, y + 28, "Últimos registros", c))
    y += 40
    for pl, titulo, sub, apoio, fim, tom_apoio in [
        ("RTA4B12", "R$ 2.176,00 · 340 L", "Auto Posto Vale · 153.980 km", "Ontem, 22:40 · ARLA 25 L", None, None),
        ("QXZ7C45", "R$ 1.890,00 · 300 L", "Posto Rodovale · 87.575 km", "Sem viagem vinculada", ("Avulso", "warn"), "warn"),
        ("RTA4B12", "R$ 2.853,00 · 450 L", "Posto Rodovale · 153.310 km", "Há 2 dias, 12:30", None, None),
        ("PLM2D88", "R$ 2.394,00 · 380 L", "Posto Trevo · 247.212 km", "Há 6 dias, 09:00", None, None),
        ("PLM2D88", "R$ 2.624,00 · 410 L", "Ipiranga Centro · 246.520 km", "Há 7 dias · ARLA 40 L", None, None),
    ]:
        y = linha_lista(t, y, titulo, sub, apoio, tom_apoio, inicio=("placa", pl), fim=fim) + 8
    rodape(t, "Registrar abastecimento", ic="fuel")
    barra_inferior(t, BARRA_ADMIN, "Combustível")
    return t


def tela_abastecimento_form():
    t = Tela("11-novo-abastecimento", "Novo abastecimento", altura=1120)
    c = t.c
    y = cabecalho(t, "Abastecimento", "Viagem #000125 · RTA4B12", voltar=True)
    y = campo(t, y + 16, "KM no tacógrafo", "153.980", sufixo="km", mono=True, tag="Pré-preenchido", foco=True, alto=58,
              dica="Vinha 153.500 (último abastecimento) · +480 km")
    hw = (W - 42) / 2
    campo(t, y + 14, "Litros", "390,00", sufixo="L", mono=True, w=hw, alto=58)
    y = campo(t, y + 14, "Valor total", "2.769,00", prefixo="R$", mono=True, x=26 + hw, w=hw, alto=58)
    y = campo(t, y + 14, "Preço por litro", "R$ 7,10", sufixo="calculado", mono=True, dica="Valor total ÷ litros. Confira com o cupom.")
    y = campo(t, y + 14, "Combustível", "Diesel S10", seletor=True)
    y = campo(t, y + 14, "Posto", "Auto Posto Vale")
    y = segmentado(t, y + 16, ["Sim", "Não"], 0, tom="ok", rotulo="Tanque cheio?")
    t.add(texto(16, y + 18, "Só tanque cheio serve de referência para km/L.", 12, c["tenue"]))
    y = segmentado(t, y + 34, ["Não", "Sim"], 1, rotulo="Abasteceu ARLA 32?")
    campo(t, y + 14, "ARLA", "25,00", sufixo="L", mono=True, w=hw)
    y = campo(t, y + 14, "Valor do ARLA", "87,50", prefixo="R$", mono=True, x=26 + hw, w=hw)
    t.add(texto(16, y + 18, "Entra no custo por km, nunca no cálculo de km/L.", 12, c["tenue"]))
    rodape(t, "Salvar abastecimento", com_barra=False, variante="sucesso", ic="check")
    return t


def tela_conta():
    t = Tela("12-conta", "Conta")
    c = t.c
    y = cabecalho(t, "Conta")
    et, _ = etiqueta(W - 28, y + 30, "Motorista", "acao", c, alinhar_direita=True)
    t.add('<defs><linearGradient id="avatar" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#3D6BF0"/>'
          '<stop offset="1" stop-color="#12275C"/></linearGradient></defs>',
          cartao(16, y + 16, W - 32, 196, c, "cartao-usuario",
                 ret(28, y + 28, 48, 48, "url(#avatar)", 12),
                 texto(52, y + 58, "JS", 17, "#FFFFFF", MONO, 600, ancora="middle"),
                 texto(88, y + 48, "João da Silva", 17, c["texto"], DISPLAY, 700),
                 texto(88, y + 66, "1001", 13, c["fraco"], MONO, 500), et,
                 linha_valor(28, y + 96, W - 56, "CNH", "00000000001 · E", c),
                 linha_valor(28, y + 132, W - 56, "Validade da CNH", "15/10/2027", c),
                 linha_valor(28, y + 168, W - 56, "Telefone", "(62) 90000-0001", c, ultima=True)))
    y += 228
    t.add(botao(16, y, W - 32, "Catálogo de componentes", c, "fantasma", ic="grid"),
          botao(16, y + 60, W - 32, "Sair", c, "secundario", ic="logout"))
    barra_inferior(t, BARRA_MOTORISTA, "Conta")
    return t


TELAS = [tela_login, tela_painel, tela_pessoas, tela_pessoa_form, tela_cargas, tela_carga_form,
         tela_viagens, tela_viagem_form, tela_viagem_detalhe, tela_combustivel, tela_abastecimento_form, tela_conta]


if __name__ == "__main__":
    SAIDA.mkdir(parents=True, exist_ok=True)
    for f in TELAS:
        t = f()
        (SAIDA / f"{t.arquivo}.svg").write_text(t.svg(), encoding="utf-8")
        print(f"{t.arquivo}.svg  {W}x{t.h}")
