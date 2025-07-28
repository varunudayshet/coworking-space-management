package dao

import database.DatabaseManager
import models.Booking
import java.sql.Statement
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDateTime

/**
 * Booking Data Access Object
 * Location: src/main/kotlin/dao/BookingDAO.kt
 */
class BookingDAO {

    fun createBooking(booking: Booking): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO Bookings (member_id, resource_type, resource_id, start_time, end_time, total_price, created_at, invoice_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, booking.memberId)
            stmt.setString(2, booking.resourceType)
            stmt.setLong(3, booking.resourceId)
            stmt.setTimestamp(4, Timestamp.valueOf(booking.startTime))
            stmt.setTimestamp(5, Timestamp.valueOf(booking.endTime))
            stmt.setLong(6, booking.totalPrice)
            stmt.setTimestamp(7, Timestamp.valueOf(booking.createdAt))
            if (booking.invoiceId != null) stmt.setLong(8, booking.invoiceId) else stmt.setNull(8, Types.BIGINT)

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun checkBookingConflict(resourceType: String, resourceId: Long, startTime: LocalDateTime, endTime: LocalDateTime): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT COUNT(*) as conflicts
            FROM Bookings 
            WHERE resource_type = ? AND resource_id = ? 
            AND ((start_time <= ? AND end_time > ?) OR (start_time < ? AND end_time >= ?))
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, resourceType)
            stmt.setLong(2, resourceId)
            stmt.setTimestamp(3, Timestamp.valueOf(startTime))
            stmt.setTimestamp(4, Timestamp.valueOf(startTime))
            stmt.setTimestamp(5, Timestamp.valueOf(endTime))
            stmt.setTimestamp(6, Timestamp.valueOf(endTime))

            val rs = stmt.executeQuery()
            rs.next() && rs.getInt("conflicts") > 0
        }
    }

    fun getMeetingRoomBookingPatterns(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT mr.meeting_room_no, l.location_name,
                   COUNT(b.booking_id) as total_bookings,
                   AVG(TIMESTAMPDIFF(HOUR, b.start_time, b.end_time)) as avg_duration_hours,
                   SUM(b.total_price) as total_revenue
            FROM Bookings b
            JOIN MeetingRooms mr ON b.resource_id = mr.meeting_room_id
            JOIN Locations l ON mr.location_id = l.location_id
            WHERE b.resource_type = 'meeting_room'
            GROUP BY mr.meeting_room_id, mr.meeting_room_no, l.location_name
            ORDER BY total_bookings DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val patterns = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                patterns.add(mapOf(
                    "meeting_room_no" to rs.getLong("meeting_room_no"),
                    "location_name" to rs.getString("location_name"),
                    "total_bookings" to rs.getInt("total_bookings"),
                    "avg_duration_hours" to rs.getDouble("avg_duration_hours"),
                    "total_revenue" to rs.getLong("total_revenue")
                ))
            }
            patterns
        }
    }

    fun getBookingsByMember(memberId: Long): List<Booking> {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Bookings WHERE member_id = ? ORDER BY created_at DESC"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            val rs = stmt.executeQuery()

            val bookings = mutableListOf<Booking>()
            while (rs.next()) {
                bookings.add(Booking(
                    bookingId = rs.getLong("booking_id"),
                    memberId = rs.getLong("member_id"),
                    resourceType = rs.getString("resource_type"),
                    resourceId = rs.getLong("resource_id"),
                    startTime = rs.getTimestamp("start_time").toLocalDateTime(),
                    endTime = rs.getTimestamp("end_time").toLocalDateTime(),
                    totalPrice = rs.getLong("total_price"),
                    createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                    invoiceId = rs.getLong("invoice_id").takeIf { !rs.wasNull() }
                ))
            }
            bookings
        }
    }

    fun getUpcomingBookings(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT b.*, m.member_name, mr.meeting_room_no, l.location_name
            FROM Bookings b
            JOIN Members m ON b.member_id = m.member_id
            LEFT JOIN MeetingRooms mr ON b.resource_id = mr.meeting_room_id AND b.resource_type = 'meeting_room'
            LEFT JOIN Locations l ON mr.location_id = l.location_id
            WHERE b.start_time > NOW()
            ORDER BY b.start_time ASC
            LIMIT 10
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val bookings = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                bookings.add(mapOf(
                    "booking_id" to rs.getLong("booking_id"),
                    "member_name" to rs.getString("member_name"),
                    "resource_type" to rs.getString("resource_type"),
                    "meeting_room_no" to (rs.getLong("meeting_room_no").takeIf { !rs.wasNull() } ?: "N/A"),
                    "location_name" to (rs.getString("location_name") ?: "N/A"),
                    "start_time" to rs.getTimestamp("start_time").toLocalDateTime(),
                    "end_time" to rs.getTimestamp("end_time").toLocalDateTime(),
                    "total_price" to rs.getLong("total_price")
                ))
            }
            bookings
        }
    }

    fun getPeakUsageHours(): Map<String, Any> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT 
                HOUR(start_time) as hour_of_day,
                COUNT(*) as booking_count,
                AVG(total_price) as avg_price
            FROM Bookings
            WHERE start_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY HOUR(start_time)
            ORDER BY booking_count DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val hourlyData = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                hourlyData.add(mapOf(
                    "hour" to rs.getInt("hour_of_day"),
                    "booking_count" to rs.getInt("booking_count"),
                    "avg_price" to rs.getDouble("avg_price")
                ))
            }
            mapOf("peak_hours" to hourlyData)
        }
    }
}