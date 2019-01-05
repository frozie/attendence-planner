package ondutyScheduler

import ondutyScheduler.nonAvailabilities.*
import ondutyScheduler.onCallScheduler.OnCallSchedulerBCI
import ondutyScheduler.onCallScheduler.OnCallSchedulerInputTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
@RestController
class SchedulingController(
        val nonAvailabilitiesBCI: NonAvailabilitiesBCI,
        val onCallSchedulerBCI: OnCallSchedulerBCI
) {

    @GetMapping("/employee/")
    fun allEmployees(): ResponseEntity<List<NamedEmployeeTO>> {
        return ResponseEntity.ok(nonAvailabilitiesBCI.getAllEmployees())
    }

    @GetMapping("/onCallScheduling/prepare")
    fun prepare(@RequestParam("starting") starting: String, @RequestParam("ending") ending: String): OnCallSchedulerInputTO {
        return onCallSchedulerBCI.prepareSchedulerInput(LocalDateTime.parse(starting), LocalDateTime.parse(ending))
    }
}