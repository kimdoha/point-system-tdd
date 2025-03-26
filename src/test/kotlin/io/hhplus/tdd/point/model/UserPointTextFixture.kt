package io.hhplus.tdd.point.model

import io.hhplus.tdd.point.UserPoint

/**
 * @author Doha Kim
 */
object UserPointTextFixture {
    fun empty(
        id: Long,
    ) = UserPoint(
        id = id,
        point = 0,
        updateMillis = System.currentTimeMillis(),
    )

    fun withBalance(
        id: Long,
        point: Long,
    ) = UserPoint(
        id = id,
        point = point,
        updateMillis = System.currentTimeMillis(),
    )
}
