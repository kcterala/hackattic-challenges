package challenges.backup_restore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kcterala.client.ErrorResponse
import dev.kcterala.client.HackAtticUtils
import dev.kcterala.client.SuccessResponse
import enums.CHALLENGE
import java.io.*
import java.sql.DriverManager
import java.util.Base64
import java.util.zip.GZIPInputStream


val objectMapper = jacksonObjectMapper()

data class ProblemData(
    val dump: String
)

data class SolutionData(
    val alive_ssns: List<String>
)

fun main() {
    val problemDataResponse = HackAtticUtils.getProblemData(CHALLENGE.BACKUP_RESTORE)
    val problemData: ProblemData
    when (problemDataResponse) {
        is ErrorResponse -> {
            println(problemDataResponse.errorMessage)
            return
        }

        is SuccessResponse -> {
            problemData = objectMapper.readValue(problemDataResponse.data)
        }
    }


    val solutionData = SolutionData(getListOfSSN(problemData))
    val postResponse = HackAtticUtils.postSolution(CHALLENGE.BACKUP_RESTORE, objectMapper.writeValueAsString(solutionData))
    println(postResponse)

}

private fun getListOfSSN(problemData: ProblemData): List<String> {
    val dumpBytes = Base64.getDecoder().decode(problemData.dump)
    val fileName = "query.sql"
    val file = File(fileName)

    // Normally pg_dump files are gzip compressed, so uncompress and write the bytes into the file
    GZIPInputStream(ByteArrayInputStream(dumpBytes)).use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }


    val filePath = file.absolutePath
    val copySqlFileCommand = listOf("docker", "cp", filePath, "db:/$fileName")
    val executeSqlFileCommand = listOf(
        "docker", "exec",
        "db",
        "psql", "-U", "postgres", "-d", "postgres", "-f", "/$fileName"
    )
    ProcessBuilder(copySqlFileCommand).redirectErrorStream(true).start()
    ProcessBuilder(executeSqlFileCommand).redirectErrorStream(true).start()


    val resultList = mutableListOf<String>()
    DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres").use { connection ->
        val query = "SELECT SSN FROM public.criminal_records WHERE status = 'alive'"
        connection.createStatement().use { statement ->
            val resultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                resultList.add(resultSet.getString(1))
            }
        }

        println("Script executed successfully.")
    }

    return resultList
}

