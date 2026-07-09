package com.BookLibre.domain

data class Leaderboard(
    val rows: List<LeaderboardRow>
)

data class LeaderboardRow(
    val user: String,
    val bibliokarmas: Int
)

data class ConversionRow(
    val bookId: String,
    val title: String,
    val clicks: Int,
    val reservas: Int,
    val tasaConversion: Double
)

data class CatalogHealth(
    val total: Int,
    val prestados: Int,
    val disponiblesNuncaReservados: Int,
    val disponiblesReservadosAFuturo: Int,
    val disponiblesDevueltos: Int
)