package dao

import database.DatabaseManager
import models.Invoice
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Billing Data Access Object
 * Location: src/main/kotlin/dao/BillingDAO.kt
 */
class BillingDAO {

    fun createInvoice(invoice: Invoice): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO Invoice (member_id, total_amount, due_date, invoice_status, invoice_date) VALUES (?, ?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setLong(1, invoice.memberId)
            stmt.setLong(2, invoice.totalAmount)
            stmt.setTimestamp(3, Timestamp.valueOf(invoice.dueDate))
            stmt.setString(4, invoice.invoiceStatus)
            stmt.setTimestamp(5, Timestamp.valueOf(invoice.invoiceDate))

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getInvoicesByMember(memberId: Long): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT i.*, m.member_name
            FROM Invoice i
            JOIN Members m ON i.member_id = m.member_id
            WHERE i.member_id = ?
            ORDER BY i.invoice_date DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            val rs = stmt.executeQuery()

            val invoices = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                invoices.add(mapOf(
                    "invoice_id" to rs.getLong("invoice_id"),
                    "member_name" to rs.getString("member_name"),
                    "total_amount" to rs.getLong("total_amount"),
                    "due_date" to rs.getTimestamp("due_date").toLocalDateTime(),
                    "invoice_status" to rs.getString("invoice_status"),
                    "invoice_date" to rs.getTimestamp("invoice_date").toLocalDateTime()
                ))
            }
            invoices
        }
    }

    fun updateInvoiceStatus(invoiceId: Long, status: String): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = "UPDATE Invoice SET invoice_status = ? WHERE invoice_id = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, status)
            stmt.setLong(2, invoiceId)
            stmt.executeUpdate() > 0
        }
    }

    fun getOverdueInvoices(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT i.*, m.member_name, m.email, m.phone
            FROM Invoice i
            JOIN Members m ON i.member_id = m.member_id
            WHERE i.due_date < NOW() AND i.invoice_status != 'paid'
            ORDER BY i.due_date ASC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val overdueInvoices = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                overdueInvoices.add(mapOf(
                    "invoice_id" to rs.getLong("invoice_id"),
                    "member_name" to rs.getString("member_name"),
                    "email" to rs.getString("email"),
                    "phone" to rs.getString("phone"),
                    "total_amount" to rs.getLong("total_amount"),
                    "due_date" to rs.getTimestamp("due_date").toLocalDateTime(),
                    "invoice_status" to rs.getString("invoice_status"),
                    "days_overdue" to "DATEDIFF(NOW(), due_date)"
                ))
            }
            overdueInvoices
        }
    }

    fun getRevenueAnalysis(): Map<String, Any> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT 
                SUM(CASE WHEN invoice_status = 'paid' THEN total_amount ELSE 0 END) as total_revenue,
                SUM(CASE WHEN invoice_status = 'pending' THEN total_amount ELSE 0 END) as pending_revenue,
                SUM(CASE WHEN invoice_status = 'overdue' THEN total_amount ELSE 0 END) as overdue_revenue,
                COUNT(CASE WHEN invoice_status = 'paid' THEN 1 END) as paid_invoices,
                COUNT(CASE WHEN invoice_status = 'pending' THEN 1 END) as pending_invoices,
                COUNT(CASE WHEN invoice_status = 'overdue' THEN 1 END) as overdue_invoices,
                AVG(CASE WHEN invoice_status = 'paid' THEN total_amount END) as avg_invoice_amount
            FROM Invoice
            WHERE invoice_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR)
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            if (rs.next()) {
                mapOf(
                    "total_revenue" to rs.getLong("total_revenue"),
                    "pending_revenue" to rs.getLong("pending_revenue"),
                    "overdue_revenue" to rs.getLong("overdue_revenue"),
                    "paid_invoices" to rs.getInt("paid_invoices"),
                    "pending_invoices" to rs.getInt("pending_invoices"),
                    "overdue_invoices" to rs.getInt("overdue_invoices"),
                    "avg_invoice_amount" to rs.getDouble("avg_invoice_amount")
                )
            } else emptyMap()
        }
    }

    fun getMonthlyRevenue(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT 
                YEAR(invoice_date) as year,
                MONTH(invoice_date) as month,
                MONTHNAME(invoice_date) as month_name,
                SUM(CASE WHEN invoice_status = 'paid' THEN total_amount ELSE 0 END) as monthly_revenue,
                COUNT(*) as total_invoices
            FROM Invoice
            WHERE invoice_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
            GROUP BY YEAR(invoice_date), MONTH(invoice_date)
            ORDER BY year DESC, month DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val monthlyRevenue = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                monthlyRevenue.add(mapOf(
                    "year" to rs.getInt("year"),
                    "month" to rs.getInt("month"),
                    "month_name" to rs.getString("month_name"),
                    "monthly_revenue" to rs.getLong("monthly_revenue"),
                    "total_invoices" to rs.getInt("total_invoices")
                ))
            }
            monthlyRevenue
        }
    }

    fun getMembershipTierProfitability(): List<Map<String, Any>> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT pt.plan_type,
                   COUNT(DISTINCT m.member_id) as member_count,
                   SUM(i.total_amount) as total_revenue,
                   AVG(i.total_amount) as avg_revenue_per_member,
                   pt.monthly_fees
            FROM Members m
            JOIN Plan_Type pt ON m.membership_type_id = pt.plan_type_id
            LEFT JOIN Invoice i ON m.member_id = i.member_id AND i.invoice_status = 'paid'
            WHERE m.status = 'active'
            GROUP BY pt.plan_type_id, pt.plan_type, pt.monthly_fees
            ORDER BY total_revenue DESC
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val profitability = mutableListOf<Map<String, Any>>()
            while (rs.next()) {
                profitability.add(mapOf(
                    "plan_type" to rs.getString("plan_type"),
                    "member_count" to rs.getInt("member_count"),
                    "total_revenue" to rs.getLong("total_revenue"),
                    "avg_revenue_per_member" to rs.getDouble("avg_revenue_per_member"),
                    "monthly_fees" to rs.getLong("monthly_fees")
                ))
            }
            profitability
        }
    }
}