package challenges.brute_force_zip

import challenges.backup_restore.objectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kcterala.client.ErrorResponse
import dev.kcterala.client.HackAtticUtils
import dev.kcterala.client.SuccessResponse
import enums.CHALLENGE
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.FileHeader
import java.io.*


data class ProblemData(
    val zip_url: String
)

data class SolutionData(
    val secret: String
)


fun main() {
    /*
        1. We use known plain-text attack to get the files
        2. https://github.com/kimci86/bkcrack - Use this tool to check the contents and to make the attack
     */

    val problemDataResponse = HackAtticUtils.getProblemData(CHALLENGE.BRUTE_FORCE_ZIP)
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

    val fileDownloadResponse = HackAtticUtils.getFile(problemData.zip_url)
    val fileName: String
    when (fileDownloadResponse) {
        is SuccessResponse -> {
            fileName = fileDownloadResponse.data
        }

        is ErrorResponse -> {
            println(fileDownloadResponse.errorMessage)
            return
        }
    }

    val file = getFileFromResources("unprotected.zip")
    val getKeysCommand = listOf("bkcrack", "-C", fileName, "-c", "dunwich_horror.txt", "-P", file?.absolutePath, "-p", "dunwich_horror.txt")
    val process = ProcessBuilder().command(getKeysCommand).start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val output = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        output.append(line).append('\n')
    }
    reader.close()

    if (!output.contains("Found a solution")) {
        println(output)
        println("Cannot crack the password")
        return
    } else {
        println("Cracked the internal keys")
    }

    val strings = output.split("\n")
    val keys = strings.get(strings.size - 2).split(" ")


    val removePasswordForZipFileCommand = listOf("bkcrack", "-C", fileName, "-c", "secret.txt",  "-k", keys[0], keys[1], keys[2], "-D", "file_without_password.zip")
    ProcessBuilder().command(removePasswordForZipFileCommand).start()

    val secret = getSecretFromZipFile("file_without_password.zip", "secret.txt")
    val postResponse = HackAtticUtils.postSolution(CHALLENGE.BRUTE_FORCE_ZIP, jacksonObjectMapper().writeValueAsString(SolutionData(secret)))
    println(postResponse)
    deleteFile("file_without_password.zip")
    deleteFile("file.zip")
}

fun getSecretFromZipFile(zipFilePath: String, entryName: String): String {
    val zipFile = ZipFile(zipFilePath)

    val fileHeader: FileHeader? = zipFile.getFileHeader(entryName)
    if (fileHeader != null) {
        zipFile.getInputStream(fileHeader).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                return reader.readLine()
            }
        }
    } else {
        println("File not found: $entryName")
        return "null"
    }
}

fun deleteFile(filePath: String): Boolean {
    val file = File(filePath)
    if (file.exists()) {
        return file.delete()
    } else {
        println("File does not exist: $filePath")
        return false
    }
}

fun getFileFromResources(fileName: String): File? {
    return object {}.javaClass.classLoader.getResource(fileName)?.let { url ->
        File(url.file)
    }
}
