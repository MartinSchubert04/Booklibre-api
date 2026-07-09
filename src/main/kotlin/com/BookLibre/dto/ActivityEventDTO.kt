package com.BookLibre.dto

import java.time.LocalDateTime

data class ActivityEventDTO(
    val fecha: LocalDateTime,
    val tipoEvento: String,
    val usuario: String,
    val titulo: String,
    val descripcion: String
)
