import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object EntitiesGenerator {

    private const val INPUT_FILE_PATH = "./data/$CURRENT_VERSION/entities.json"
    private const val OUTPUT_FILE_PATH = "./out/Entities.kt"

    private val json = Json { ignoreUnknownKeys = true }
    private val input = File(INPUT_FILE_PATH).readText()

    fun generate() {
        val items = parseEntities(input)

        val classContent = buildClassContent(items)
        writeFile(OUTPUT_FILE_PATH, classContent)
        println("Generated Entities")
    }

    private fun parseEntities(jsonInput: String): List<EntityType> {
        return json.decodeFromString(jsonInput)
    }

    private fun buildClassContent(blocks: List<EntityType>): String {
        return buildString {
            appendLine("package io.github.dockyardmc.registry")
            appendImports()
            appendMetadata()
            appendLine("object Entities {")
            appendLine("    private val idToEntityMap by lazy {")
            appendLine("        val json = Json { ignoreUnknownKeys = true }")
            appendLine("        val entities = json.decodeFromString<List<Entity>>(Resources.getText(\"./data/entities.json\"))")
            appendLine("        entities.associateBy { it.id }")
            appendLine("    }")
            appendLine("    fun getEntityById(id: Int): Entity {")
            appendLine("        return idToEntityMap[id] ?: error(\"Entity ID \$id not found\")")
            appendLine("    }")
            blocks.forEach {
                appendLine("    val ${it.namespace.toUpperSnakeCase()} = getEntityById(${it.id})")
            }
            appendLine("}")
            appendLine(serializeEntityDataClasses())
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

    private fun serializeEntityDataClasses(): String {
        return """
            @Serializable
            data class EntityType(
                val id: Int,
                @SerialName("displayName")
                val name: String,
                @SerialName("name")
                val namespace: String,
                val width: Float,
                val height: Float,
                val type: String,
                val category: String
            )
        """.trimIndent()
    }

    private fun writeFile(path: String, content: String) {
        val outFile = File(path)
        outFile.createNewFile()
        outFile.writeText(content)
    }

    @Serializable
    data class EntityType(
        val id: Int,
        @SerialName("displayName")
        val name: String,
        @SerialName("name")
        val namespace: String,
        val width: Float,
        val height: Float,
        val type: String,
        val category: String
    )
}