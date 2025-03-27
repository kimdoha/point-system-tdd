package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.domain.UserPoint.Companion.MAX_POINT_LIMIT
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Doha Kim
 */
class UserPointTest {

    companion object {
        private const val INITIAL_POINT = 1000L
        private const val INITIAL_POINT_FOR_USE = 5000L
        private const val VALID_AMOUNT = 2000L
        private const val USER_ID = 1L
        private const val ALMOST_MAX_POINT_LIMIT = 1_900_000L
    }

    @Test
    fun `charge - 유효한 금액을 충전하면 포인트가 증가한다`() {
        // given
        val userPoint = UserPoint(USER_ID, INITIAL_POINT, System.currentTimeMillis())

        // when
        val result = userPoint.charge(VALID_AMOUNT)

        // then
        assertThat(result.id).isEqualTo(USER_ID)
        assertThat(result.point).isEqualTo(INITIAL_POINT + VALID_AMOUNT)
        assertThat(result.updateMillis).isGreaterThanOrEqualTo(userPoint.updateMillis)
    }

    @Test
    fun `charge - 최대 한도를 초과하는 금액 충전 시 예외가 발생한다`() {
        // given
        val userPoint = UserPoint(USER_ID, ALMOST_MAX_POINT_LIMIT, System.currentTimeMillis())
        val excessAmount = 100_001L  // 한도를 초과

        // when & then
        assertThatThrownBy { userPoint.charge(excessAmount) }
            .isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `charge - 한도에 딱 맞는 금액을 충전하면 성공한다`() {
        // given
        val userPoint = UserPoint(USER_ID, ALMOST_MAX_POINT_LIMIT, System.currentTimeMillis())
        val exactAmount = 100_000L  // 정확히 한도에 도달

        // when
        val result = userPoint.charge(exactAmount)

        // then
        assertThat(result.point).isEqualTo(MAX_POINT_LIMIT)
    }

    @Test
    fun `use - 유효한 금액을 사용하면 포인트가 차감된다`() {
        // given
        val userPoint = UserPoint(USER_ID, INITIAL_POINT_FOR_USE, System.currentTimeMillis())

        // when
        val result = userPoint.use(VALID_AMOUNT)

        // then
        assertThat(result.id).isEqualTo(USER_ID)
        assertThat(result.point).isEqualTo(INITIAL_POINT_FOR_USE - VALID_AMOUNT)
        assertThat(result.updateMillis).isGreaterThanOrEqualTo(userPoint.updateMillis)
    }

    @Test
    fun `use - 보유 포인트보다 많은 금액 사용 시 예외가 발생한다`() {
        // given
        val userPoint = UserPoint(USER_ID, INITIAL_POINT_FOR_USE, System.currentTimeMillis())
        val excessAmount = INITIAL_POINT_FOR_USE + 1

        // when & then
        assertThatThrownBy { userPoint.use(excessAmount) }
            .isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `use - 보유 포인트와 정확히 동일한 금액 사용은 성공한다`() {
        // given
        val userPoint = UserPoint(USER_ID, INITIAL_POINT_FOR_USE, System.currentTimeMillis())

        // when
        val result = userPoint.use(INITIAL_POINT_FOR_USE)

        // then
        assertThat(result.point).isEqualTo(0)
    }
}
