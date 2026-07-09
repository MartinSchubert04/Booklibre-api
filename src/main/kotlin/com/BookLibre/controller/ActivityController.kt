package com.BookLibre.controller

import com.BookLibre.service.ActivitySseService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/activity")
class ActivityController(private val activitySseService: ActivitySseService) {

    @GetMapping("/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        activitySseService.addEmitter(emitter)
        return emitter
    }
}
