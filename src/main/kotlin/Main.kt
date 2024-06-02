
const val CURRENT_VERSION = "1.20.4"

fun main(args: Array<String>) {
    println("Generating registry classes..")
    BlocksGenerator.generate()
    BiomesGenerator.generate()
}

