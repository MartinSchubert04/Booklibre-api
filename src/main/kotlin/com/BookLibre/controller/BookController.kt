package com.BookLibre.controller

import com.BookLibre.domain.Book
import com.BookLibre.domain.BookFilter
import com.BookLibre.dto.*
import com.BookLibre.dto.ClickLogDTO
import com.BookLibre.service.BookService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/book")
@CrossOrigin(origins = ["*"])
class BookController(val bookService: BookService) {

    @PostMapping("/search")
    fun search(
        @RequestBody @Valid request: SearchRequest
    ): ResponseEntity<SearchResponse> {
        val result = bookService.search(request)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = ResponseEntity.ok(bookService.delete(id))

    @PostMapping("/user/{userId}")
    fun create(@RequestBody @Valid request: BookCreateDTO,
               @PathVariable userId: Int): ResponseEntity<BookRowDTO> {
        return ResponseEntity.ok(bookService.create(request, userId).toRowDTO())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<BookRowDTO> {
        return ResponseEntity.ok(bookService.bookByIdAsRow(id))
    }

    @GetMapping("/getUpdateById/{id}")
    fun getBookUpdateById(@PathVariable id: String): ResponseEntity<BookUpdateDTO> {
        val book = bookService.bookById(id)
        return ResponseEntity.ok(book.toGetInfoBookUpdateDTO())
    }

    @PutMapping("/update/{id}")
    fun updateBook(@PathVariable id: String, @RequestBody @Valid bookBody: BookUpdateDTO): ResponseEntity<BookUpdateDTO?> {
        return ResponseEntity.ok(bookService.update(id, bookBody))
    }

    @GetMapping("/getDetailById/{id}/user/{userId}")
    fun getBookDetailById(
        @PathVariable id: String,
        @PathVariable userId: Int,
    ): ResponseEntity<BookDetailDTO> {
        return ResponseEntity.ok(bookService.getBookDetail(id, userId))
    }

    @PostMapping("/{id}/reserve/{userId}")
    fun reserveBook(@PathVariable id: String,
                    @PathVariable userId: Int,
                    @RequestBody @Valid request: ReservationRequestDTO): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(bookService.createReservation(id, userId, request))
    }

    @GetMapping("/{id}/reviews")
    fun getReviews(
        @PathVariable id: String,
        @RequestParam(defaultValue = "0") page: Int,
    ): ResponseEntity<List<ReviewDTO>> {
        return ResponseEntity.ok(bookService.getReviews(id, page))
    }

    @GetMapping("/{id}/clicks")
    fun getClicks(@PathVariable id: String): ResponseEntity<List<ClickLogDTO>> {
        return ResponseEntity.ok(bookService.getClicksForBook(id))
    }


    @GetMapping("/completed-reservations")
    fun getBooksWithCompletedReservations(): List<Book> {
        return bookService.getBooksWithCompletedReservations()
    }
}