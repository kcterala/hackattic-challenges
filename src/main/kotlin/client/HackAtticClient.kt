package dev.kcterala.client

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HackAtticClient {
    val BASE_URL = "https://hackattic.com/challenges/"
    val TOKEN = System.getenv("HACK_TOKEN")
    val client = HttpClient.newHttpClient()

    fun getProblemData(challengeName: String) : String {
        val url = BASE_URL + challengeName + "/problem?access_token=$TOKEN"
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(url))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun postSolution(challengeName: String, challengeData: String) : String {
        val url = BASE_URL + challengeName + "/solve?access_token=$TOKEN"
        val request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(challengeData))
            .uri(URI.create(url))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

}