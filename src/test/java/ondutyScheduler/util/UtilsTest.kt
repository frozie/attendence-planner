package ondutyScheduler.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
internal class UtilsTest {
    @Test
    fun intersectCompletely() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 1, 12, 0)
        val endingB = LocalDateTime.of(2018, 12, 1, 18, 0)

        assertTrue(intersectingTimes(startingA, endingA, startingB, endingB))
        assertTrue(intersectingTimes(startingB, endingB, startingA, endingA))
    }

    @Test
    fun intersectExactly() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingB = LocalDateTime.of(2018, 12, 2, 8, 0)

        assertTrue(intersectingTimes(startingA, endingA, startingB, endingB))
        assertTrue(intersectingTimes(startingB, endingB, startingA, endingA))
    }


    @Test
    fun intersectSameStart() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingB = LocalDateTime.of(2018, 12, 1, 18, 0)

        assertTrue(intersectingTimes(startingA, endingA, startingB, endingB))
        assertTrue(intersectingTimes(startingB, endingB, startingA, endingA))
    }

    @Test
    fun intersectSameEnd() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 1, 12, 0)
        val endingB = LocalDateTime.of(2018, 12, 2, 8, 0)

        assertTrue(intersectingTimes(startingA, endingA, startingB, endingB))
        assertTrue(intersectingTimes(startingB, endingB, startingA, endingA))
    }

    @Test
    fun noIntersectContinuousAtEnd() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 2, 8, 0)
        val endingB = LocalDateTime.of(2018, 12, 2, 12, 0)

        assertFalse(intersectingTimes(startingA, endingA, startingB, endingB))
        assertFalse(intersectingTimes(startingB, endingB, startingA, endingA))
    }

    @Test
    fun noIntersectContinuousAtStart() {
        val startingA = LocalDateTime.of(2018, 12, 1, 8, 0)
        val endingA = LocalDateTime.of(2018, 12, 2, 8, 0)

        val startingB = LocalDateTime.of(2018, 12, 1, 1, 0)
        val endingB = LocalDateTime.of(2018, 12, 1, 8, 0)

        assertFalse(intersectingTimes(startingA, endingA, startingB, endingB))
        assertFalse(intersectingTimes(startingB, endingB, startingA, endingA))
    }
}