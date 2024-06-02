import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object BiomesGenerator {

    private const val INPUT_FILE_PATH = "./data/$CURRENT_VERSION/biomes.json"
    private const val OUTPUT_FILE_PATH = "./out/Biomes.kt"

    private val json = Json { ignoreUnknownKeys = true }
    private val input = File(INPUT_FILE_PATH).readText()

    fun generate() {
        val biomes = parseBiomes(input)
        val classContent = buildClassContent(biomes.biomeList.value)
        writeFile(OUTPUT_FILE_PATH, classContent)
        println("Generated Biomes")
    }

    private fun parseBiomes(jsonInput: String): BiomesSerializable {
        return json.decodeFromString(jsonInput)
    }

    private fun buildClassContent(biomes: List<Biome>): String {
        return buildString {
            appendLine("package io.github.dockyardmc.registry")
            appendImports()
            appendMetadata()
            appendLine("object Biomes {")
            appendLine("    private val idToBiomeMap by lazy {")
            appendLine("        val json = Json { ignoreUnknownKeys = true }")
            appendLine("        val biomes = json.decodeFromString<BiomesSerializable>(Resources.getText(\"./data/biomes.json\")).biomeList.value")
            appendLine("        biomes.associateBy { it.id }")
            appendLine("    }")
            appendLine("    fun getBiomeById(id: Int): Biome {")
            appendLine("        return idToBiomeMap[id] ?: error(\"Biome ID \$id not found\")")
            appendLine("    }")
            biomes.forEach {
                appendLine("    val ${it.name.replace("minecraft:", "").replace(" ", "_").uppercase()} = getBiomeById(${it.id})")
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
            data class BiomesSerializable(
                @SerialName("minecraft:worldgen/biome")
                val biomeList: BiomeObjects
            )
            @Serializable
            data class BiomeObjects(
                val type: String,
                val value: MutableList<Biome>
            )
        
            @Serializable
            data class Biome(
                val name: String,
                val id: Int
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
    data class BiomesSerializable(
        @SerialName("minecraft:worldgen/biome")
        val biomeList: BiomeObjects
    )
    @Serializable
    data class BiomeObjects(
        val type: String,
        val value: MutableList<Biome>
    )

    @Serializable
    data class Biome(
        val name: String,
        val id: Int
    )
}