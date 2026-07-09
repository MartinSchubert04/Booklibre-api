package com.BookLibre.controller

import com.BookLibre.dto.BookCardDTO
import com.BookLibre.service.HomeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = ["*"])
class HomeController(private val homeService: HomeService) {

    @GetMapping("/popular")
    fun get(@RequestParam(required = false) userId: Int?): ResponseEntity<List<BookCardDTO>> {
        return ResponseEntity.ok(homeService.getHomeData(userId))
    }
}