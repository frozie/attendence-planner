package ondutyScheduler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
@SpringBootApplication
class OnDutySchedulerApplication {

}

fun main(args: Array<String>) {
    runApplication<OnDutySchedulerApplication>(*args)
}
