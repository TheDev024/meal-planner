fun getInput(
    prompt: String,
    validFormat: Regex,
    caseSensitive: Boolean = false,
    errorMessage: String = "Invalid option"
): String {
    println(prompt)
    val input = scanner.nextLine()
    return if (input.isNotBlank() && input.matches(validFormat)) {
        if (caseSensitive) input else input.lowercase()
    } else {
        println(errorMessage)
        getInput(prompt, validFormat, caseSensitive, errorMessage)
    }
}

fun getInput(
    prompt: String,
    validInputs: List<String>? = null,
    caseSensitive: Boolean = false,
    errorMessage: String = "Invalid option"
): String {
    println(prompt)
    val input = scanner.nextLine()
    return if (validInputs == null || input in if (caseSensitive) validInputs else validInputs.map { it.lowercase() }) {
        if (caseSensitive) input else input.lowercase()
    } else {
        println(errorMessage)
        getInput(prompt, validInputs, caseSensitive, errorMessage)
    }
}

fun String.toCapitalCase(): String = this.replaceFirstChar { it.uppercase() }
