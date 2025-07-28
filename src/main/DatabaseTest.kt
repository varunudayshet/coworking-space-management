import java.sql.Connection
import java.sql.DriverManager

class DatabaseTest {
    fun connect() {
        val url = "jdbc:mysql://localhost:3306/coworking_space" // ✅ Fixed: removed underscore
        val user = "root"
        val password = "varun_382900"

        try {
            val connection: Connection = DriverManager.getConnection(url, user, password)
            println("✅ Connected to the database!")
            connection.close()
        } catch (e: Exception) {
            println("❌ Failed to connect:")
            e.printStackTrace()
        }
    }
}

fun main() {
    val db = DatabaseTest()
    db.connect()
}