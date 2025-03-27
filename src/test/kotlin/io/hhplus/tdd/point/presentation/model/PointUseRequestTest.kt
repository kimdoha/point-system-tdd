package io.hhplus.tdd.point.presentation.model

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * @author Doha Kim
 */
class PointUseRequestTest {

    @Test
    fun `포인트 사용 요청 - 유효한 데이터는 검증을 통과한다`() {
        // given
        val request = PointUseRequest(userId = 1L, amount = 1000L)

        // when & then: 예외가 발생하지 않아야 함
        assertThatCode { request.validate() }.doesNotThrowAnyException()
    }


    @ParameterizedTest
    @MethodSource("invalidUseRequestsProvider")
    fun `포인트 사용 요청 - 유효하지 않은 데이터는 예외를 발생시킨다`(userId: Long, amount: Long) { // given
        val request = PointUseRequest(userId, amount)

        // when & then
        assertThatThrownBy { request.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    companion object {
        @JvmStatic
        fun invalidUseRequestsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0L, 1000L),
                Arguments.of(-1L, 1000L),
                Arguments.of(1L, 0L),
                Arguments.of(1L, -100L),
                Arguments.of(1L, 1_000_001L)
            )
        }
    }
}
