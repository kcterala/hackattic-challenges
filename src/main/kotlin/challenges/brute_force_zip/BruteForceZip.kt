package challenges.brute_force_zip

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kcterala.client.ErrorResponse
import dev.kcterala.client.HackAtticUtils
import dev.kcterala.client.SuccessResponse
import enums.CHALLENGE


data class ProblemData(
    val zip_url: String
)

data class SolutionData(
    val secret: String
)


fun main() {
    val problemDataResponse = HackAtticUtils.getProblemData(CHALLENGE.BRUTE_FORCE_ZIP)
    val problemData: ProblemData
    when (problemDataResponse) {
        is ErrorResponse -> {
            println(problemDataResponse.errorMessage)
            return
        }

        is SuccessResponse -> {
            problemData = challenges.backup_restore.objectMapper.readValue(problemDataResponse.data)
        }
    }



    val postResponse = HackAtticUtils.postSolution(CHALLENGE.BRUTE_FORCE_ZIP, jacksonObjectMapper().writeValueAsString(null))
    println(postResponse)

}



