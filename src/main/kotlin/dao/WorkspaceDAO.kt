package dao

import database.DatabaseManager
import models.Workspace
import models.WorkspaceBooking
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Workspace Data Access Object
 * Location: src/main/kotlin/dao/WorkspaceDAO.kt
 */
class WorkspaceDAO {

    fun getAvailableWorkspaces(locationId: Long): List<Workspace> {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Workspaces WHERE location_id = ? AND occupied = 'not_occupied'"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, locationId)
            val rs = stmt.executeQuery()

            val workspaces = mutableListOf<Workspace>()
            while (rs.next()) {
                workspaces.add(Workspace(
                    workspaceId = rs.getLong("workspace_id"),
                    workspaceType = rs.getLong("workspace_type"),
                    locationId = rs.getLong("location_id"),
                    occupied = rs.getString("occupied"),
                    workspaceAreaSqft = rs.getLong("workspace_area_sqft")
                ))
            }
            workspaces
        }
    }

    fun updateWorkspaceStatus(workspaceId: Long, status: String): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = "UPDATE Workspaces SET occupied = ? WHERE workspace_id = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, status)
            stmt.setLong(2, workspaceId)
            stmt.executeUpdate() > 0
        }
    }

    fun createWorkspaceBooking(booking: WorkspaceBooking): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO WorkspaceBookings (member_id, workspace_id, start_time, end_time, created_at, invoice_id) VALUES (?, ?, ?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, booking.memberId)
            stmt.setLong(2, booking.workspaceId)
            stmt.setTimestamp(3, Timestamp.valueOf(booking.startTime))
            stmt.setTimestamp(4, Timestamp.valueOf(booking.endTime))
            stmt.setTimestamp(5, Timestamp.valueOf(booking.createdAt))
            if (booking.invoiceId != null) {
                stmt.setLong(6, booking.invoiceId)
            } else {
                stmt.setNull(6, java.sql.Types.BIGINT)
            }

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getWorkspaceUtilizationByLocation(): Map<String, Any> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT l.location_name,
                   COUNT(w.workspace_id) as total_workspaces,
                   COUNT(CASE WHEN w.occupied = 'occupied' THEN 1 END) as occupied_workspaces,
                   COUNT(CASE WHEN w.occupied = 'not_occupied' THEN 1 END) as available_workspaces,
                   COUNT(CASE WHEN w.occupied = 'under_maintenance' THEN 1 END) as maintenance_workspaces,
                   (COUNT(CASE WHEN w.occupied = 'occupied' THEN 1 END) * 100.0 / COUNT(w.workspace_id)) as utilization_rate
            FROM Locations l
            LEFT JOIN Workspaces w ON l.location_id = w.location_id
            GROUP BY l.location_id, l.location_name
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val utilization = mutableMapOf<String, Any>()
            val locationData = mutableListOf<Map<String, Any>>()

            while (rs.next()) {
                locationData.add(mapOf(
                    "location_name" to rs.getString("location_name"),
                    "total_workspaces" to rs.getInt("total_workspaces"),
                    "occupied_workspaces" to rs.getInt("occupied_workspaces"),
                    "available_workspaces" to rs.getInt("available_workspaces"),
                    "maintenance_workspaces" to rs.getInt("maintenance_workspaces"),
                    "utilization_rate" to rs.getDouble("utilization_rate")
                ))
            }

            utilization["locations"] = locationData
            utilization
        }
    }

    fun getWorkspacesByMembershipType(membershipTypeId: Long): List<Workspace> {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Workspaces WHERE workspace_type = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, membershipTypeId)
            val rs = stmt.executeQuery()

            val workspaces = mutableListOf<Workspace>()
            while (rs.next()) {
                workspaces.add(Workspace(
                    workspaceId = rs.getLong("workspace_id"),
                    workspaceType = rs.getLong("workspace_type"),
                    locationId = rs.getLong("location_id"),
                    occupied = rs.getString("occupied"),
                    workspaceAreaSqft = rs.getLong("workspace_area_sqft")
                ))
            }
            workspaces
        }
    }

    fun checkWorkspaceAvailability(workspaceId: Long, startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT COUNT(*) as conflicts
            FROM WorkspaceBookings 
            WHERE workspace_id = ? 
            AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, workspaceId)
            stmt.setTimestamp(2, Timestamp.valueOf(startTime))
            stmt.setTimestamp(3, Timestamp.valueOf(startTime))
            stmt.setTimestamp(4, Timestamp.valueOf(endTime))
            stmt.setTimestamp(5, Timestamp.valueOf(endTime))

            val rs = stmt.executeQuery()
            rs.next() && rs.getInt("conflicts") == 0
        }
    }
}