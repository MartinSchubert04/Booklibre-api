package com.BookLibre.dto

import com.BookLibre.domain.Book
import com.BookLibre.domain.Role
import com.BookLibre.domain.User
import java.time.LocalDate

data class UserProfileDTO (
    val id: Int,
    val fullName: String = "",
    val description: String = "",
    val registerDate: LocalDate,
    val biblioKarmas: Int = 0,
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val roles: List<String>? = null,
    var imgUrl: String = "",
    val lentBooks: Int,
    val readedBooks: Int,
    val ownedBooks: List<BookRowDTO>,
)

fun User.toProfileDTO(): UserProfileDTO {
    return UserProfileDTO(
        id = id!!,
        fullName = getFullName(),
        description = getDescription(),
        registerDate = getRegisterDate(),
        biblioKarmas = getBiblioKarmas(),
        email = getEmail(),
        phone = getPhone(),
        city = getCity(),
        roles = getRoles(),
        imgUrl = getImgUrl(),
        lentBooks = getLentBooksAmout(),
        readedBooks = getReadedBooksAmount(),
        ownedBooks = getOwnedBooks().map { it.toRowDTO() }
    )
}

fun User.toCardDTO(): UserCardDTO {
    return UserCardDTO(
        fullName = getFullName(),
        imgUrl = getImgUrl()
    )
}


data class UserCardDTO (
    var fullName: String = "",
    var imgUrl: String = "",
)


data class UserUpdateDTO(
    val fullName: String,
    val description: String,
    val email: String,
    val phone: String,
    val city: String,
    val roles: List<String>,
    val imgUrl: String,
)

data class UpdateResponseDTO(
    val user: UserProfileDTO,
    val token: String,
)
