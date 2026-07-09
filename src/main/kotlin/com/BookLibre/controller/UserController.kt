package com.BookLibre.controller

import com.BookLibre.domain.User
import com.BookLibre.dto.ReservationResponseDTO
import com.BookLibre.dto.LoginPostParams
import com.BookLibre.dto.ProfileResponse
import com.BookLibre.dto.RefreshTokenRequest
import com.BookLibre.dto.RegisterParams
import com.BookLibre.dto.UpdateResponseDTO
import com.BookLibre.dto.UserProfileDTO
import com.BookLibre.dto.UserUpdateDTO
import com.BookLibre.error.BusinessException
import com.BookLibre.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = ["*"])
class UserController(val userService: UserService) {

    @GetMapping("/getAll")
    fun getAll(): ResponseEntity<List<UserProfileDTO>> {
        return ResponseEntity.ok(userService.getAll())
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Int): ResponseEntity<UserProfileDTO> {
        return ResponseEntity.ok(userService.get(id))
    }
    @GetMapping("/{id}/profile")
    fun getProfile(@PathVariable id: Int): ResponseEntity<ProfileResponse> {
        return ResponseEntity.ok(userService.getProfile(id))
    }

    @GetMapping("/{userId}/reservations/made")
    fun getReservationsMade(@PathVariable userId: Int): ResponseEntity<List<ReservationResponseDTO>> {
        return ResponseEntity.ok(userService.getReservationsMade(userId))
    }

    @GetMapping("/{userId}/reservations/received")
    fun getReservationsReceived(@PathVariable userId: Int): ResponseEntity<List<ReservationResponseDTO>> {
        return ResponseEntity.ok(userService.getReservationsReceived(userId))
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginData: LoginPostParams): ResponseEntity<Map<String, Any>> {
        val (userId, token,refreshToken) = userService.login(loginData)
        return ResponseEntity.ok(mapOf(
            "id" to userId,
            "token" to token,
            "refreshToken" to refreshToken
        ))
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid request: RefreshTokenRequest): ResponseEntity<Map<String, String>> {
        val newToken = userService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(mapOf("token" to newToken))
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid registerParams: RegisterParams): ResponseEntity<User> {
        return ResponseEntity.status(201).body(userService.register(registerParams))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int,
               @RequestBody @Valid user: UserUpdateDTO): ResponseEntity<UpdateResponseDTO> {
        return ResponseEntity.ok(userService.update(id, user))
    }

}