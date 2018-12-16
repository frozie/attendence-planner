package ondutyScheduler.opsgenie

import ondutyScheduler.util.intersectingTimes
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */

data class Schedule(
        val scheduleIdOpsGenie: String,
        val overrideTimeSlots: Collection<OverrideTimeSlot>
)

data class OverrideTimeSlot(
        val startingAt: LocalDateTime,
        val endingAt: LocalDateTime,
        val employeeIdOpsGenie: String
)

data class OpsGenieUser(
        val id: String,
        val type: String = "user"
)

data class OpsGenieOverride(
        val user: OpsGenieUser,
        val startDate: ZonedDateTime,
        val endDate: ZonedDateTime
)

data class OpsGenieGetOverridesResponse(
        val data: List<OpsGenieOverride>
)

@Component
class OpsGenieAdapter {

    val TIMEZONE = ZoneId.of("Europe/Berlin")

    fun createOverridesForScheduleIfNoConflict(schedule: Schedule, apiKey: String): Collection<OverrideTimeSlot> {
        val client = getPreparedClient(apiKey)
        val currentOverrides = fetchAllOverrides(client, schedule.scheduleIdOpsGenie)

        val (nonOverrideTimeSlots, toOverrideTimeSlot) = schedule.overrideTimeSlots.partition {
            existsOverlappingTimeSlot(currentOverrides, it)
        }
        for (overrideTimeSlot in toOverrideTimeSlot) {
            addOverride(client, overrideTimeSlot, schedule.scheduleIdOpsGenie)
        }
        return nonOverrideTimeSlots
    }

    private fun existsOverlappingTimeSlot(inTimeSlots: Collection<OpsGenieOverride>, probing: OverrideTimeSlot): Boolean {
        for (inTimeSlot in inTimeSlots) {
            if (intersectingTimes(probing.startingAt, probing.endingAt, inTimeSlot.startDate.withZoneSameInstant(TIMEZONE).toLocalDateTime(), inTimeSlot.endDate.withZoneSameInstant(TIMEZONE).toLocalDateTime())) {
                return true
            }
        }
        return false
    }

    private fun getAvailableSchedules(client: WebClient) {
        val data = client.get().uri("/v2/schedules").retrieve().bodyToMono(String::class.java).block()
        println(data)
    }


    private fun addOverride(client: WebClient, override: OverrideTimeSlot, scheduleId: String) {
        val request = OpsGenieOverride(
                OpsGenieUser(override.employeeIdOpsGenie),
                override.startingAt.atZone(TIMEZONE),
                override.endingAt.atZone(TIMEZONE)
        )
        val response = client.post()
                .uri("/v2/schedules/$scheduleId/overrides")
                .body(BodyInserters.fromObject(request))
                .retrieve().bodyToMono(String::class.java).block()
        println(response)
    }

    private fun fetchAllOverrides(client: WebClient, scheduleId: String): List<OpsGenieOverride> {
        val response = client.get().uri("/v2/schedules/$scheduleId/overrides").retrieve().bodyToMono(OpsGenieGetOverridesResponse::class.java).block()
        return response!!.data
    }


    private fun getPreparedClient(apiKey: String): WebClient { //TODO maybe make it injected
        return WebClient.builder()
                .baseUrl("https://api.opsgenie.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "GenieKey $apiKey")
                .build()
    }
}