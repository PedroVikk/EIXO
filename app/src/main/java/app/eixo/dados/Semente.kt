package app.eixo.dados

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Dados de exemplo carregados ao abrir o app. Todos fictícios — nomes, placas e
 * documentos não pertencem a ninguém. As datas são relativas a hoje, para o painel
 * do mês nunca aparecer vazio na apresentação.
 */
internal object Semente {
    private val hoje: LocalDate = LocalDate.now()
    private fun dia(deslocamento: Long, hora: Int, minuto: Int = 0): LocalDateTime =
        hoje.plusDays(deslocamento).atTime(LocalTime.of(hora, minuto))

    val usuarios = listOf(
        Usuario("u1", "Marcos Ribeiro", "admin@eixo.app", Perfil.ADMIN),
        Usuario("u2", "João da Silva", "1001", Perfil.MOTORISTA, motorista = DadosMotorista(
            cpf = "000.000.000-01", cnh = "00000000001", categoriaCnh = "E",
            validadeCnh = LocalDate.of(2027, 10, 15), telefone = "(62) 90000-0001")),
        Usuario("u3", "Carlos Nunes", "1002", Perfil.MOTORISTA, motorista = DadosMotorista(
            cpf = "000.000.000-02", cnh = "00000000002", categoriaCnh = "E",
            validadeCnh = LocalDate.of(2026, 12, 3), telefone = "(62) 90000-0002")),
        Usuario("u4", "Ana Prado", "1003", Perfil.MOTORISTA, motorista = DadosMotorista(
            cpf = "000.000.000-03", cnh = "00000000003", categoriaCnh = "E",
            validadeCnh = LocalDate.of(2028, 4, 22), telefone = "(64) 90000-0003")),
        Usuario("u5", "Pedro Alves", "1004", Perfil.MOTORISTA, motorista = DadosMotorista(
            cpf = "000.000.000-04", cnh = "00000000004", categoriaCnh = "D",
            validadeCnh = LocalDate.of(2027, 1, 9), telefone = "(62) 90000-0004")),
        Usuario("u6", "Edson Lima", "1005", Perfil.MOTORISTA, ativo = false, motorista = DadosMotorista(
            cpf = "000.000.000-05", cnh = "00000000005", categoriaCnh = "E",
            validadeCnh = LocalDate.of(2025, 11, 30), telefone = "(62) 90000-0005")),
    )

    val caminhoes = listOf(
        Caminhao("c1", "RTA4B12", "Scania R450", consumoEsperado = 2.00, kmAtual = 152_430),
        Caminhao("c2", "QXZ7C45", "Volvo FH 460", consumoEsperado = 2.10, kmAtual = 87_575),
        Caminhao("c3", "PLM2D88", "Mercedes Actros 2651", consumoEsperado = 1.80, kmAtual = 247_212),
        Caminhao("c4", "KJH5E31", "DAF XF 480", consumoEsperado = 2.05, kmAtual = 61_340),
        Caminhao("c5", "BRA2E19", "Scania R500", consumoEsperado = 1.95, kmAtual = 112_900),
    )

    val cargas = listOf(
        Carga("k1", "Soja em grão", TipoCarga.GRAOS, 32_000, "123456", "Cooperativa Vale Verde"),
        Carga("k2", "Bebidas paletizadas", TipoCarga.GERAL, 24_500, "128877", "Distribuidora Planalto"),
        Carga("k3", "Carne bovina congelada", TipoCarga.REFRIGERADA, 26_000, "130112", "Frigorífico Serra Azul",
            observacao = "Manter a -18 °C"),
        Carga("k4", "Fertilizante NPK", TipoCarga.GERAL, 30_000, "131540", "AgroCentro"),
    )

    val viagens = listOf(
        Viagem("v1", "000125", "c1", "u2", "k1", "Goiânia/GO", "São Paulo/SP", dia(-3, 6, 30),
            StatusViagem.EM_ANDAMENTO, kmInicial = 152_430),
        Viagem("v2", "000126", "c2", "u3", "k2", "Anápolis/GO", "Uberlândia/MG", dia(0, 14),
            StatusViagem.PLANEJADA),
        Viagem("v3", "000124", "c3", "u4", "k3", "Rio Verde/GO", "Campinas/SP", dia(-8, 5),
            StatusViagem.FINALIZADA, kmInicial = 245_826, kmFinal = 247_212),
        Viagem("v4", "000123", "c4", "u5", "k4", "Jataí/GO", "Santos/SP", dia(-12, 4, 30),
            StatusViagem.FINALIZADA, kmInicial = 59_100, kmFinal = 61_340),
        Viagem("v5", "000127", "c5", "u4", null, "Goiânia/GO", "Brasília/DF", dia(1, 5),
            StatusViagem.PLANEJADA),
    )

    val abastecimentos = listOf(
        // RTA4B12 — viagem em andamento
        Abastecimento("a1", "v1", "c1", "u2", dia(-3, 6, 10), 152_430, 400.0, 2_560.00, TipoCombustivel.DIESEL_S10,
            "Auto Posto Vale", tanqueCheio = true, arlaLitros = 30.0, arlaValor = 105.00),
        Abastecimento("a2", "v1", "c1", "u2", dia(-2, 12, 30), 153_310, 450.0, 2_853.00, TipoCombustivel.DIESEL_S10,
            "Posto Rodovale", tanqueCheio = true),
        Abastecimento("a3", "v1", "c1", "u2", dia(-1, 22, 40), 153_980, 340.0, 2_176.00, TipoCombustivel.DIESEL_S10,
            "Auto Posto Vale", tanqueCheio = true, arlaLitros = 25.0, arlaValor = 87.50),
        // PLM2D88 — viagem finalizada, consumo em atenção
        Abastecimento("a4", "v3", "c3", "u4", dia(-8, 4, 40), 245_826, 600.0, 3_816.00, TipoCombustivel.DIESEL_S10,
            "Posto Trevo", tanqueCheio = true),
        Abastecimento("a5", "v3", "c3", "u4", dia(-7, 15, 10), 246_520, 410.0, 2_624.00, TipoCombustivel.DIESEL_S10,
            "Ipiranga Centro", tanqueCheio = true, arlaLitros = 40.0, arlaValor = 140.00),
        Abastecimento("a6", "v3", "c3", "u4", dia(-6, 9, 0), 247_212, 380.0, 2_394.00, TipoCombustivel.DIESEL_S10,
            "Posto Trevo", tanqueCheio = true),
        // KJH5E31 — viagem finalizada
        Abastecimento("a7", "v4", "c4", "u5", dia(-12, 4, 0), 59_100, 500.0, 3_150.00, TipoCombustivel.DIESEL_S500,
            "Auto Posto Vale", tanqueCheio = true),
        Abastecimento("a8", "v4", "c4", "u5", dia(-11, 13, 20), 60_210, 560.0, 3_528.00, TipoCombustivel.DIESEL_S500,
            "Posto Bandeirantes", tanqueCheio = true, arlaLitros = 35.0, arlaValor = 119.00),
        Abastecimento("a9", "v4", "c4", "u5", dia(-10, 18, 5), 61_340, 560.0, 3_500.00, TipoCombustivel.DIESEL_S500,
            "Posto Bandeirantes", tanqueCheio = true),
        // QXZ7C45 — abastecimento sem viagem (avulso)
        Abastecimento("a10", null, "c2", "u3", dia(-1, 7, 15), 87_575, 300.0, 1_890.00, TipoCombustivel.DIESEL_S10,
            "Posto Rodovale", tanqueCheio = true),
    )
}
