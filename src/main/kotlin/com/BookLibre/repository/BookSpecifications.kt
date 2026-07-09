package com.BookLibre.repository

import com.BookLibre.domain.Book
import com.BookLibre.domain.BookProps
import com.BookLibre.domain.Reservation
import com.BookLibre.domain.User
import com.BookLibre.domain.UserProps
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate

object BookSpecifications {

    fun titleContains(title: String): Specification<Book> {
        return Specification { root, _, cb ->
            if (title.isBlank()) null
            else cb.like(cb.lower(root.get<BookProps>("props").get("title")), "%${title.lowercase()}%")
        }
    }

    fun isbnContains(isbn: String): Specification<Book> {
        return Specification { root, _, cb ->
            if (isbn.isBlank()) null
            else cb.like(root.get<BookProps>("props").get("isbn"), "%$isbn%")
        }
    }

    fun genreIn(genres: List<String>): Specification<Book> {
        return Specification { root, _, cb ->
            if (genres.isEmpty()) null
            else cb.lower(root.get<BookProps>("props").get("genre")).`in`(genres.map { it.lowercase() })
        }
    }

    fun pagesBetween(min: Int, max: Int): Specification<Book> {
        return Specification { root, _, cb ->
            cb.between(root.get<BookProps>("props").get("totalPages"), min, max)
        }
    }

    fun ownerNameContains(ownerName: String): Specification<Book> {
        return Specification { root, _, cb ->
            if (ownerName.isBlank()) null
            else {
                val ownerJoin = root.get<BookProps>("props").get<User>("owner")
                cb.like(
                    cb.lower(ownerJoin.get<UserProps>("props").get("fullName")),
                    "%${ownerName.lowercase()}%"
                )
            }
        }
    }

    fun availableInRange(from: LocalDate, until: LocalDate): Specification<Book> {
        return Specification { root, query, cb ->
            val subquery = query.subquery(Int::class.java)
            val reservation = subquery.from(Reservation::class.java)
            subquery.select(cb.literal(1))
            subquery.where(
                cb.equal(reservation.get<Book>("book"), root),
                cb.lessThanOrEqualTo(reservation.get("from"), until),
                cb.greaterThanOrEqualTo(reservation.get("until"), from)
            )
            cb.not(cb.exists(subquery))
        }
    }

}
