package io.github.e1turin.itmochan.security.service

import io.github.e1turin.itmochan.security.configuration.JWTProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenService(
    jwtProperties: JWTProperties
) {

    private val secretKey = Keys.hmacShaKeyFor(
        jwtProperties.key.toByteArray()
    )

    fun generate(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(secretKey)
            .compact()

    fun isValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return userDetails.username == username && !isExpired(token)
    }

    fun extractUsername(token: String): String? =
        getAllClaims(token)
            .subject
    fun isExpired(token: String): Boolean =
        getAllClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))
    private fun getAllClaims(token: String): Claims {
        val parser = Jwts.parser()
            .verifyWith(secretKey)
            .build()
        return parser
            .parseSignedClaims(token)
            .payload
    }
}
