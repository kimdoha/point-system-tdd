package io.hhplus.tdd.point

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.hhplus.tdd.point.model.PointHistoryTestFixture
import io.hhplus.tdd.point.model.UserPointTextFixture
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author Doha Kim
 */
/**
 * 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
class PointControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var pointService: PointService

    @Test
    fun `포인트 조회 API - 유저 ID로 요청하면 해당 유저의 포인트 정보를 반환한다`() {
        // given
        val userId = 1L
        val expectRandomUserPoint = UserPointTextFixture.random(userId)
        every { pointService.getUserPoint(userId) } returns expectRandomUserPoint

        // when & then
        mockMvc.perform(
            get("/point/$userId")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").exists())
        .andExpect(jsonPath("$.updateMillis").exists())

        verify(exactly = 1) { pointService.getUserPoint(userId) }
    }

    @Test
    fun `포인트 내역 API - 유저 ID로 요청하면 해당 유저의 포인트 사용 내역을 조회한다`() {
        //given
        val userId = 1L
        val expectPointHistories = PointHistoryTestFixture.getChargePointHistories(userId = userId)
        every { pointService.getUserPointHistories(userId) } returns expectPointHistories

        // when & then
        mockMvc.perform(
            get("/point/$userId/histories")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].type").value(TransactionType.CHARGE.name))
            .andExpect(jsonPath("$[0].timeMillis").exists())
        .andReturn()

        verify(exactly = 1) { pointService.getUserPointHistories(userId) }
    }

    @Test
    fun `포인트 충전 API - 유저 ID와 포인트 금액을 요청하면 충전 후 업데이트 된 포인트 정보를 반환한다`() {
        // given
        val userId = 1L
        val amount = 2_000L
        val expectUserPoint = UserPointTextFixture.withBalance(userId, amount)
        val expectChargePointHistory = PointHistoryTestFixture.getChargePointHistories(userId = userId, amount = amount)

        every { pointService.chargeUserPoint(userId, amount) } returns expectUserPoint
        every { pointService.getUserPointHistories(userId) } returns expectChargePointHistory

        // when & then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(amount))
        .andExpect(jsonPath("$.updateMillis").exists())
        .andReturn()

        verify(exactly = 1) { pointService.chargeUserPoint(userId, amount) }
    }

    @Test
    fun `포인트 사용 API - 유저 ID와 포인트 금액을 요청하면 사용 후 업데이트 된 포인트 정보를 반환한다`() {
        // given
        val userId = 1L
        val (amount, originPoint) = Pair(2_000L, 2_000L)
        val originUserPoint = UserPointTextFixture.withBalance(userId, originPoint)
        val expectUserPoint = UserPointTextFixture.use(userId, originUserPoint.point, amount)
        val expectUsePointHistory = PointHistoryTestFixture.getUsePointHistory(userId = userId, amount = amount)

        every { pointService.useUserPoint(userId, amount) } returns expectUserPoint
        every { pointService.getUserPointHistories(userId) } returns expectUsePointHistory

        // when & then
        mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.point").value(expectUserPoint.point))
        .andExpect(jsonPath("$.updateMillis").exists())

        verify(exactly = 1) { pointService.useUserPoint(userId, amount) }
    }
}
