package ondutyScheduler

import ondutyScheduler.opsgenie.OpsGenieAdapter
import ondutyScheduler.opsgenie.OverrideTimeSlot
import ondutyScheduler.opsgenie.Schedule
import ondutyScheduler.optimizer.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
data class Greeting(val id: Long, val content: String)

@RestController
class GreetingController(
        private val optimizerService: OptimizerService,
        private val opsgenie: OpsGenieAdapter
) {


    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String,
                 @RequestParam(value = "apiKeyOpsGenie") apiKeyOpsGenie: String): OptimizerOutput {
        val e1 = Employee(1, 1, 1, 1, emptySet(), emptySet(), "292f3b08-bdf0-43d0-b452-a442e3d570cb")
        val e2 = Employee(1, 1, 2, 4, emptySet(), emptySet(), "91de6977-2e82-4193-8e97-a3d552329285")
        val e3 = Employee(1, 3, 1, 1, emptySet(), emptySet(), "735b0817-0c3d-47c9-a8d1-ace051327cb7")
        val efz = Employee(1, 1, 1, 1, emptySet(), emptySet(), "158a7481-06eb-4b11-934e-b0a1c5ac51b7")

        val input = OptimizerInput(
                setOf(
                        e1, e2, e3, efz
                ),
                setOf(
                        e1, e2, efz
                ),
                listOf(
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 1, 1, 0),
                                LocalDateTime.of(2019, 1, 1, 9, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 1, 8, 0),
                                LocalDateTime.of(2019, 1, 1, 12, 0),
                                false),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 1, 12, 0),
                                LocalDateTime.of(2019, 1, 1, 18, 0),
                                false),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 1, 17, 0),
                                LocalDateTime.of(2019, 1, 2, 1, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 2, 1, 0),
                                LocalDateTime.of(2019, 1, 2, 9, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 2, 9, 0),
                                LocalDateTime.of(2019, 1, 2, 17, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 2, 17, 0),
                                LocalDateTime.of(2019, 1, 3, 1, 0))
                ),
                listOf(
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 1, 8, 0),
                                LocalDateTime.of(2019, 1, 2, 8, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 2, 8, 0),
                                LocalDateTime.of(2019, 1, 3, 8, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 3, 8, 0),
                                LocalDateTime.of(2019, 1, 4, 8, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 4, 8, 0),
                                LocalDateTime.of(2019, 1, 5, 8, 0)),
                        TimeSlot(
                                LocalDateTime.of(2019, 1, 5, 8, 0),
                                LocalDateTime.of(2019, 1, 6, 8, 0))
                ))

        val solution = optimizerService.calculateSolution(input)
        val (overridesFor24_7, overridesForSupport) = solution.normalSchedule.partition { it.timeSlot.isPayed }
        val schedule24_7 = Schedule("72ab43d8-460a-48d0-bb3d-460bf4769553", mapMappedEmployeeTimeSlotToOverrideTimeSlot(overridesFor24_7))
        val scheduleSupport = Schedule("0b89d36e-bb9f-44fc-868a-008065a953a9", mapMappedEmployeeTimeSlotToOverrideTimeSlot(overridesForSupport))
        val nonImported24_7 = opsgenie.createOverridesForScheduleIfNoConflict(schedule24_7, apiKeyOpsGenie)
        val nonImportedSupport = opsgenie.createOverridesForScheduleIfNoConflict(scheduleSupport, apiKeyOpsGenie)
        return solution
    }

    private fun mapMappedEmployeeTimeSlotToOverrideTimeSlot(mappedEmployeeTimeSlots: Collection<MappedEmployeeToTimeSlot>): Collection<OverrideTimeSlot> = mappedEmployeeTimeSlots.map {
        OverrideTimeSlot(
                it.timeSlot.starting,
                it.timeSlot.ending,
                it.employee.identifier.toString()
        )
    }

}
