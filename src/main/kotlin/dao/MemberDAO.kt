package dao

import database.DatabaseManager
import models.Member
import models.PlanType
import java.sql.Statement

/**
 * Member Data Access Object
 * Location: src/main/kotlin/dao/MemberDAO.kt
 */
class MemberDAO {

    fun addMember(member: Member): Long {
        val connection = DatabaseManager.getConnection()
        val sql = "INSERT INTO Members (member_name, email, phone, membership_type_id, status) VALUES (?, ?, ?, ?, ?)"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            stmt.setString(1, member.memberName)
            stmt.setString(2, member.email)
            stmt.setString(3, member.phone)
            stmt.setLong(4, member.membershipTypeId)
            stmt.setString(5, member.status)

            stmt.executeUpdate()
            val keys = stmt.generatedKeys
            if (keys.next()) keys.getLong(1) else 0L
        }
    }

    fun getMemberById(memberId: Long): Member? {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Members WHERE member_id = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, memberId)
            val rs = stmt.executeQuery()

            if (rs.next()) {
                Member(
                    memberId = rs.getLong("member_id"),
                    memberName = rs.getString("member_name"),
                    email = rs.getString("email"),
                    phone = rs.getString("phone"),
                    membershipTypeId = rs.getLong("membership_type_id"),
                    status = rs.getString("status")
                )
            } else null
        }
    }

    fun getMembersByMembershipType(membershipTypeId: Long): List<Member> {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Members WHERE membership_type_id = ? AND status = 'active'"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setLong(1, membershipTypeId)
            val rs = stmt.executeQuery()

            val members = mutableListOf<Member>()
            while (rs.next()) {
                members.add(Member(
                    memberId = rs.getLong("member_id"),
                    memberName = rs.getString("member_name"),
                    email = rs.getString("email"),
                    phone = rs.getString("phone"),
                    membershipTypeId = rs.getLong("membership_type_id"),
                    status = rs.getString("status")
                ))
            }
            members
        }
    }

    fun updateMemberStatus(memberId: Long, status: String): Boolean {
        val connection = DatabaseManager.getConnection()
        val sql = "UPDATE Members SET status = ? WHERE member_id = ?"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, status)
            stmt.setLong(2, memberId)
            stmt.executeUpdate() > 0
        }
    }

    fun getAllMembers(): List<Member> {
        val connection = DatabaseManager.getConnection()
        val sql = "SELECT * FROM Members ORDER BY member_name"

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            val members = mutableListOf<Member>()
            while (rs.next()) {
                members.add(Member(
                    memberId = rs.getLong("member_id"),
                    memberName = rs.getString("member_name"),
                    email = rs.getString("email"),
                    phone = rs.getString("phone"),
                    membershipTypeId = rs.getLong("membership_type_id"),
                    status = rs.getString("status")
                ))
            }
            members
        }
    }

    fun getMemberRetentionMetrics(): Map<String, Any> {
        val connection = DatabaseManager.getConnection()
        val sql = """
            SELECT 
                COUNT(*) as total_members,
                COUNT(CASE WHEN status = 'active' THEN 1 END) as active_members,
                COUNT(CASE WHEN status = 'inactive' THEN 1 END) as inactive_members,
                (COUNT(CASE WHEN status = 'active' THEN 1 END) * 100.0 / COUNT(*)) as retention_rate
            FROM Members
        """

        return connection.use { conn ->
            val stmt = conn.prepareStatement(sql)
            val rs = stmt.executeQuery()

            if (rs.next()) {
                mapOf(
                    "total_members" to rs.getInt("total_members"),
                    "active_members" to rs.getInt("active_members"),
                    "inactive_members" to rs.getInt("inactive_members"),
                    "retention_rate" to rs.getDouble("retention_rate")
                )
            } else emptyMap()
        }
    }
}