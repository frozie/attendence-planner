package ondutyScheduler

import ondutyScheduler.nonAvailabilities.EmployeePageTO
import ondutyScheduler.nonAvailabilities.NonAvailabilitiesBCI
import ondutyScheduler.nonAvailabilities.NonAvailabilityNoticeTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
@RestController
class EmployeeController(
        val nonAvailabilitiesBCI: NonAvailabilitiesBCI
) {

    @GetMapping("/employee/{employeeId}")
    fun employeePage(@PathVariable("employeeId") employeeId: Long): ResponseEntity<EmployeePageTO> {
        val employeePage = nonAvailabilitiesBCI.employeePage(employeeId)
        return ResponseEntity.of(employeePage)
    }

    @PutMapping("/employee/{employeeId}/nonAvailabilityNotices")
    fun addNonAvailabilityNotice(
            @PathVariable("employeeId") employeeId: Long,
            @RequestBody nonAvailabilityNoticeTO: NonAvailabilityNoticeTO): ResponseEntity<List<NonAvailabilityNoticeTO>> {
        return ResponseEntity.of(nonAvailabilitiesBCI.addNonAvailabilityNotice(employeeId, nonAvailabilityNoticeTO))
    }

    @DeleteMapping("employee/{employeeId}/nonAvailabilityNotices/{id}")
    fun deleteNonAvailabilityNotice(@PathVariable("employeeId") employeeId: Long, @PathVariable("id") nonAvailabilityNoticeId: Long): ResponseEntity<List<NonAvailabilityNoticeTO>> {
        return ResponseEntity.of(nonAvailabilitiesBCI.deleteNonAvailabilityNotice(employeeId, nonAvailabilityNoticeId))
    }
}