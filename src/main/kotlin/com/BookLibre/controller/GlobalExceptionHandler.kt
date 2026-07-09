package com.BookLibre.controller

import com.BookLibre.error.ApiError
import com.BookLibre.error.BusinessException
import com.BookLibre.error.NotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(
        ex: BusinessException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI
        log.warn("Business error at {} {}: {}", request.method, path, ex.message)

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "BUSINESS_ERROR",
            message = ex.message ?: "Error de negocio",
            path = path
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI
        log.warn("Resource not found at {} {}: {}", request.method, path, ex.message)

        val error = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            error = "NOT_FOUND",
            message = ex.message ?: "Recurso no encontrado",
            path = path
        )
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI

        val fieldErrors = ex.bindingResult.fieldErrors
            .distinctBy { it.field }
            .map {
                ApiError.FieldError(
                    field = it.field,
                    message = it.defaultMessage ?: "valor inválido"
                )
            }

        log.debug("Validation failed at {} {}: {}", request.method, path, fieldErrors)

        val message = fieldErrors.joinToString(", ") { it.message }
            .ifEmpty { "Validación fallida" }

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "VALIDATION_ERROR",
            message = message,
            path = path,
            fieldErrors = fieldErrors
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidFormat(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI
        log.debug("Malformed JSON at {} {}: {}", request.method, path, ex.mostSpecificCause.message)

        val error = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "INVALID_JSON",
            message = "Formato de datos inválido",
            path = path
        )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI
        log.warn("Access denied at {} {}: {}", request.method, path, ex.message)

        val error = ApiError(
            status = HttpStatus.FORBIDDEN.value(),
            error = "ACCESS_DENIED",
            message = "No tenés permisos para acceder a este recurso",
            path = path
        )
        return ResponseEntity(error, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val path = request.requestURI
        log.error("Unhandled exception at {} {}", request.method, path, ex)

        val error = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "INTERNAL_ERROR",
            message = "Error interno del servidor",
            path = path
        )
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
