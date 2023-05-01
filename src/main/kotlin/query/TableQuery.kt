package query

data class TableQuery(
    private val name: String,
    private val columns: List<String>
) {
    override fun toString(): String = "CREATE TABLE IF NOT EXISTS $name" +
            columns.joinToString(",\n", "(\n", "\n)")
}
