
const val CURRENT_VERSION = "1.21"

fun main(args: Array<String>) {
    println("Generating registry classes..")
    BlocksGenerator.generate()
    BiomesGenerator.generate()
    ItemsGenerator.generate()
    ParticlesGenerator.generate()
    EntitiesGenerator.generate()
}

