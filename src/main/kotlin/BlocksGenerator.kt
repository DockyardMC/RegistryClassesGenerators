import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object BlocksGenerator {

    private const val INPUT_FILE_PATH = "./data/$CURRENT_VERSION/blocks.json"
    private const val OUTPUT_FILE_PATH = "./out/Blocks.kt"

    private val json = Json { ignoreUnknownKeys = true }
    private val input = File(INPUT_FILE_PATH).readText()

    fun generate() {
        val blocks = parseBlocks(input)
        val classContent = buildClassContent(blocks)
        writeFile(OUTPUT_FILE_PATH, classContent)
        println("Generated Blocks")
    }

    private fun parseBlocks(jsonInput: String): List<Block> {
        return json.decodeFromString(jsonInput)
    }

    private fun buildClassContent(blocks: List<Block>): String {
        return buildString {
            appendLine("package io.github.dockyardmc.registry")
            appendImports()
            appendMetadata()
            appendLine("object Blocks {")
            appendLine("    private val idToBlockMap by lazy {")
            appendLine("        val json = Json { ignoreUnknownKeys = true }")
            appendLine("        val blocks = json.decodeFromString<List<Block>>(Resources.getText(\"./data/blocks.json\"))")
            appendLine("        blocks.associateBy { it.blockStateId }")
            appendLine("    }")
            appendLine("    fun getBlockById(id: Int): Block {")
            appendLine("        return idToBlockMap[id] ?: error(\"Block ID \$id not found\")")
            appendLine("    }")
            blocks.forEach {
                appendLine("    val ${it.namespace.toUpperSnakeCase()} = getBlockById(${it.blockStateId})")
            }
            appendLine("}")
            appendLine(serializeBlockDataClass())
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

    private fun serializeBlockDataClass(): String {
        return """
            @Serializable
            data class Block(
                @SerialName("defaultState")
                var blockStateId: Int,
                @SerialName("displayName")
                var name: String,
                @SerialName("name")
                var namespace: String,
                @SerialName("transparent")
                var isTransparent: Boolean,
                @SerialName("emitLight")
                var lightEmitted: Int,
                @SerialName("filterLight")
                var lightFiltered: Int,
                @SerialName("minStateId")
                var minState: Int,
                @SerialName("maxStateId")
                var maxState: Int
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
    data class Block(
        @SerialName("name")
        var namespace: String,
        @SerialName("displayName")
        var name: String,
        @SerialName("transparent")
        var isTransparent: Boolean,
        @SerialName("emitLight")
        var lightEmitted: Int,
        @SerialName("filterLight")
        var lightFiltered: Int,
        @SerialName("defaultState")
        var blockStateId: Int,
        @SerialName("minStateId")
        var minState: Int,
        @SerialName("maxStateId")
        var maxState: Int
    )
}