import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File

object ItemsGenerator {

    private const val INPUT_FILE_PATH = "./data/$CURRENT_VERSION/items.json"
    private const val OUTPUT_FILE_PATH = "./out/Items.kt"

    private val json = Json { ignoreUnknownKeys = true }
    private val input = File(INPUT_FILE_PATH).readText()

    fun generate() {
        val items = parseItems(input)
        val blocks = BlocksGenerator.parseBlocks(BlocksGenerator.input)
        items.forEach { item ->
            if(blocks.firstOrNull { it.namespace == item.namespace } != null) {
                val block = blocks.first { it.namespace == item.namespace }
                item.blockId = block.blockStateId
                item.isBlock = true
            }
        }

        val classContent = buildClassContent(items)
        writeFile(OUTPUT_FILE_PATH, classContent)
        println("Generated Items")
    }

    private fun parseItems(jsonInput: String): List<Item> {
        return json.decodeFromString(jsonInput)
    }

    private fun buildClassContent(blocks: List<Item>): String {
        return buildString {
            appendLine("package io.github.dockyardmc.registry")
            appendImports()
            appendMetadata()
            appendLine("object Items {")
            appendLine("    val idToItemMap by lazy {")
            appendLine("        val json = Json { ignoreUnknownKeys = true }")
            appendLine("        val items = json.decodeFromString<List<Item>>(Resources.getText(\"./data/items.json\"))")
            appendLine("        items.forEach { item ->")
            appendLine("            val block = Blocks.idToBlockMap.values.firstOrNull { item.namespace == it.namespace } ?: return@forEach")
            appendLine("            item.isBlock = true")
            appendLine("            item.blockId = block.blockStateId")
            appendLine("        }")
            appendLine("        items.associateBy { it.id }")
            appendLine("    }")
            appendLine("    fun getItemById(id: Int): Item {")
            appendLine("        return idToItemMap[id] ?: error(\"Item ID \$id not found\")")
            appendLine("    }")
            blocks.forEach {
                appendLine("    val ${it.namespace.toUpperSnakeCase()} = getItemById(${it.id})")
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
        appendLine("import kotlinx.serialization.Transient")
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
            data class Item(
                val id: Int,
                @SerialName("displayName")
                val name: String,
                @SerialName("name")
                val namespace: String,
                val stackSize: Int,
                @Transient
                var isBlock: Boolean = false,
                @Transient
                var blockId: Int? = null
            )
        """.trimIndent()
    }

    private fun writeFile(path: String, content: String) {
        val outFile = File(path)
        outFile.createNewFile()
        outFile.writeText(content)
    }

    @Serializable
    data class Item(
        val id: Int,
        @SerialName("displayName")
        val name: String,
        @SerialName("name")
        val namespace: String,
        val stackSize: Int,
        @Transient
        var isBlock: Boolean = false,
        @Transient
        var blockId: Int? = null
    )
}