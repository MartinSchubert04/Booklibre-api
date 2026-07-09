package com.BookLibre.domain

data class NewBookEvent(
    val id: String,
    val fecha: String,
    val tipoEvento: String,
    val titulo: String,
    val usuario: String
)

data class ConfirmedReservationEvent(
    val id: String,
    val fecha: String,
    val tipoEvento: String,
    val titulo: String,
    val usuario: String
)
