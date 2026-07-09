package com.BookLibre.domain

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "reservaciones")
class Reservation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    val owner: User,// usar estos datos para lo faltante

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id")
    val reader: User,

    @Column(name = "book_id") val bookId: String,
    @Column(name = "book_title") val bookTitle: String,
    @Column(name = "book_author") val bookAuthor: String,
    @Column(name = "book_img_url", columnDefinition = "TEXT") val bookImgUrl: String,

    @Column(name = "from_date") val from: LocalDate,
    @Column(name = "until_date") val until: LocalDate
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    // No se persiste en PostgreSQL — se asigna en memoria para calcular karmas
    @jakarta.persistence.Transient
    var book: Book? = null

    fun calculateBiblioKarmas(): Int {
        return book?.getBiblioKarmas(reader, from, until) ?: 0
    }
}
