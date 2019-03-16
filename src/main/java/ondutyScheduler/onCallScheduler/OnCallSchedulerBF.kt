package ondutyScheduler.onCallScheduler

import ondutyScheduler.nonAvailabilities.NonAvailabilitiesICI
import ondutyScheduler.optimizer.OptimizerOutput
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
data class OnCallSchedulerInputTO(
        val starting: LocalDateTime,
        val ending: LocalDateTime,
        val escalationEmployees: List<Long>,
        val normalEmployees: List<Long>,
        val normalDays: List<LocalDate>,
        val holidays: List<LocalDate>,
        val payedNonHolidays: List<LocalDate>
)

interface OnCallSchedulerBCI {
    /**
     * prepares an OnCallSchedulerInputTO by filling out default values and enumerating all days
     */
    fun prepareSchedulerInput(starting: LocalDateTime, ending: LocalDateTime): OnCallSchedulerInputTO

    fun calculateSolution(input: OnCallSchedulerInputTO): OptimizerOutput

    fun publishSolution()
}

@Component
class OnCallSchedulerBF(
        val nonAvailabilities: NonAvailabilitiesICI
): OnCallSchedulerBCI {
    override fun prepareSchedulerInput(starting: LocalDateTime, ending: LocalDateTime): OnCallSchedulerInputTO {
        val allDays = generateSequence(starting.toLocalDate()) {t -> t.plusDays(1)}.takeWhile { d: LocalDate -> d.isBefore(ending.toLocalDate())}
        val normalDays = ArrayList<LocalDate>()
        val holidays = ArrayList<LocalDate>()
        for (day in allDays) {
            if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) {
                holidays.add(day)
            } else {
                normalDays.add(day)
            }
        }
        return OnCallSchedulerInputTO(
                starting,
                ending,
                emptyList(),
                emptyList(),
                normalDays,
                holidays,
                emptyList()
        )
    }

    override fun calculateSolution(input: OnCallSchedulerInputTO): OptimizerOutput {
        TODO("not implemented")
    }

    override fun publishSolution() {
        TODO("not implemented")
    }


}
