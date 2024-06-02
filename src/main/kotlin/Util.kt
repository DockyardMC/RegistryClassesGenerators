fun String.toUpperSnakeCase(): String {
    val value = this
    var isFirst = true
    val out = buildString {
        value.forEachIndexed { index, c ->
            if(!isFirst && c.isUpperCase() && value[index - 1].isLowerCase()) {
                append("_")
            }
            append(c.uppercase())
            isFirst = false
        }
    }
    return out.replace(" ", "_")
}