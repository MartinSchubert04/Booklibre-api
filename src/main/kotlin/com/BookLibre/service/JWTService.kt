package com.BookLibre.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    private val expirationMs = 1000L * 60 * 60 * 24 // 24 horas
    private val refreshExpiration = 1000 * 60 * 15 // 15 minutos

    fun generateToken(userId: Int, roles: List<String>): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("userId", userId)
            .claim("roles", roles)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(userId: Int,roles: List<String>): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("userId", userId) // preguntar si esta bien pasar la info en el refresh
            .claim("roles", roles) // preguntar si esta bien pasar la info en el refresh
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(secretKey)
            .compact()
    }



        fun extractRoles(token: String): List<String> {
        val roles = getClaims(token).get("roles", List::class.java)
        return roles?.filterIsInstance<String>() ?: emptyList()
    }

    fun extractUserId(token: String): Int {
        return getClaims(token).get("userId", Integer::class.java).toInt()
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            getClaims(token) // si no lanza excepción, es válido
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String) = Jwts.parser()
        .verifyWith(secretKey)     // Valida que el token no fué alterado
        .build()
        .parseSignedClaims(token)  // Decodifica, verifica la firma, revisa expiración
        .payload
}