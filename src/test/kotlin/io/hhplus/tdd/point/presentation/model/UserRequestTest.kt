package io.hhplus.tdd.point.presentation.model

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * @author Doha Kim
 */
class UserRequestTest {

    @Test
    fun `유저 요청 - 유효한 데이터는 검증을 통과한다`() {
        // given
        val request = UserRequest(id = 1L)

        // when & then: 예외가 발생하지 않아야 함
        assertThatCode { request.validate() }.doesNotThrowAnyException()
    }

    @ParameterizedTest(name = "유저 ID가 {0}인 경우 충전 시 IllegalArgumentException이 발생한다")
    @MethodSource("invalidUserIdsProvider")
    fun `유저 요청 - 유효하지 않은 데이터는 예외를 발생시킨다`(userId: Long) {
        // given
        val request = UserRequest(userId)

        // when & then
        assertThatThrownBy { request.validate() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("유효하지 않은 유저")
    }

    companion object {
        @JvmStatic
        fun invalidUserIdsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(0L),
                Arguments.of(-1L),
            )
        }
    }
}
