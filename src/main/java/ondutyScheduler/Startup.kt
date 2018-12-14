package ondutyScheduler

import ondutyScheduler.optimizer.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
data class Greeting(val id: Long, val content: String)

@RestController
class GreetingController(
        private val optimizerService: OptimizerService
) {

    val counter = AtomicLong()

    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): OptimizerOutput {

        val e1 = Employee(1,1,1,1, emptySet(), emptySet(),"a")
        val e2 = Employee(1,1,2,4, emptySet(), emptySet(),"b")
        val e3 = Employee(1,3,1,1, emptySet(), emptySet(),"c")

        val input = OptimizerInput(
                setOf(
                        e1,e2,e3
                ),
                setOf(
                        e1,e2
                ),
                listOf(
                        TimeSlot(
                                LocalDateTime.of(2018,12,1,1,0),
                                LocalDateTime.of(2018,12,1,9,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,1,8,0),
                                LocalDateTime.of(2018,12,1,12,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,1,12,0),
                                LocalDateTime.of(2018,12,1,18,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,1,17,0),
                                LocalDateTime.of(2018,12,2,1,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,2,1,0),
                                LocalDateTime.of(2018,12,2,9,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,2,9,0),
                                LocalDateTime.of(2018,12,2,17,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,2,17,0),
                                LocalDateTime.of(2018,12,3,1,0))
                ),
                listOf(
                        TimeSlot(
                                LocalDateTime.of(2018,12,1,8,0),
                                LocalDateTime.of(2018,12,2,8,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,2,8,0),
                                LocalDateTime.of(2018,12,3,8,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,3,8,0),
                                LocalDateTime.of(2018,12,4,8,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,4,8,0),
                                LocalDateTime.of(2018,12,5,8,0)),
                        TimeSlot(
                                LocalDateTime.of(2018,12,5,8,0),
                                LocalDateTime.of(2018,12,6,8,0))
                ))

        val solution = optimizerService.calculateSolution(input)
        return solution
    }

}
