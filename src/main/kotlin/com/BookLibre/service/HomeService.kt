package com.BookLibre.service

import com.BookLibre.dto.BookCardDTO
import com.BookLibre.dto.toEntity
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class HomeService(
    private val clickService: ClickService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository
) {

    fun getHomeData(userId: Int?): List<BookCardDTO> {
        val popular = clickService.getTopBooks()
        // getBibliokarmas() espera un User
        // por lo cual la query a postgres debe hacerse de todas formas, el strategy necesita data del user
        val user = userRepository.findById(userId ?: return popular).orElse(null) ?: return popular

        popular.forEach { book ->
            book.bibliokarmas = book.toEntity().getBiblioKarmas(user)
        }

        return popular
    }
}