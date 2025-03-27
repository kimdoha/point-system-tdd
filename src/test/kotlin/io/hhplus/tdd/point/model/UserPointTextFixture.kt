package io.hhplus.tdd.point.model

import io.hhplus.tdd.point.UserPoint
import kotlin.random.Random

/**
 * @author Doha Kim
 */
object UserPointTextFixture {
    fun empty(
        id: Long,
        updateMillis: Long = System.currentTimeMillis(),
    ) = UserPoint(
        id = id,
        point = 0,
        updateMillis = updateMillis,
    )

    fun random(
        id: Long,
    ) = UserPoint(
        id = id,
        point = Random.nextLong(0, MAX_POINT_AMOUNT + 1),
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

    fun use(
        id: Long,
        originPoint: Long,
        usePoint: Long,
        updateMillis: Long = System.currentTimeMillis(),
    ) = UserPoint(
        id = id,
        point = originPoint - usePoint,
        updateMillis = updateMillis,
    )

    private const val MAX_POINT_AMOUNT = 1_000_000L

}
