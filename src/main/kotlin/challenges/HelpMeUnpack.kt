package dev.kcterala.challenges

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kcterala.client.HackAtticClient
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64

val client = HackAtticClient()
val CHALLENGE_NAME = "help_me_unpack"
val objectMapper = jacksonObjectMapper()

data class ProblemData(
    val bytes: String
)

data class SolutionData(
    val int: Int,
    val uint: Long,
    val short: Short,
    val float: Float,
    val double: Double,
    val big_endian_double: Double
)



fun main() {
    val problemDataString = client.getProblemData(CHALLENGE_NAME)
    val problemData = objectMapper.readValue<ProblemData>(problemDataString)
    val solutionData = getDecodedData(problemData)
    val solutionDataString = objectMapper.writeValueAsString(solutionData)
    val response = client.postSolution(CHALLENGE_NAME, solutionDataString)
    println(response)
}


fun getDecodedData(data: ProblemData) : SolutionData {
    val decodedBytes = Base64.getDecoder().decode(data.bytes)
    val buffer = ByteBuffer.wrap(decodedBytes).order(ByteOrder.LITTLE_ENDIAN)

    val regularInt = buffer.int

    val unsignedInt = buffer.int.toLong() and 0xFFFFFFFFL

    val signedShortBytes = ByteArray(4)
    buffer.get(signedShortBytes)

    val signedShortValue = ByteBuffer.wrap(signedShortBytes).order(ByteOrder.LITTLE_ENDIAN).short

    val floatValue = buffer.float

    val doubleValue = buffer.double

    val doubleBigEndian = buffer.order(ByteOrder.BIG_ENDIAN).double

    return SolutionData(regularInt, unsignedInt, signedShortValue, floatValue, doubleValue, doubleBigEndian)
}