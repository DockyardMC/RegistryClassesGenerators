import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object ParticlesGenerator {

    private const val INPUT_FILE_PATH = "./data/$CURRENT_VERSION/particles.json"
    private const val OUTPUT_FILE_PATH = "./out/Particles.kt"

    private val json = Json { ignoreUnknownKeys = true }
    private val input = File(INPUT_FILE_PATH).readText()

    fun generate() {
        val particles = parseParticles(input)
        val classContent = buildClassContent(particles)
        writeFile(OUTPUT_FILE_PATH, classContent)
        println("Generated Particles")
    }

    private fun parseParticles(jsonInput: String): MutableList<Particle> {
        return json.decodeFromString(jsonInput)
    }

    private fun buildClassContent(particles: MutableList<Particle>): String {
        return buildString {
            appendLine("package io.github.dockyardmc.registry")
            appendImports()
            appendMetadata()
            appendLine("object Particles {")
            appendLine("    private val idToParticleMap by lazy {")
            appendLine("        val json = Json { ignoreUnknownKeys = true }")
            appendLine("        val particles = json.decodeFromString<MutableList<Particle>>(Resources.getText(\"./data/particles.json\"))")
            appendLine("        particles.associateBy { it.id }")
            appendLine("    }")
            appendLine("    fun getParticleById(id: Int): Particle {")
            appendLine("        return idToParticleMap[id] ?: error(\"Particle ID \$id not found\")")
            appendLine("    }")
            particles.forEach {
                appendLine("    val ${it.namespace.replace(" ", "_").uppercase()} = getParticleById(${it.id})")
            }
            appendLine("}")
            appendLine(serializeBiomeDataClass())
        }
    }

    private fun StringBuilder.appendImports() {
        appendLine("import io.github.dockyardmc.utils.Resources")
        appendLine("import kotlinx.serialization.decodeFromString")
        appendLine("import kotlinx.serialization.json.Json")
        appendLine("import kotlinx.serialization.SerialName")
        appendLine("import kotlinx.serialization.Serializable")
    }

    private fun StringBuilder.appendMetadata() {
        appendLine("// THIS CLASS IS AUTO-GENERATED")
        appendLine("// DATA FROM MINECRAFT $CURRENT_VERSION")
        appendLine("// https://github.com/DockyardMC/RegistryClassesGenerators")
        appendLine()
    }

    private fun serializeBiomeDataClass(): String {
        return """
            @Serializable
            data class Particle(
                val id: Int,
                val namespace: String
            )
        """.trimIndent()
    }

    private fun writeFile(path: String, content: String) {
        val outFile = File(path)
        outFile.createNewFile()
        outFile.writeText(content)
    }

    private fun String.toUpperSnakeCase(): String {
        return this.split("(?=[A-Z])".toRegex()).joinToString("_") { it.toUpperCase() }
    }

    @Serializable
    data class Particle(
        val id: Int,
        @SerialName("name")
        val namespace: String
    )
}