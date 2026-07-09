package com.BookLibre.dto

import com.BookLibre.domain.Role

data class LoginPostParams(
    val email: String,
    val password: String
)

data class LoginPostResponse(
    val response: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RegisterParams(
    val fullName: String,
    val description: String = "",
    val email: String,
    val phone: String = "",
    val city: String = "",
    val roles: List<String> = listOf(),
    val biblioKarmas: Int = 0,
    val password: String,
    val passConfirm: String
)