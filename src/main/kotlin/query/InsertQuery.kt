package query

data class InsertQuery(
    private val table: String,
    private val values: List<List<Any>>,
    private val columns: List<String> = emptyList()
) {
    override fun toString(): String = "INSERT INTO $table" +
            "${
                if (columns.isEmpty()) "" else columns.joinToString(
                    ",", " (", ")"
                )
            } VALUES " +
            values.joinToString(
                ",\n",
                "\n",
                "\n"
            ) {
                it.joinToString(
                    ", ",
                    "(",
                    ")"
                ) { value -> if (value is String) "'$value'" else value.toString() }
            }
}