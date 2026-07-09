package com.BookLibre.repository

import com.BookLibre.domain.Book
import com.BookLibre.domain.BookTypeAverageRating
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BookRepository : MongoRepository<Book, String> {
    @Query("{ 'props.owner.id': ?0 }")
    fun findByOwnerId(ownerId: Int): List<Book>

    @Query("{ 'props.reservations.0': { \$exists: true }, 'props.reservations': { \$not: { \$elemMatch: { 'until': { \$gte: ?0 } } } } }")
    fun findBooksWithAllReservationsCompleted(today: LocalDate): List<Book>


    fun findTop5ByOrderByCreatedAtDesc(): List<Book>


    // --- buckets cathalog health
    @Query(value = "{ 'props.reservations': { \$elemMatch: { 'from': { \$lte: ?0 }, 'until': { \$gte: ?0 } } } }", count = true)
    fun countPrestados(today: LocalDate): Long
    @Query(value = "{ \$or: [ { 'props.reservations': { \$exists: false } }, { 'props.reservations': { \$size: 0 } } ] }", count = true)
    fun countNuncaReservados(): Long
    @Query(value = "{ 'props.reservations': { \$elemMatch: { 'until': { \$lt: ?0 } } }, \$nor: [ { 'props.reservations': { \$elemMatch: { 'from': { \$lte: ?0 }, 'until': { \$gte: ?0 } } } } ] }", count = true)
    fun countDevueltos(today: LocalDate): Long
    @Query(value = "{ 'props.reservations': { \$elemMatch: { 'from': { \$gt: ?0 } } }, \$nor: [ { 'props.reservations': { \$elemMatch: { 'from': { \$lte: ?0 }, 'until': { \$gte: ?0 } } } }, { 'props.reservations': { \$elemMatch: { 'until': { \$lt: ?0 } } } } ] }", count = true)
    fun countReservadosAFuturo(today: LocalDate): Long

    //  1) $unwind: descompone props.reservations[] en docs individuales
    //  2) $match: descarta reservas sin rating (excluye libros sin reseñas)
    //  3) $group por (bookId, typeClass): promedio POR libro — un libro con 50 reseñas
    //     no pesa mas que uno con 1 (consigna: "promedio de promedios ponderado por libro")
    //  4) $group por typeClass: promedio de promedios por tipo
    //  5) $project: mapea el shape al DTO BookTypeRating
    @Aggregation(pipeline = [
        "{ \$unwind: '\$props.reservations' }",
        "{ \$match: { 'props.reservations.rating': { \$ne: null } } }",
        "{ \$group: { _id: { bookId: '\$_id', typeClass: '\$props.type._class' }, avgPerBook: { \$avg: '\$props.reservations.rating' } } }",
        "{ \$group: { _id: '\$_id.typeClass', averageRating: { \$avg: '\$avgPerBook' } } }",
        "{ \$project: { _id: 0, type: '\$_id', averageRating: 1 } }"
    ])
    fun aggregateAverageRatingByType(): List<BookTypeAverageRating>



}


