package dao

import database.DatabaseManager
import models.AccessLog
import models.AccessCard
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Access Control Data Access Object
 * Location: src/main/kotlin/dao/AccessControlDAO.kt
 */
class AccessControlDAO {

    fun logAccess(accessLog: AccessLog): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO AccessLogs (member_id, device_id, timestamp, entry_type) VALUES (?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, accessLog.memberId)
            stmt.setLong(2, accessLog.deviceId)
            stmt.setTimestamp(3, Timestamp.valueOf(accessLog.timestamp))
            stmt.setString(4, accessLog.entryType)

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getAccessLogsByMember(memberId: Long, fromDate: LocalDateTime, toDate: LocalDateTime): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT al.*, sd.device_name, l.location_name
            FROM AccessLogs al 
            JOIN SecurityDevices sd ON al.device_id = sd.device_id 
            JOIN Locations l ON sd.location_id = l.location_id
            WHERE al.member_id = ? AND al.timestamp BETWEEN ? AND ?
            ORDER BY al.timestamp DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            stmt.setTimestamp(2, Timestamp.valueOf(fromDate))
            stmt.setTimestamp(3, Timestamp.valueOf(toDate))
            val rs = stmt.executeQuery()

            val logs = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                logs.add(mapOf(
                    "log_id" to rs.getLong("log_id"),
                    "member_id" to rs.getLong("member_id"),
                    "device_name" to rs.getString("device_name"),
                    "location_name" to rs.getString("location_name"),
                    "timestamp" to rs.getTimestamp("timestamp").toLocalDateTime(),
                    "entry_type" to rs.getString("entry_type")
                ))
            }
            logs
        }
    }

    fun createAccessCard(accessCard: AccessCard): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO AccessCard (member_id, access_type) VALUES (?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, accessCard.memberId)
            stmt.setLong(2, accessCard.accessType)

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getAccessCardByMember(memberId: Long): AccessCard? {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM AccessCard WHERE member_id = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            val rs = stmt.executeQuery()

            if (rs.next()) {
                AccessCard(
                    cardId = rs.getLong("card_id"),
                    memberId = rs.getLong("member_id"),
                    accessType = rs.getLong("access_type")
                )
            } else null
        }
    }

    fun getTodayAccessLogs(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT al.*, m.member_name, sd.device_name, l.location_name
            FROM AccessLogs al
            JOIN Members m ON al.member_id = m.member_id
            JOIN SecurityDevices sd ON al.device_id = sd.device_id
            JOIN Locations l ON sd.location_id = l.location_id
            WHERE DATE(al.timestamp) = CURDATE()
            ORDER BY al.timestamp DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val logs = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                logs.add(mapOf(
                    "log_id" to rs.getLong("log_id"),
                    "member_name" to rs.getString("member_name"),
                    "device_name" to rs.getString("device_name"),
                    "location_name" to rs.getString("location_name"),
                    "timestamp" to rs.getTimestamp("timestamp").toLocalDateTime(),
                    "entry_type" to rs.getString("entry_type")
                ))
            }
            logs
        }
    }

    fun getAccessStatsByLocation(): Map<String, Any> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT l.location_name,
                   COUNT(CASE WHEN al.entry_type = 'entry' THEN 1 END) as total_entries,
                   COUNT(CASE WHEN al.entry_type = 'exit' THEN 1 END) as total_exits,
                   COUNT(DISTINCT al.member_id) as unique_members
            FROM Locations l
            LEFT JOIN SecurityDevices sd ON l.location_id = sd.location_id
            LEFT JOIN AccessLogs al ON sd.device_id = al.device_id
            WHERE DATE(al.timestamp) = CURDATE() OR al.timestamp IS NULL
            GROUP BY l.location_id, l.location_name
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val locationStats = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                locationStats.add(mapOf(
                    "location_name" to rs.getString("location_name"),
                    "total_entries" to rs.getInt("total_entries"),
                    "total_exits" to rs.getInt("total_exits"),
                    "unique_members" to rs.getInt("unique_members")
                ))
            }
            mapOf("location_stats" to locationStats)
        }
    }
}