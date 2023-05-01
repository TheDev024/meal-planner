package query

data class SelectQuery(
    private val table: String,
    private val columns: List<String> = listOf("*"),
    private val aggregates: List<String> = emptyList(),
    private val join: List<String> = emptyList(),
    private val where: String = "",
    private val orderBy: List<Any> = emptyList(),
    private val groupBy: List<String> = emptyList(),
    private val having: List<String> = emptyList(),
    private val set: String = "",
    private val secondQuery: SelectQuery? = null
) {
    override fun toString(): String =
        "SELECT ${if (columns.isEmpty() && aggregates.isEmpty()) "*" else (columns + aggregates).joinToString(", ")}" +
                "\nFROM $table" +
                (if (join.isEmpty()) "" else join.joinToString("\n", "\n") { "JOIN $it" }) +
                (if (where.isBlank()) "" else "\nWHERE $where") +
                (if (groupBy.isEmpty()) "" else {
                    "\nGROUP BY " + groupBy.joinToString(", ") + if (having.isEmpty()) "" else "\nHAVING ${
                        having.joinToString(
                            ",\n"
                        )
                    }"
                }) +
                if (set.isBlank()) "" else "\n$set\n$secondQuery"
}
