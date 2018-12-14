package ondutyScheduler.optimizer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */
internal class TimeSlotTest {

    @Test
    fun intersectOtherCompletelyThis() {
        val other = TimeSlot(
                LocalDateTime.of(2018,12,1,8,0),
                LocalDateTime.of(2018,12,2,8,0))

        val thisSlot = TimeSlot(
                LocalDateTime.of(2018,12,1,12,0),
                LocalDateTime.of(2018,12,1,18,0))

        val actual = thisSlot.intersects(other)

        assertTrue(actual)
    }

    @Test
    fun intersectThisCompletelyOther() {
        val thisSlot = TimeSlot(
                LocalDateTime.of(2018,12,1,8,0),
                LocalDateTime.of(2018,12,2,8,0))

        val other = TimeSlot(
                LocalDateTime.of(2018,12,1,12,0),
                LocalDateTime.of(2018,12,1,18,0))

        val actual = thisSlot.intersects(other)

        assertTrue(actual)
    }

    @Test
    fun intersectSameStart() {
        val thisSlot = TimeSlot(
                LocalDateTime.of(2018,12,1,8,0),
                LocalDateTime.of(2018,12,2,8,0))

        val other = TimeSlot(
                LocalDateTime.of(2018,12,1,8,0),
                LocalDateTime.of(2018,12,1,18,0))

        assertTrue(thisSlot.intersects(other))
        assertTrue(other.intersects(thisSlot))
    }
}