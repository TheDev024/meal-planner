package entity

data class Meal(
    val category: String, val name: String, val ingredients: List<String>
) {
    override fun toString(): String =
        "Category: ${this.category}\nName: ${this.name}\nIngredients:${this.ingredients.joinToString("\n", "\n")}"
}
