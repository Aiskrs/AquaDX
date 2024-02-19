package icu.samnyan.aqua.net.utils

import ext.Str
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

class ApiException(val code: Int, message: Str) : RuntimeException(message)

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleCustomApiException(e: ApiException): ResponseEntity<Any?> {
        // On error, return the error code and message
        return ResponseEntity.status(e.code).body(e.message)
    }
}