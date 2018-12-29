package ondutyScheduler.nonAvailabilities

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
data class ScheduleTO(
        val id: Long,
        val name: String
)

data class NonAvailabilityNoticeTO(
        val id: Long?,
        val startingAt: LocalDateTime,
        val endingAt: LocalDateTime,
        val comment: String,
        val regardingSchedules: List<Long>
)

data class EmployeePageTO(
        val name: String,
        val nonAvailabilityNotices: List<NonAvailabilityNoticeTO>,
        val allExistingSchedules: List<ScheduleTO>,
        val allowedSchedules: List<Long>
)

interface NonAvailabilitiesBCI {
    fun employeePage(employeeId: Long): Optional<EmployeePageTO>;
    fun addNonAvailabilityNotice(employeeId: Long, nonAvailabilityNoticeTO: NonAvailabilityNoticeTO): Optional<List<NonAvailabilityNoticeTO>>
    fun deleteNonAvailabilityNotice(employeeId: Long, id: Long): Optional<List<NonAvailabilityNoticeTO>>
}

@Component
class NonAvailabilitiesBF(
        private val employeeEM: EmployeeEM,
        private val scheduleEM: ScheduleEM,
        private val nonAvailabilityNoticeEM: NonAvailabilityNoticeEM
): NonAvailabilitiesBCI {

    override fun employeePage(employeeId: Long): Optional<EmployeePageTO> {
        val employeeBE = employeeEM.findById(employeeId)
        val schedulesBEs = scheduleEM.findAll()
        return employeeBE.map {
            EmployeePageTO(
                    it.name,
                    it.nonAvailabilityNotices.map(this::mapNonAvailabilityNoticeBE),
                    schedulesBEs.map(this::mapScheduleBE),
                    schedulesBEs.map { it.id }
            )
        }
    }

    override fun addNonAvailabilityNotice(employeeId: Long, nonAvailabilityNoticeTO: NonAvailabilityNoticeTO): Optional<List<NonAvailabilityNoticeTO>> {
        val employeeBE = employeeEM.findById(employeeId)
        val schedulesBEs = scheduleEM.findAll()
        return employeeBE.map {
            val newNotice = mapNonAvailabilityNoticeTO(nonAvailabilityNoticeTO, schedulesBEs, it)
            it.nonAvailabilityNotices.add(newNotice)
            nonAvailabilityNoticeEM.save(newNotice)
            return@map it.nonAvailabilityNotices.map(this::mapNonAvailabilityNoticeBE)
        }
    }

    override fun deleteNonAvailabilityNotice(employeeId: Long, id: Long): Optional<List<NonAvailabilityNoticeTO>> {
        nonAvailabilityNoticeEM.deleteById(id)
        val employeeBE = employeeEM.findById(employeeId)
        return employeeBE.map {
            return@map it.nonAvailabilityNotices.map(this::mapNonAvailabilityNoticeBE)
        }
    }

    private fun mapNonAvailabilityNoticeBE(notice: NonAvailabilityNoticeBE): NonAvailabilityNoticeTO {
        return NonAvailabilityNoticeTO(
                notice.id,
                notice.starting,
                notice.ending,
                notice.comment,
                notice.regardingSchedules.map { it.id }
        )
    }

    private fun mapNonAvailabilityNoticeTO(notice: NonAvailabilityNoticeTO, schedules: List<ScheduleBE>, employee: EmployeeBE): NonAvailabilityNoticeBE {
        return NonAvailabilityNoticeBE(
                null,
                notice.startingAt,
                notice.endingAt,
                notice.comment,
                schedules.filter { notice.regardingSchedules.contains(it.id) }.toSet(),
                employee
        )
    }

    private fun mapScheduleBE(schedule:  ScheduleBE): ScheduleTO {
        return ScheduleTO(
                schedule.id,
                schedule.name
        )
    }
}