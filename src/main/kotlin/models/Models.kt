package models

import java.time.LocalDateTime

/**
 * Data Models for Coworking Space Management System
 * Location: src/main/kotlin/models/Models.kt
 */

data class Member(
    val memberId: Long = 0,
    val memberName: String,
    val email: String,
    val phone: String,
    val membershipTypeId: Long,
    val status: String = "active"
)

data class PlanType(
    val planTypeId: Long = 0,
    val planType: String,
    val monthlyFees: Long
)

data class Location(
    val locationId: Long = 0,
    val locationName: String,
    val address: String
)

data class Workspace(
    val workspaceId: Long = 0,
    val workspaceType: Long,
    val locationId: Long,
    val occupied: String = "not_occupied",
    val workspaceAreaSqft: Long
)

data class MeetingRoom(
    val meetingRoomId: Long = 0,
    val meetingRoomNo: Long,
    val locationId: Long,
    val occupied: String = "not_occupied",
    val capacity: Long,
    val pricePerHour: Long,
    val deviceId: Long
)

data class Equipment(
    val equipmentId: Long = 0,
    val equipmentTypeId: Long,
    val pricePerHour: Long,
    val locationId: Long,
    val occupied: String = "not_occupied"
)

data class Booking(
    val bookingId: Long = 0,
    val memberId: Long,
    val resourceType: String,
    val resourceId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val totalPrice: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val invoiceId: Long? = null
)

data class WorkspaceBooking(
    val bookingId: Long = 0,
    val memberId: Long,
    val workspaceId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val invoiceId: Long? = null
)

data class AccessLog(
    val logId: Long = 0,
    val memberId: Long,
    val deviceId: Long,
    val timestamp: LocalDateTime,
    val entryType: String
)

data class AccessCard(
    val cardId: Long = 0,
    val memberId: Long,
    val accessType: Long
)

data class Invoice(
    val invoiceId: Long = 0,
    val memberId: Long,
    val totalAmount: Long,
    val dueDate: LocalDateTime,
    val invoiceStatus: String,
    val invoiceDate: LocalDateTime
)

data class Event(
    val eventId: Long = 0,
    val title: String,
    val locationId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val capacity: Long
)

data class StockedItem(
    val itemId: Long = 0,
    val itemTypeId: Long,
    val vendorId: Long,
    val price: Long,
    val availableQuantity: Long,
    val locationId: Long
)

data class ServiceUsage(
    val usageId: Long = 0,
    val serviceId: Long,
    val memberId: Long,
    val quantity: Long,
    val totalPrice: Long,
    val purchasedDate: LocalDateTime,
    val invoiceId: Long? = null
)