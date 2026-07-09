package com.BookLibre.controller

import com.BookLibre.domain.Reservation

import com.BookLibre.dto.ReservationRatingDTO
import com.BookLibre.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = ["*"])
class ReservationController(val reservationService: ReservationService) {

    @GetMapping("/getAll")
    fun getAll(): ResponseEntity<List<Reservation>> {
        return ResponseEntity.ok(reservationService.getAll())
    }

    @PostMapping("/{reservationId}/rate")
    fun rateReservation(@PathVariable reservationId: Int,
                        @RequestBody @Valid dto: ReservationRatingDTO): ResponseEntity<Void> {
        reservationService.rateReservation(reservationId, dto)
        return ResponseEntity.ok().build()
    }
}