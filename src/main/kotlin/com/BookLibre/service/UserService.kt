package com.BookLibre.service

import com.BookLibre.domain.Role
import com.BookLibre.domain.User
import com.BookLibre.dto.ReservationResponseDTO
import com.BookLibre.repository.UserRepository
import com.BookLibre.repository.ReservationRepository
import com.BookLibre.domain.UserProps
import com.BookLibre.dto.LoginPostParams
import com.BookLibre.dto.ProfileResponse
import com.BookLibre.dto.RegisterParams
import com.BookLibre.dto.UpdateResponseDTO
import com.BookLibre.dto.UserProfileDTO
import com.BookLibre.dto.UserUpdateDTO
import com.BookLibre.dto.toDTO
import com.BookLibre.dto.toProfileDTO
import com.BookLibre.error.BusinessException
import com.BookLibre.error.NotFoundException
import com.BookLibre.repository.BookRepository
import com.BookLibre.repository.ClickLogRepository
import com.BookLibre.repository.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val reservationRepository: ReservationRepository,
    val roleRepository: RoleRepository,
    val jwtService: JwtService,
    val bookRepository: BookRepository,
    val clickLogRepository: ClickLogRepository
) {

    @Transactional(readOnly = true)
    fun getAll(): List<UserProfileDTO> {
        return userRepository.findAll().map { it.toProfileDTO() }  // ← mapea dentro de la transacción
    }


    private fun getEntity(id: Int): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("User not found") }
    }

    @Transactional(readOnly = true)
    fun getReservationsMade(userId: Int): List<ReservationResponseDTO> {
        val reservations = reservationRepository.findByReaderId(userId)
        return mapReservationsWithRatings(reservations)
    }

    @Transactional(readOnly = true)
    fun getReservationsReceived(userId: Int): List<ReservationResponseDTO> {
        val reservations = reservationRepository.findByBookOwnerId(userId)
        return mapReservationsWithRatings(reservations)
    }

    private fun mapReservationsWithRatings(
        reservations: List<com.BookLibre.domain.Reservation>
    ): List<ReservationResponseDTO> {
        if (reservations.isEmpty()) return emptyList()

        val bookIds = reservations.map { it.bookId }.distinct()
        val books = bookRepository.findAllById(bookIds).associateBy { it.id!! }

        val avgRatingByBook: Map<String, Double?> = books.mapValues { it.value.getRating() }
        val myRatingByReservationId: Map<Int, Double?> = books.values
            .flatMap { it.getReservations() }
            .mapNotNull { embed -> embed.id?.let { it to embed.rating } }
            .toMap()

        return reservations.map { reservation ->
            reservation.toDTO(
                bookRating = avgRatingByBook[reservation.bookId],
                myCalificacion = reservation.id?.let { myRatingByReservationId[it] }
            )
        }
    }

    @Transactional(readOnly = true)
    fun get(id: Int): UserProfileDTO {
        val user = getEntity(id)
        val books = bookRepository.findByOwnerId(id)
        user.setOwnedBooks(books.toMutableList())

        return user.toProfileDTO()
    }

    @Transactional(readOnly = true)
    fun getProfile(id: Int): ProfileResponse {
        val user = getEntity(id)
        val books = bookRepository.findByOwnerId(id)
        user.setOwnedBooks(books.toMutableList())

        val clicks = getClicks(id)

        return ProfileResponse(user.toProfileDTO(), clicks)
    }

    @Transactional
    fun login(loginData: LoginPostParams): Triple<Int, String,String> {
        if(loginData.email == ""){
            throw BusinessException("Email vacío")
        }
        if(loginData.password == "") {
            throw BusinessException("Contraseña vacía")
        }

        val user = userRepository.findByProps_Email(loginData.email)
            ?: throw NotFoundException("Usuario no encontrado")

        if (!passwordEncoder.matches(loginData.password, user.getPassword())) {
            throw BusinessException("Usuario y/o contraseña incorrectos")
        }

        val token = jwtService.generateToken(user.getId(), user.getRoles())
        val refreshToken = jwtService.generateRefreshToken(user.getId(),user.getRoles())

        return Triple(user.getId(), token, refreshToken)
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): String {

        if (!jwtService.isTokenValid(refreshToken)) {
            throw BusinessException("Refresh token inválido")
        }

        val userId = jwtService.extractUserId(refreshToken)

        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("Usuario no encontrado") }

        return jwtService.generateToken(user.getId(), user.getRoles())
    }

    @Transactional
    fun register(registerParams: RegisterParams): User {

        if(registerParams.fullName == ""){
            throw BusinessException("Nombre vacío")
        }
        if(registerParams.email == ""){
            throw BusinessException("Email vacío")
        }
        if(registerParams.password == ""){
            throw BusinessException("Contraseña vacía")
        }
        if(registerParams.password != registerParams.passConfirm){
            throw BusinessException("Las contraseñas no coinciden")
        }

        if (userRepository.findByProps_Email(registerParams.email) != null) {
            throw BusinessException("Email ya en uso")
        }

        val roles = findRoles(registerParams.roles)

        val user = User(
            UserProps().apply {
                fullName = registerParams.fullName
                description = registerParams.description
                email = registerParams.email
                phone = registerParams.phone
                city = registerParams.city
                biblioKarmas = registerParams.biblioKarmas
                password = passwordEncoder.encode(registerParams.password)
            }
        ).apply {
            roles.forEach { addRole(it) }
        }

        return userRepository.save(user)
    }

    @Transactional
    fun update(id: Int, dto: UserUpdateDTO): UpdateResponseDTO {
        val user = getEntity(id)

        // Verificar que el email no esté en uso por OTRO usuario
        val existingWithEmail = userRepository.findByProps_Email(dto.email)
        if (existingWithEmail != null && existingWithEmail.getId() != id) {
            throw BusinessException("El email ya está en uso por otro usuario")
        }

        val roles = findRoles(dto.roles)

        user.update(
            fullName = dto.fullName,
            email = dto.email,
            description = dto.description,
            phone = dto.phone,
            city = dto.city,
            imgUrl = dto.imgUrl
        )

        user.setRoles(roles.toMutableSet())

        val savedUser = userRepository.save(user)
        val books = bookRepository.findByOwnerId(id)
        savedUser.setOwnedBooks(books.toMutableList())

        val newToken = jwtService.generateToken(savedUser.getId(), savedUser.getRoles())

        return UpdateResponseDTO(savedUser.toProfileDTO(), newToken)
    }


    private fun getClicks(userId: Int): Map<String, Int> {
        val bookIds = bookRepository.findByOwnerId(userId).map { it.id.toString() }
        return clickLogRepository.countGroupedByBookIdIn(bookIds).associate { it.id to it.count }
    }


    fun findRoles(roleNames: List<String>): List<Role> {
        return roleNames.map { name ->
            roleRepository.findByName(name)
                ?: throw NotFoundException("Rol no encontrado: $name")
        }
    }
}


