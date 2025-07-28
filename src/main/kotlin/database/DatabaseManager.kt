package database

import java.sql.Connection
import java.sql.DriverManager

/**
 * Database Connection Manager for Coworking Space Management System
 * Location: src/main/kotlin/database/DatabaseManager.kt
 */
class DatabaseManager {
    companion object {
        private const val URL = "jdbc:mysql://localhost:3306/coworking_space"
        private const val USER = "root"
        private const val PASSWORD = "varun_382900"

        fun getConnection(): Connection {
            return DriverManager.getConnection(URL, USER, PASSWORD)
        }

        fun closeConnection(connection: Connection) {
            try {
                if (!connection.isClosed) {
                    connection.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun testConnection(): Boolean {
            return try {
                val connection = getConnection()
                val isValid = !connection.isClosed
                closeConnection(connection)
                isValid
            } catch (e: Exception) {
                println("Database connection failed: ${e.message}")
                false
            }
        }
    }
}