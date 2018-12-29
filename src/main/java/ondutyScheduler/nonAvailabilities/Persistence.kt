package ondutyScheduler.nonAvailabilities

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.persistence.*

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */

@Entity
@Table(name = "schedule")
data class ScheduleBE(
        @Id val id: Long,
        val name: String,
        val opsGenieId: String
)

@Entity
@Table(name = "non_availability_notice")
data class NonAvailabilityNoticeBE(
        @Id @GeneratedValue val id: Long?,
        val starting: LocalDateTime,
        val ending: LocalDateTime,
        val comment: String,
        @ManyToMany val regardingSchedules: Set<ScheduleBE>,
        @ManyToOne val employee: EmployeeBE
)

@Entity
@Table(name = "workload_preference")
data class WorkloadPreferenceBE(
        @Id @GeneratedValue val id: Long,
        val minWorkload: Int,
        val maxWorkload: Int,
        @ManyToMany val regardingSchedules: Set<ScheduleBE>,
        @ManyToOne val employee: EmployeeBE
)

@Entity
@Table(name = "employee")
data class EmployeeBE(
        @Id val id: Long,
        val name: String,
        val opsGenieId: String,
        @OneToMany(mappedBy = "employee") val nonAvailabilityNotices: MutableCollection<NonAvailabilityNoticeBE>,
        @OneToMany(mappedBy = "employee") val workloadPreferences: List<WorkloadPreferenceBE>
)

@Repository
interface EmployeeEM: JpaRepository<EmployeeBE, Long>

@Repository
interface ScheduleEM: JpaRepository<ScheduleBE, Long>

@Repository
interface NonAvailabilityNoticeEM: JpaRepository<NonAvailabilityNoticeBE, Long>