package service

import dao.*
import models.*
import java.time.LocalDateTime

/**
 * Main Business Logic Service for Coworking Space Management
 * Location: src/main/kotlin/service/CoworkingSpaceService.kt
 */
class CoworkingSpaceService {

    private val memberDAO = MemberDAO()
    private val workspaceDAO = WorkspaceDAO()
    private val bookingDAO = BookingDAO()
    private val accessControlDAO = AccessControlDAO()
    private val serviceDAO = ServiceDAO()
    private val billingDAO = BillingDAO()

    // Member Management Functions
    fun registerMember(memberName: String, email: String, phone: String, membershipTypeId: Long): Long {
        val member = Member(
            memberName = memberName,
            email = email,
            phone = phone,
            membershipTypeId = membershipTypeId
        )

        val memberId = memberDAO.addMember(member)

        // Create access card for new member
        if (memberId > 0) {
            val accessCard = AccessCard(
                memberId = memberId,
                accessType = membershipTypeId
            )
            accessControlDAO.createAccessCard(accessCard)
        }

        return memberId
    }

    fun getMembersByAccessLevel(membershipTypeId: Long): List<Member> {
        return memberDAO.getMembersByMembershipType(membershipTypeId)
    }

    fun updateMemberStatus(memberId: Long, status: String): Boolean {
        return memberDAO.updateMemberStatus(memberId, status)
    }

    // Workspace Booking Functions
    fun bookWorkspace(memberId: Long, workspaceId: Long, startTime: LocalDateTime, endTime: LocalDateTime): String {
        // Check if workspace is available
        if (!workspaceDAO.checkWorkspaceAvailability(workspaceId, startTime, endTime)) {
            return "Error: Workspace is not available for the selected time slot"
        }

        // Create workspace booking
        val booking = WorkspaceBooking(
            memberId = memberId,
            workspaceId = workspaceId,
            startTime = startTime,
            endTime = endTime
        )

        val bookingId = workspaceDAO.createWorkspaceBooking(booking)

        if (bookingId > 0) {
            // Update workspace status if it's an immediate booking
            if (startTime.isBefore(LocalDateTime.now().plusHours(1))) {
                workspaceDAO.updateWorkspaceStatus(workspaceId, "occupied")
            }
            return "✅ Workspace booked successfully! Booking ID: $bookingId"
        }

        return "❌ Failed to book workspace"
    }

    fun bookMeetingRoom(memberId: Long, meetingRoomId: Long, startTime: LocalDateTime, endTime: LocalDateTime, pricePerHour: Long): String {
        // Check for booking conflicts
        if (bookingDAO.checkBookingConflict("meeting_room", meetingRoomId, startTime, endTime)) {
            return "Error: Meeting room is already booked for the selected time slot"
        }

        // Calculate total price
        val hours = java.time.Duration.between(startTime, endTime).toHours()
        val totalPrice = hours * pricePerHour

        val booking = Booking(
            memberId = memberId,
            resourceType = "meeting_room",
            resourceId = meetingRoomId,
            startTime = startTime,
            endTime = endTime,
            totalPrice = totalPrice
        )

        val bookingId = bookingDAO.createBooking(booking)

        return if (bookingId > 0) {
            "✅ Meeting room booked successfully! Total cost: $$totalPrice"
        } else {
            "❌ Failed to book meeting room"
        }
    }

    // Access Control Functions
    fun logMemberAccess(memberId: Long, deviceId: Long, entryType: String): String {
        val accessLog = AccessLog(
            memberId = memberId,
            deviceId = deviceId,
            timestamp = LocalDateTime.now(),
            entryType = entryType
        )

        val logId = accessControlDAO.logAccess(accessLog)

        return if (logId > 0) {
            "✅ Access logged: Member $memberId ${entryType.uppercase()}"
        } else {
            "❌ Failed to log access"
        }
    }

    fun getMemberAccessHistory(memberId: Long, days: Int = 30): List<Map<String, Any>> {
        val fromDate = LocalDateTime.now().minusDays(days.toLong())
        val toDate = LocalDateTime.now()
        return accessControlDAO.getAccessLogsByMember(memberId, fromDate, toDate)
    }

    // Service Management Functions
    fun purchaseService(memberId: Long, serviceId: Long, quantity: Long, unitPrice: Long): String {
        val totalPrice = quantity * unitPrice

        val serviceUsage = ServiceUsage(
            serviceId = serviceId,
            memberId = memberId,
            quantity = quantity,
            totalPrice = totalPrice,
            purchasedDate = LocalDateTime.now()
        )

        val usageId = serviceDAO.recordServiceUsage(serviceUsage)

        // Update stock if it's a stocked item
        serviceDAO.updateStockQuantity(serviceId, quantity)

        return if (usageId > 0) {
            "✅ Service purchased successfully! Total: $$totalPrice"
        } else {
            "❌ Failed to purchase service"
        }
    }

    // Billing Functions
    fun generateMemberInvoice(memberId: Long, totalAmount: Long, dueDate: LocalDateTime): Long {
        val invoice = Invoice(
            memberId = memberId,
            totalAmount = totalAmount,
            dueDate = dueDate,
            invoiceStatus = "pending",
            invoiceDate = LocalDateTime.now()
        )

        return billingDAO.createInvoice(invoice)
    }

    fun payInvoice(invoiceId: Long): Boolean {
        return billingDAO.updateInvoiceStatus(invoiceId, "paid")
    }

    // Analytics and Reporting Functions
    fun getSpaceUtilizationReport(): Map<String, Any> {
        return workspaceDAO.getWorkspaceUtilizationByLocation()
    }

    fun getMeetingRoomAnalysis(): List<Map<String, Any>> {
        return bookingDAO.getMeetingRoomBookingPatterns()
    }

    fun getPopularAmenitiesReport(): List<Map<String, Any>> {
        return serviceDAO.getPopularAmenities()
    }

    fun getMemberRetentionStats(): Map<String, Any> {
        return memberDAO.getMemberRetentionMetrics()
    }

    fun getRevenueReport(): Map<String, Any> {
        return billingDAO.getRevenueAnalysis()
    }

    fun getPeakUsageAnalysis(): Map<String, Any> {
        return bookingDAO.getPeakUsageHours()
    }

    fun getAccessStatsByLocation(): Map<String, Any> {
        return accessControlDAO.getAccessStatsByLocation()
    }

    fun getLowStockAlert(): List<Map<String, Any>> {
        return serviceDAO.getLowStockItems(5) // Alert when stock is 5 or below
    }

    fun getOverdueInvoicesReport(): List<Map<String, Any>> {
        return billingDAO.getOverdueInvoices()
    }

    fun getMembershipProfitability(): List<Map<String, Any>> {
        return billingDAO.getMembershipTierProfitability()
    }

    // Dashboard Summary Function
    fun getDashboardSummary(): Map<String, Any> {
        val summary = mutableMapOf<String, Any>()

        // Get today's access logs
        summary["today_access_logs"] = accessControlDAO.getTodayAccessLogs()

        // Get upcoming bookings
        summary["upcoming_bookings"] = bookingDAO.getUpcomingBookings()

        // Get member retention stats
        summary["member_stats"] = getMemberRetentionStats()

        // Get revenue summary
        summary["revenue_stats"] = getRevenueReport()

        // Get low stock alerts
        summary["low_stock_items"] = getLowStockAlert()

        // Get overdue invoices count
        summary["overdue_invoices_count"] = getOverdueInvoicesReport().size

        return summary
    }
}