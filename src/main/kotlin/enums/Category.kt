package enums

enum class Category {
    BREAKFAST, LUNCH, DINNER;

    override fun toString(): String = this.name.lowercase()
}
