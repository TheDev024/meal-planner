import query.InsertQuery
import query.SelectQuery
import query.TableQuery
import java.sql.*

class DataBaseManager(private val dataBaseFile: String) {
    private lateinit var connection: Connection
    private lateinit var statement: Statement

    fun open() {
        connection = DriverManager.getConnection("jdbc:sqlite:$dataBaseFile")
        statement = connection.createStatement()
    }

    fun createTable(query: TableQuery) = statement.executeUpdate(query.toString())

    fun insert(query: InsertQuery): Int = statement.executeUpdate(query.toString())

    fun selectAll(table: String): ResultSet = statement.executeQuery("SELECT * FROM $table")

    fun select(query: SelectQuery): ResultSet = statement.executeQuery(query.toString())

    fun close() {
        try {
            statement.close()
        } catch (e: SQLException) {
            throw SQLException()
        }
    }
}