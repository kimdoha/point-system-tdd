package io.hhplus.tdd.point

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.hhplus.tdd.point.model.PointHistoryTestFixture
import io.hhplus.tdd.point.model.UserPointTextFixture
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author Doha Kim
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
    fun `사용자 포인트 조회 - 유저 Id로 요청하면 UserPoint 정보를 반환한다`() {
        // given
        val userId = 1L
        val expectUserPoint = UserPointTextFixture.empty(userId)
        every { pointService.getUserPoint(userId) } returns expectUserPoint

        // when
        val result = mockMvc.perform(
            get("/point/$userId")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
        .andReturn()

        //then
        val responseContent = result.response.contentAsString
        val responseUserPoint = objectMapper.readValue(responseContent, UserPoint::class.java)

        assertEquals(expectUserPoint.id, responseUserPoint.id)
        assertEquals(expectUserPoint.point, responseUserPoint.point)
        assertEquals(expectUserPoint.updateMillis, responseUserPoint.updateMillis)

        verify(exactly = 1) { pointService.getUserPoint(userId) }
    }

    @Test
    fun `포인트 사용내역 조회 - 특정 유저의 포인트 충전 및 이용내역을 조회한다`() {
        //given
        val userId = 1L
        val expectPointHistories = listOf(
            PointHistoryTestFixture.charge(userId = userId),
            PointHistoryTestFixture.charge(userId = userId),
            PointHistoryTestFixture.use(userId = userId),
        )
        every { pointService.getUserPointHistories(userId) } returns expectPointHistories

        // when
        val result = mockMvc.perform(
            get("/point/$userId/histories")
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andReturn()

        // then
        val responseContent = result.response.contentAsString
        val responsePointHistories = objectMapper.readValue(responseContent, Array<PointHistory>::class.java).toList()

        assertIterableEquals(expectPointHistories, responsePointHistories)
        verify(exactly = 1) { pointService.getUserPointHistories(userId) }
    }

    @Test
    fun `포인트 충전 - 특정 유저의 포인트를 충천한다`() {
        // given
        val userId = 1L
        val amount = 1_000L
        val expectUserPoint = UserPointTextFixture.withBalance(userId, amount)
        val expectPointHistories = listOf(
            PointHistoryTestFixture.charge(userId, amount)
        )
        every { pointService.chargeUserPoint(userId, amount) } returns expectUserPoint
        every { pointService.getUserPointHistories(userId) } returns expectPointHistories

        // when
        val result = mockMvc.perform(
            patch("/point/$userId/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andReturn()

        // then
        val responseContent = result.response.contentAsString
        val responseUserPoint = objectMapper.readValue(responseContent, UserPoint::class.java)

        assertEquals(expectUserPoint.id, responseUserPoint.id)
        assertEquals(expectUserPoint.point, responseUserPoint.point)
        assertEquals(expectUserPoint.updateMillis, responseUserPoint.updateMillis)

        assertEquals(expectPointHistories.last().id, userId)
        assertEquals(expectPointHistories.last().type, TransactionType.CHARGE)
        assertEquals(expectPointHistories.last().amount, amount)

        verify(exactly = 1) { pointService.chargeUserPoint(userId, amount) }
    }

    @Test
    fun `포인트 사용 - 특정 유저의 포인트를 사용한다`() {
        // given
        val userId = 1L
        val amount = 1000L
        val expectUserPoint = UserPointTextFixture.empty(userId)
        val expectPointHistories = listOf(
            PointHistoryTestFixture.use(userId = userId, amount = amount),
        )
        every { pointService.useUserPoint(userId, amount) } returns expectUserPoint

        // when
        val result = mockMvc.perform(
            patch("/point/$userId/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(amount))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk)
            .andReturn()


        // then
        val resultContent = result.response.contentAsString
        val responseUserPoint = objectMapper.readValue(resultContent, UserPoint::class.java)

        assertEquals(expectUserPoint.id, responseUserPoint.id)
        assertEquals(expectUserPoint.point, responseUserPoint.point)
        assertEquals(expectUserPoint.updateMillis, responseUserPoint.updateMillis)

        assertEquals(expectPointHistories.last().id, userId)
        assertEquals(expectPointHistories.last().type, TransactionType.USE)
        assertEquals(expectPointHistories.last().amount, amount)

        verify(exactly = 1) { pointService.useUserPoint(userId, amount) }
    }
}
