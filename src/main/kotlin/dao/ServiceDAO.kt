package dao

import database.DatabaseManager
import models.ServiceUsage
import models.StockedItem
import java.sql.Statement
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDateTime

/**
 * Service Management Data Access Object
 * Location: src/main/kotlin/dao/ServiceDAO.kt
 */
class ServiceDAO {

    fun recordServiceUsage(serviceUsage: ServiceUsage): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO ServiceUsage (service_id, member_id, quantity, total_price, purchased_date, invoice_id) VALUES (?, ?, ?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, serviceUsage.serviceId)
            stmt.setLong(2, serviceUsage.memberId)
            stmt.setLong(3, serviceUsage.quantity)
            stmt.setLong(4, serviceUsage.totalPrice)
            stmt.setTimestamp(5, Timestamp.valueOf(serviceUsage.purchasedDate))
            if (serviceUsage.invoiceId != null) stmt.setLong(6, serviceUsage.invoiceId) else stmt.setNull(6, Types.BIGINT)

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getStockedItemsByLocation(locationId: Long): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT si.*, it.item_type_name, v.vendor_name
            FROM StockedItem si
            JOIN ItemsType it ON si.item_type_id = it.item_type_id
            JOIN Vendors v ON si.vendor_id = v.vendor_id
            WHERE si.location_id = ? AND si.available_quantity > 0
            ORDER BY it.item_type_name
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, locationId)
            val rs = stmt.executeQuery()

            val items = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                items.add(mapOf(
                    "item_id" to rs.getLong("item_id"),
                    "item_type_name" to rs.getString("item_type_name"),
                    "vendor_name" to rs.getString("vendor_name"),
                    "price" to rs.getLong("price"),
                    "available_quantity" to rs.getLong("available_quantity")
                ))
            }
            items
        }
    }

    fun updateStockQuantity(itemId: Long, quantityUsed: Long): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = "UPDATE StockedItem SET available_quantity = available_quantity - ? WHERE item_id = ? AND available_quantity >= ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, quantityUsed)
            stmt.setLong(2, itemId)
            stmt.setLong(3, quantityUsed)
            stmt.executeUpdate() > 0
        }
    }

    fun getServiceUsageByMember(memberId: Long): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT su.*, si.item_type_id, it.item_type_name, v.vendor_name
            FROM ServiceUsage su
            JOIN ServiceItems si ON su.service_id = si.item_id
            JOIN ItemsType it ON si.item_type_id = it.item_type_id
            JOIN Vendors v ON si.vendor_id = v.vendor_id
            WHERE su.member_id = ?
            ORDER BY su.purchased_date DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            val rs = stmt.executeQuery()

            val usage = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                usage.add(mapOf(
                    "usage_id" to rs.getLong("usage_id"),
                    "service_name" to rs.getString("item_type_name"),
                    "vendor_name" to rs.getString("vendor_name"),
                    "quantity" to rs.getLong("quantity"),
                    "total_price" to rs.getLong("total_price"),
                    "purchased_date" to rs.getTimestamp("purchased_date").toLocalDateTime()
                ))
            }
            usage
        }
    }

    fun getPopularAmenities(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT it.item_type_name as amenity_name,
                   COUNT(su.usage_id) as usage_count,
                   SUM(su.quantity) as total_quantity_used,
                   SUM(su.total_price) as total_revenue
            FROM ServiceUsage su
            JOIN ServiceItems si ON su.service_id = si.item_id
            JOIN ItemsType it ON si.item_type_id = it.item_type_id
            WHERE su.purchased_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY it.item_type_id, it.item_type_name
            ORDER BY usage_count DESC
            LIMIT 10
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val amenities = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                amenities.add(mapOf(
                    "amenity_name" to rs.getString("amenity_name"),
                    "usage_count" to rs.getInt("usage_count"),
                    "total_quantity_used" to rs.getLong("total_quantity_used"),
                    "total_revenue" to rs.getLong("total_revenue")
                ))
            }
            amenities
        }
    }

    fun getLowStockItems(threshold: Long = 10): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT si.*, it.item_type_name, v.vendor_name, l.location_name
            FROM StockedItem si
            JOIN ItemsType it ON si.item_type_id = it.item_type_id
            JOIN Vendors v ON si.vendor_id = v.vendor_id
            JOIN Locations l ON si.location_id = l.location_id
            WHERE si.available_quantity <= ?
            ORDER BY si.available_quantity ASC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, threshold)
            val rs = stmt.executeQuery()

            val lowStockItems = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                lowStockItems.add(mapOf(
                    "item_id" to rs.getLong("item_id"),
                    "item_type_name" to rs.getString("item_type_name"),
                    "vendor_name" to rs.getString("vendor_name"),
                    "location_name" to rs.getString("location_name"),
                    "available_quantity" to rs.getLong("available_quantity"),
                    "price" to rs.getLong("price")
                ))
            }
            lowStockItems
        }
    }
}