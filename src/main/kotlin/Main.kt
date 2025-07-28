import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import dao.*
import models.*
import service.CoworkingSpaceService
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDateTime

// Database connection tester
fun connectToDatabase(): String {
    val url = "jdbc:mysql://localhost:3306/coworking_space"
    val user = "root"
    val password = "varun_382900"
    return try {
        val connection: Connection = DriverManager.getConnection(url, user, password)
        connection.close()
        "✅ Connected to the database successfully!"
    } catch (e: Exception) {
        "❌ Failed to connect: ${e.message}"
    }
}

// DAO and Service instances
val memberDAO = MemberDAO()
val bookingDAO = BookingDAO()
val workspaceDAO = WorkspaceDAO()
val serviceDAO = ServiceDAO()
val billingDAO = BillingDAO()
val coworkingService = CoworkingSpaceService()

// Test methods for buttons
fun testDashboard(): String = try {
    val dashboard = coworkingService.getDashboardSummary()
    "✅ Dashboard loaded: ${dashboard.size} items"
} catch (e: Exception) {
    "❌ Dashboard failed: ${e.message}"
}

fun testMembers(): String = try {
    val members = memberDAO.getAllMembers()
    "✅ Found ${members.size} members"
} catch (e: Exception) {
    "❌ Member test failed: ${e.message}"
}

fun testBookings(): String = try {
    val bookings = bookingDAO.getUpcomingBookings()
    "✅ Found ${bookings.size} upcoming bookings"
} catch (e: Exception) {
    "❌ Booking test failed: ${e.message}"
}

fun testWorkspaces(): String = try {
    val workspaces = workspaceDAO.getAvailableWorkspaces(1)
    "✅ Found ${workspaces.size} available workspaces"
} catch (e: Exception) {
    "❌ Workspace test failed: ${e.message}"
}

fun testServices(): String = try {
    // Use serviceDAO to get popular amenities as a sample test
    val services = serviceDAO.getPopularAmenities()
    "✅ Found ${services.size} popular amenities"
} catch (e: Exception) {
    "❌ Service test failed: ${e.message}"
}

fun testInvoices(): String = try {
    val invoices = billingDAO.getOverdueInvoices()
    "✅ Found ${invoices.size} overdue invoices"
} catch (e: Exception) {
    "❌ Invoice test failed: ${e.message}"
}

@Composable
@Preview
fun App() {

    val scrollState = rememberScrollState()

    var statusMessage by remember { mutableStateOf("Coworking Space Management System") }
    var testResults by remember { mutableStateOf(listOf<String>()) }

    // -- Add Member form states --
    var memberName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var membershipTypeId by remember { mutableStateOf("") }
    var addMemberResult by remember { mutableStateOf("") }

    // -- Workspace Booking states --
    var bookingMemberId by remember { mutableStateOf("") }
    var bookingWorkspaceId by remember { mutableStateOf("") }
    var bookingStartDateTime by remember { mutableStateOf("") }
    var bookingEndDateTime by remember { mutableStateOf("") }
    var bookingResult by remember { mutableStateOf("") }

    // -- Meeting Room Booking states --
    var meetingBookingMemberId by remember { mutableStateOf("") }
    var meetingRoomId by remember { mutableStateOf("") }
    var meetingStartDateTime by remember { mutableStateOf("") }
    var meetingEndDateTime by remember { mutableStateOf("") }
    var meetingRoomPricePerHour by remember { mutableStateOf("") }
    var meetingBookingResult by remember { mutableStateOf("") }

    // -- Service Purchase states --
    var servicePurchaseMemberId by remember { mutableStateOf("") }
    var serviceId by remember { mutableStateOf("") }
    var serviceQuantity by remember { mutableStateOf("") }
    var serviceUnitPrice by remember { mutableStateOf("") }
    var servicePurchaseResult by remember { mutableStateOf("") }

    // -- Invoice Payment states --
    var invoiceIdToPay by remember { mutableStateOf("") }
    var invoicePaymentResult by remember { mutableStateOf("") }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text("Coworking Space Management System", style = MaterialTheme.typography.h4)

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.body1,
                    color = if (statusMessage.startsWith("✅")) Color.Green else Color.Black
                )

                Spacer(Modifier.height(16.dp))

                // DB Connection Test
                Button(
                    onClick = { statusMessage = connectToDatabase() },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Test Database Connection")
                }

                // Test buttons grid
                Row(modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { testResults += testDashboard() }, modifier = Modifier.weight(1f)) { Text("Test Dashboard") }
                    Button(onClick = { testResults += testMembers() }, modifier = Modifier.weight(1f)) { Text("Test Members") }
                }
                Row(modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { testResults += testBookings() }, modifier = Modifier.weight(1f)) { Text("Test Bookings") }
                    Button(onClick = { testResults += testWorkspaces() }, modifier = Modifier.weight(1f)) { Text("Test Workspaces") }
                }
                Row(modifier = Modifier.fillMaxWidth(0.8f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { testResults += testServices() }, modifier = Modifier.weight(1f)) { Text("Test Services") }
                    Button(onClick = { testResults += testInvoices() }, modifier = Modifier.weight(1f)) { Text("Test Invoices") }
                }

                Spacer(Modifier.height(24.dp))

                // Add Member Form
                Card(modifier = Modifier.fillMaxWidth(0.8f), elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
                        Text("Add Member", style = MaterialTheme.typography.h6)
                        OutlinedTextField(
                            value = memberName,
                            onValueChange = { memberName = it },
                            label = { Text("Member Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = membershipTypeId,
                            onValueChange = { membershipTypeId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Membership Type ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (memberName.isBlank() || email.isBlank() || phone.isBlank() || membershipTypeId.isBlank()) {
                                    addMemberResult = "❌ Please fill in all fields"
                                    return@Button
                                }
                                try {
                                    val member = Member(
                                        memberName = memberName.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        membershipTypeId = membershipTypeId.toLong(),
                                        status = "active"
                                    )
                                    val newMemberId = memberDAO.addMember(member)
                                    if (newMemberId > 0) {
                                        addMemberResult = "✅ Member added with ID: $newMemberId"
                                        memberName = ""
                                        email = ""
                                        phone = ""
                                        membershipTypeId = ""
                                    } else {
                                        addMemberResult = "❌ Failed to add member"
                                    }
                                } catch (e: Exception) {
                                    addMemberResult = "❌ Add member failed: ${e.message}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Add Member") }
                        if (addMemberResult.isNotEmpty()) {
                            Text(addMemberResult, color = if (addMemberResult.startsWith("✅")) Color.Green else Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Workspace Booking Form
                Card(modifier = Modifier.fillMaxWidth(0.8f), elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Book Workspace", style = MaterialTheme.typography.h6)
                        OutlinedTextField(
                            value = bookingMemberId,
                            onValueChange = { bookingMemberId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Member ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = bookingWorkspaceId,
                            onValueChange = { bookingWorkspaceId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Workspace ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = bookingStartDateTime,
                            onValueChange = { bookingStartDateTime = it },
                            label = { Text("Start Time (YYYY-MM-DDTHH:MM)") },
                            placeholder = { Text("2025-07-28T14:30") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bookingEndDateTime,
                            onValueChange = { bookingEndDateTime = it },
                            label = { Text("End Time (YYYY-MM-DDTHH:MM)") },
                            placeholder = { Text("2025-07-28T16:30") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (bookingMemberId.isBlank() || bookingWorkspaceId.isBlank() || bookingStartDateTime.isBlank() || bookingEndDateTime.isBlank()) {
                                    bookingResult = "❌ Please fill all booking fields"
                                    return@Button
                                }
                                try {
                                    val memberIdLong = bookingMemberId.toLong()
                                    val workspaceIdLong = bookingWorkspaceId.toLong()
                                    val startTime = LocalDateTime.parse(bookingStartDateTime)
                                    val endTime = LocalDateTime.parse(bookingEndDateTime)

                                    bookingResult = coworkingService.bookWorkspace(memberIdLong, workspaceIdLong, startTime, endTime)

                                    if (bookingResult.startsWith("✅")) {
                                        bookingMemberId = ""
                                        bookingWorkspaceId = ""
                                        bookingStartDateTime = ""
                                        bookingEndDateTime = ""
                                    }
                                } catch (e: Exception) {
                                    bookingResult = "❌ Booking failed: ${e.message}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Book Workspace") }
                        if (bookingResult.isNotEmpty()) {
                            Text(bookingResult, color = if (bookingResult.startsWith("✅")) Color.Green else Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Meeting Room Booking Form
                Card(modifier = Modifier.fillMaxWidth(0.8f), elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        var meetingBookingMemberId by remember { mutableStateOf("") }
                        var meetingRoomId by remember { mutableStateOf("") }
                        var meetingStartDateTime by remember { mutableStateOf("") }
                        var meetingEndDateTime by remember { mutableStateOf("") }
                        var meetingRoomPricePerHour by remember { mutableStateOf("") }
                        var meetingBookingResult by remember { mutableStateOf("") }

                        Text("Book Meeting Room", style = MaterialTheme.typography.h6)
                        OutlinedTextField(
                            value = meetingBookingMemberId,
                            onValueChange = { meetingBookingMemberId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Member ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = meetingRoomId,
                            onValueChange = { meetingRoomId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Meeting Room ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = meetingStartDateTime,
                            onValueChange = { meetingStartDateTime = it },
                            label = { Text("Start Time (YYYY-MM-DDTHH:MM)") },
                            placeholder = { Text("2025-07-28T14:30") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = meetingEndDateTime,
                            onValueChange = { meetingEndDateTime = it },
                            label = { Text("End Time (YYYY-MM-DDTHH:MM)") },
                            placeholder = { Text("2025-07-28T16:30") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = meetingRoomPricePerHour,
                            onValueChange = { meetingRoomPricePerHour = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Price Per Hour") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (meetingBookingMemberId.isBlank() || meetingRoomId.isBlank() || meetingStartDateTime.isBlank() || meetingEndDateTime.isBlank() || meetingRoomPricePerHour.isBlank()) {
                                    meetingBookingResult = "❌ Please fill all fields"
                                    return@Button
                                }
                                try {
                                    val memId = meetingBookingMemberId.toLong()
                                    val roomId = meetingRoomId.toLong()
                                    val start = LocalDateTime.parse(meetingStartDateTime)
                                    val end = LocalDateTime.parse(meetingEndDateTime)
                                    val priceHour = meetingRoomPricePerHour.toLong()

                                    meetingBookingResult = coworkingService.bookMeetingRoom(memId, roomId, start, end, priceHour)

                                    if (meetingBookingResult.startsWith("✅")) {
                                        meetingBookingMemberId = ""
                                        meetingRoomId = ""
                                        meetingStartDateTime = ""
                                        meetingEndDateTime = ""
                                        meetingRoomPricePerHour = ""
                                    }
                                } catch (e: Exception) {
                                    meetingBookingResult = "❌ Booking failed: ${e.message}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Book Meeting Room") }
                        if (meetingBookingResult.isNotEmpty()) {
                            Text(meetingBookingResult, color = if (meetingBookingResult.startsWith("✅")) Color.Green else Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Service Purchase Form
                Card(modifier = Modifier.fillMaxWidth(0.8f), elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        var servicePurchaseMemberId by remember { mutableStateOf("") }
                        var serviceId by remember { mutableStateOf("") }
                        var serviceQuantity by remember { mutableStateOf("") }
                        var serviceUnitPrice by remember { mutableStateOf("") }
                        var servicePurchaseResult by remember { mutableStateOf("") }

                        Text("Purchase Service / Amenity", style = MaterialTheme.typography.h6)

                        OutlinedTextField(
                            value = servicePurchaseMemberId,
                            onValueChange = { servicePurchaseMemberId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Member ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = serviceId,
                            onValueChange = { serviceId = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Service Item ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = serviceQuantity,
                            onValueChange = { serviceQuantity = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = serviceUnitPrice,
                            onValueChange = { serviceUnitPrice = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Unit Price") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (servicePurchaseMemberId.isBlank() || serviceId.isBlank() || serviceQuantity.isBlank() || serviceUnitPrice.isBlank()) {
                                    servicePurchaseResult = "❌ Please fill all fields"
                                    return@Button
                                }
                                try {
                                    val memId = servicePurchaseMemberId.toLong()
                                    val servId = serviceId.toLong()
                                    val qty = serviceQuantity.toLong()
                                    val unitPrice = serviceUnitPrice.toLong()
                                    servicePurchaseResult = coworkingService.purchaseService(memId, servId, qty, unitPrice)
                                    if (servicePurchaseResult.startsWith("✅")) {
                                        servicePurchaseMemberId = ""
                                        serviceId = ""
                                        serviceQuantity = ""
                                        serviceUnitPrice = ""
                                    }
                                } catch (e: Exception) {
                                    servicePurchaseResult = "❌ Service purchase failed: ${e.message}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Purchase Service") }
                        if (servicePurchaseResult.isNotEmpty()) {
                            Text(servicePurchaseResult, color = if (servicePurchaseResult.startsWith("✅")) Color.Green else Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Invoice Payment Form
                Card(modifier = Modifier.fillMaxWidth(0.8f), elevation = 4.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        var invoiceIdToPay by remember { mutableStateOf("") }
                        var invoicePaymentResult by remember { mutableStateOf("") }

                        Text("Pay Invoice", style = MaterialTheme.typography.h6)

                        OutlinedTextField(
                            value = invoiceIdToPay,
                            onValueChange = { invoiceIdToPay = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Invoice ID (number)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (invoiceIdToPay.isBlank()) {
                                    invoicePaymentResult = "❌ Please provide invoice ID"
                                    return@Button
                                }
                                try {
                                    val invId = invoiceIdToPay.toLong()
                                    val success = coworkingService.payInvoice(invId)
                                    invoicePaymentResult = if (success) {
                                        "✅ Invoice $invId marked as paid"
                                    } else {
                                        "❌ Failed to update invoice status"
                                    }
                                    if (success) invoiceIdToPay = ""
                                } catch (e: Exception) {
                                    invoicePaymentResult = "❌ Invoice payment failed: ${e.message}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Pay Invoice") }
                        if (invoicePaymentResult.isNotEmpty()) {
                            Text(invoicePaymentResult, color = if (invoicePaymentResult.startsWith("✅")) Color.Green else Color.Red, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Clear test results button
                Button(
                    onClick = { testResults = emptyList() },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Clear Results")
                }

                // Results display
                if (testResults.isNotEmpty()) {
                    Divider(modifier = Modifier.fillMaxWidth(0.8f))
                    Text("Test Results:", style = MaterialTheme.typography.h6)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(0.8f).height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(testResults) { result ->
                            Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                                Text(
                                    text = result,
                                    modifier = Modifier.padding(8.dp),
                                    color = if (result.startsWith("✅")) Color.Green else Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Coworking Space Management System"
    ) {
        App()
    }
}
