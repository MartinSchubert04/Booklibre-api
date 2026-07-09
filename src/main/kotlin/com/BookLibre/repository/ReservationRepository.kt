package com.BookLibre.repository

import com.BookLibre.domain.Reservation
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ReservationRepository : JpaRepository<Reservation, Int> {

    @Query("""
        SELECT DISTINCT r FROM Reservation r
        JOIN FETCH r.reader
        JOIN FETCH r.owner
        WHERE r.reader.id = :readerId
    """)
    fun findByReaderId(@Param("readerId") readerId: Int): List<Reservation>

    @Query("""
        SELECT DISTINCT r FROM Reservation r
        JOIN FETCH r.reader
        JOIN FETCH r.owner
        WHERE r.owner.id = :ownerId
    """)
    fun findByBookOwnerId(@Param("ownerId") ownerId: Int): List<Reservation>

    fun findByBookId(bookId: String): List<Reservation>

    @Query("SELECT r FROM Reservation r JOIN FETCH r.reader ORDER BY r.from DESC")
    fun findRecentWithReader(pageable: Pageable): List<Reservation>

    @Query("""
        SELECT DISTINCT r.bookId
        FROM Reservation r
        WHERE r.bookId IN :bookIds
        AND r.from <= :until
        AND r.until >= :from
    """)
    fun getReservedBookIdsInRange(
        @Param("bookIds") bookIds: List<String>,
        @Param("from") from: LocalDate,
        @Param("until") until: LocalDate
    ): Set<String>

    @Query("""
        SELECT r.bookId, COUNT(r)
        FROM Reservation r
        WHERE r.bookId IN :bookIds
        GROUP BY r.bookId
    """)
    fun countReservasByBookIds(@Param("bookIds") bookIds: List<String>): List<Array<Any>>
}
