package ondutyScheduler.util

import java.time.LocalDateTime

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
fun intersectingTimes(startingA: LocalDateTime, endingA: LocalDateTime, startingB: LocalDateTime, endingB: LocalDateTime): Boolean {
    return intersectingTimesWithA(startingA, endingA, startingB, endingB)
            || intersectingTimesWithA(startingB, endingB, startingA, endingA)
            || startingA == startingB && endingA == endingB
}

private fun intersectingTimesWithA(startingA: LocalDateTime, endingA: LocalDateTime, startingB: LocalDateTime, endingB: LocalDateTime): Boolean {
    return startingA.isAfter(startingB) && startingA.isBefore(endingB) || endingA.isAfter(startingB) && endingA.isBefore(endingB)
}