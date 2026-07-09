package com.BookLibre.domain

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.*
import kotlin.math.ceil
import kotlin.math.floor


interface BookTypeStrategy {
    fun getTypeName(): String
    fun getBiblioKarmas(user: User, book: Book): Int

    companion object {
        fun fromString(typeString: String): BookTypeStrategy {
            return when (typeString) {
                "Común", "Libro Común" -> CommonStrategy()
                "Con dedicatoria", "Con Dedicatoria" -> DedicationStrategy()
                "Coleccionable" -> CollectableStrategy()
                else -> CommonStrategy()
            }
        }
    }
}



class CommonStrategy: BookTypeStrategy {
    override fun getTypeName() = "Libro Común"
    override fun getBiblioKarmas(user: User, book: Book): Int {
        val scalar = if (user.getBiblioKarmas() < 1000) {
            5
        } else {
            2
        }

        return book.getTotalPages() * scalar
    }
}

class DedicationStrategy(): BookTypeStrategy {
    override fun getTypeName() = "Con Dedicatoria"
    override fun getBiblioKarmas(user: User, book: Book): Int {
        return 200 + 10 * book.getAmountReservations()
    }
}

class CollectableStrategy: BookTypeStrategy {
    override fun getTypeName() = "Coleccionable"
    override fun getBiblioKarmas(user: User, book: Book): Int {
        return ceil(user.getBiblioKarmas() / 5.0).toInt() + book.getTotalPages()
    }
}



