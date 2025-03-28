package io.hhplus.tdd.point.integration.concurrent

import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.PointService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * @author Doha Kim
 */
@SpringBootTest
class PointConcurrentTest {

    @Autowired
    private lateinit var userPointTable: UserPointTable

    @Autowired
    private lateinit var pointService: PointService

    @Test
    fun `동시에 100원 충전 요청이 들어오면 오차 없이 200원이 되어야 한다`() {
        // given
        val userId = 1L
        val thread1 = Thread {
            pointService.chargeUserPoint(userId, 100)
        }
        val thread2 = Thread {
            pointService.chargeUserPoint(userId, 100)
        }

        // when
        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        val result = userPointTable.selectById(userId)

        // then
        assertThat(result.point).isEqualTo(200)
    }
}
