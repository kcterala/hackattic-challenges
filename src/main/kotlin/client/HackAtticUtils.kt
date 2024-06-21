package dev.kcterala.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import enums.CHALLENGE
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

sealed class ApiResponse<out T>

data class SuccessResponse<T>(val data: T) : ApiResponse<T>()

data class ErrorResponse(val errorMessage: String) : ApiResponse<Nothing>()


object HackAtticUtils {
    val BASE_URL = "https://hackattic.com/challenges/"
    val TOKEN = System.getenv("HACK_TOKEN")
    val client = HttpClient.newHttpClient()

    fun getProblemData(challenge: CHALLENGE) : ApiResponse<String> {
        val url = "$BASE_URL${challenge.value}/problem?access_token=$TOKEN"
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            return ErrorResponse(response.body())
        }
        return SuccessResponse(response.body())
    }

    fun postSolution(challenge: CHALLENGE, challengeData: String) : ApiResponse<String> {
        val url = "$BASE_URL${challenge.value}/solve?access_token=$TOKEN&playground=1"
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(challengeData))
            .uri(URI.create(url))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            return ErrorResponse(response.body())
        }
        return SuccessResponse(response.body())
    }

}