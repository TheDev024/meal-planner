package entity

data class Meal(
    val meal: String,
    val category: String,
    val ingredients: List<String>
) {
    override fun toString(): String = "Name: $meal\n" +
            "Ingredients: ${ingredients.joinToString("\n", "\n")}"
}
