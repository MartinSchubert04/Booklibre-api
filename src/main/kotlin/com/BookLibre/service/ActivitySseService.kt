package com.BookLibre.service

import com.BookLibre.dto.ActivityEventDTO
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Service
class ActivitySseService {

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun addEmitter(emitter: SseEmitter) {
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
    }

    fun broadcast(event: ActivityEventDTO) {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(SseEmitter.event().data(event, MediaType.APPLICATION_JSON))
            } catch (e: Exception) {
                dead.add(emitter)
            }
        }
        emitters.removeAll(dead.toSet())
    }
}
