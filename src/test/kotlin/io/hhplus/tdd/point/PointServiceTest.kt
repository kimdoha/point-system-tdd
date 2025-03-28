package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.model.PointHistoryTestFixture
import io.hhplus.tdd.point.model.UserPointTextFixture
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * @author Doha Kim
 */
@ExtendWith(MockKExtension::class)
class PointServiceTest {

    @MockK
    private lateinit var userPointTable: UserPointTable

    @MockK
    private lateinit var pointHistoryTable: PointHistoryTable

    @InjectMockKs
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        pointService = PointService(
            userPointTable,
            pointHistoryTable
        )
    }

    @Test
    fun `유효한 유저 ID로 포인트 정보를 반환한다`() {
        // given: 기존 포인트가 1000인 유저
        val userId = 123L
        val expectPoint = 1_000L
        val updateMillis = System.currentTimeMillis()
        val expectUserPoint = UserPointTextFixture.create(
            userId,
            expectPoint,
            updateMillis,
        )
        every { userPointTable.selectById(userId) } returns expectUserPoint

        // when: 해당 유저 ID로 포인트 조회 시
        val response = pointService.getUserPoint(userId)

        // then: 포인트가 1000인 정보가 반환됨
        assertThat(response.id).isEqualTo(userId)
        assertThat(response.point).isEqualTo(expectPoint)
        assertThat( response.updateMillis).isEqualTo(updateMillis)

        verify(exactly = 1) { userPointTable.selectById(userId) }
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 해당 ID의 포인트 0인 유저 정보를 생성하여 반환한다`() {
        // given: 존재하지 않는 유저 ID
        val nonExistUserId = 999L
        val expectDefaultUserPoint = UserPointTextFixture.empty(nonExistUserId)
        every { userPointTable.selectById(nonExistUserId) } returns expectDefaultUserPoint

        // when: 해당 ID로 포인트 조회 시
        val response = pointService.getUserPoint(nonExistUserId)

        // then: ID는 동일하고 포인트가 0인 새로운 유저 정보 반환됨
        assertThat(response.id).isEqualTo(nonExistUserId)
        assertThat(response.point).isEqualTo(0)
        assertThat(response.updateMillis).isNotNull()

        verify(exactly = 1) { userPointTable.selectById(nonExistUserId) }
    }

    @Test
    fun `유효한 유저 ID로 포인트 히스토리를 조회하면 해당 내역 목록을 반환한다`() {
        // given: 포인트 내역이 있는 유저
        val userId = 123L
        val expectPointHistories = listOf(
            PointHistoryTestFixture.charge(userId = userId, amount = 2_000L),
            PointHistoryTestFixture.use(userId = userId, amount = 1_000L),
        )
        every { pointHistoryTable.selectAllByUserId(userId) } returns expectPointHistories

        // when: 해당 유저의 히스토리 조회 시
        val response = pointService.getUserPointHistories(userId)

        // then: 저장된 내역 목록이 정확히 반환된다
        assertThat(response)
            .hasSize(expectPointHistories.size)
            .containsExactlyInAnyOrderElementsOf(expectPointHistories)  // 순서 무관하게 동일 요소 포함 확인
    }

    @Test
    fun `포인트 내역이 없는 유저의 히스토리를 조회하면 빈 목록을 반환한다`() {
        // given: 포인트 내역이 없는 유저
        val userId = 999L
        every { pointHistoryTable.selectAllByUserId(userId) } returns emptyList()

        // when: 해당 유저의 히스토리 조회 시
        val response = pointService.getUserPointHistories(userId)

        // then: 빈 목록이 반환된다
        assertThat(response).isEmpty()
        verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId)}
    }

    @Test
    fun `충전과 사용 내역이 모두 포함된 히스토리를 정확히 반환한다`() {
        // given: 충전과 사용 내역이 모두 있는 유저
        val userId = 123L
        val expectPointHistories = listOf(
            PointHistoryTestFixture.charge(userId = userId, amount = 2_000L),
            PointHistoryTestFixture.use(userId = userId, amount = 1_000L),
        ) // TODO 코드 중복됨
        every { pointHistoryTable.selectAllByUserId(userId) } returns expectPointHistories

        // when: 해당 유저의 히스토리 조회 시
        val response = pointService.getUserPointHistories(userId)

        // then: 충전과 사용 내역이 모두 포함된 목록이 반환된다
        // 1. 전체 응답이 기대한 데이터와 일치하는지 확인
        assertThat(response).isEqualTo(expectPointHistories)

        // 2. 충전과 사용 타입이 모두 포함되어 있는지 확인
        assertThat(response.map { it.type }).containsExactlyInAnyOrderElementsOf(TransactionType.entries)

        // 3. 각 타입별 금액이 정확한지 추가 검증
        val chargeHistory = response.find { it.type == TransactionType.CHARGE }!!
        val useHistory = response.find { it.type == TransactionType.USE }!!

        assertThat(chargeHistory.amount).isEqualTo(2_000L)
        assertThat(useHistory.amount).isEqualTo(1_000L)

        verify(exactly = 1) { pointHistoryTable.selectAllByUserId(userId) }
    }

    @Test
    fun `유효한 유저 ID와 금액으로 충전하면 포인트가 정상적으로 증가한다`() {
        // given: 기존 포인트가 있는 유저와 충전 금액
        val userId = 123L
        val chargePointAmount = 1_000L

        // 초기 유저 포인트 상태 && 업데이트 포인트 상태
        val expectUserPoint = UserPointTextFixture.empty(userId)
        val expectUserUpdatePoint = expectUserPoint.charge(chargePointAmount)

        // 업데이트 히스토리 상태
        val expectPointUpdateHistory = PointHistoryTestFixture.charge(userId, chargePointAmount)

        every { userPointTable.selectById(userId) } returns expectUserPoint
        every { pointHistoryTable.insert(userId, chargePointAmount, TransactionType.CHARGE, any()) } returns expectPointUpdateHistory
        every { userPointTable.insertOrUpdate(userId, chargePointAmount) } returns expectUserUpdatePoint

        // when: 포인트 충전 시
        val response = pointService.chargeUserPoint(userId, chargePointAmount)

        // then: 포인트가 정확히 증가하고 충전 내역이 기록됨
        assertThat(response.id).isEqualTo(userId)
        assertThat(response.point).isEqualTo(expectUserPoint.point + chargePointAmount)
        assertThat(response.updateMillis).isNotNull()

        verify(exactly = 1) { userPointTable.selectById(userId) }
        verify(exactly = 1) { pointHistoryTable.insert(userId, chargePointAmount, TransactionType.CHARGE, any()) }
        verify(exactly = 1) { userPointTable.insertOrUpdate(userId, chargePointAmount) }
    }

    @Test
    fun `100만원 초과 금액으로 충전 시 RuntimeException이 발생한다`() {
        // given: 유효한 유저 ID와 100만원 초과 충전 금액
        // when & then: 충전 시도 시 예외 발생

        assertThatThrownBy {
            val exceedMaximumChargeAmount = 1_000_001L
            pointService.chargeUserPoint(1L, exceedMaximumChargeAmount)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun `충전 후 잔액이 200만원을 초과하면 IllegalArgumentException이 발생한다`() {
        // given: 높은 잔액을 가진 유저와 한도 초과 충전 금액
        // when & then: 충전 시도 시 예외 발생
        val userId = 123L
        val chargePointAmount = 200_000L
        val expectUserPoint = UserPointTextFixture.almostFull(userId)
        every { userPointTable.selectById(userId) } returns expectUserPoint

        assertThatThrownBy {
            pointService.chargeUserPoint(userId, chargePointAmount)
        }.isInstanceOf(RuntimeException::class.java)

        verify(exactly = 1) { userPointTable.selectById(userId) }
        verify(exactly = 0) { pointHistoryTable.insert(userId, chargePointAmount, TransactionType.CHARGE, any()) }
        verify(exactly = 0) { userPointTable.insertOrUpdate(userId, chargePointAmount) }
    }

    @Test
    fun `충전 성공 시 포인트 히스토리에 내역이 기록된다`() {
        // given: 유효한 유저 ID와 충전 금액
        val userId = 123L
        val chargePointAmount = 200_000L
        val updateMillis = System.currentTimeMillis()

        val expectUserPoint = UserPointTextFixture.empty(userId, updateMillis)
        val expectUserUpdatePoint = expectUserPoint.charge(chargePointAmount)
        val expectPointHistory = PointHistoryTestFixture.charge(userId, chargePointAmount, updateMillis)

        every { userPointTable.selectById(userId) } returns expectUserPoint
        every { pointHistoryTable.insert(userId, chargePointAmount, TransactionType.CHARGE, any()) } returns expectPointHistory
        every { userPointTable.insertOrUpdate(userId, chargePointAmount) } returns expectUserUpdatePoint

        // when: 포인트 충전 시
        pointService.chargeUserPoint(userId, chargePointAmount)

        // then: 포인트 히스토리 테이블에 충전 내역이 정확히 기록됨
        verify(exactly = 1) {
            pointHistoryTable.insert(userId, chargePointAmount, TransactionType.CHARGE, any())
        }
    }

    @Test
    fun `유효한 유저 ID와 금액으로 포인트를 사용하면 잔액이 정상적으로 차감된다`() {
        // given: 충분한 포인트를 가진 유저와 사용 금액
        val userId = 123L
        val usePointAmount = 1_000L
        val updateMillis = System.currentTimeMillis()

        val userPoint = UserPointTextFixture.almostFull(userId, updateMillis)
        val expectUserUpdatePoint = userPoint.use(usePointAmount)
        val expectUserUpdatePointHistory = PointHistoryTestFixture.use(userId, usePointAmount, updateMillis)

        every { userPointTable.selectById(userId) } returns userPoint
        every { pointHistoryTable.insert(userId, usePointAmount, TransactionType.USE, any()) } returns expectUserUpdatePointHistory
        every { userPointTable.insertOrUpdate(userId, userPoint.point - usePointAmount) } returns expectUserUpdatePoint

        // when: 포인트 사용 시
        val response = pointService.useUserPoint(userId, usePointAmount)

        // then: 포인트가 정확히 차감되고 사용 내역이 기록됨
        assertThat(response.id).isEqualTo(userId)
        assertThat(response.point).isEqualTo(userPoint.point - usePointAmount)
        assertThat(response.updateMillis).isNotNull()

        verify(exactly = 1) { userPointTable.selectById(userId) }
        verify(exactly = 1) { pointHistoryTable.insert(userId, usePointAmount, TransactionType.USE, any()) }
        verify(exactly = 1) { userPointTable.insertOrUpdate(userId, expectUserUpdatePoint.point) }
    }

    @Test
    fun `잔액보다 많은 금액을 사용하려 할 때 IllegalArgumentException이 발생한다`() {
        // given: 유저의 잔액보다 큰 사용 금액
        // when & then: 사용 시도 시 예외 발생

        val userId = 123L
        val usePointAmount = 200_000L
        val expectUserPoint = UserPointTextFixture.almostEmpty(userId)
        every { userPointTable.selectById(userId) } returns expectUserPoint

        assertThatThrownBy {
            pointService.useUserPoint(userId, usePointAmount)
        }.isInstanceOf(RuntimeException::class.java)

        verify(exactly = 1) { userPointTable.selectById(userId) }
        verify(exactly = 0) { pointHistoryTable.insert(userId, usePointAmount, TransactionType.USE, any()) }
        verify(exactly = 0) { userPointTable.insertOrUpdate(userId, usePointAmount) }
    }

    @Test
    fun `포인트 사용 성공 시 포인트 히스토리에 내역이 기록된다`() {
        // given: 유효한 유저 ID와 사용 금액
        val userId = 123L
        val usePointAmount = 300_000L
        val updateMillis = System.currentTimeMillis()

        val expectUserPoint = UserPointTextFixture.full(userId, updateMillis)
        val expectUserUpdatePoint = expectUserPoint.use(usePointAmount)
        val expectPointHistory = PointHistoryTestFixture.use(userId, usePointAmount, updateMillis)

        every { userPointTable.selectById(userId) } returns expectUserPoint
        every { pointHistoryTable.insert(userId, usePointAmount, TransactionType.USE,  any()) } returns expectPointHistory
        every { userPointTable.insertOrUpdate(userId, expectUserPoint.point - usePointAmount) } returns expectUserUpdatePoint

        // when: 포인트 사용 시
        pointService.useUserPoint(userId, usePointAmount)

        // then: 포인트 히스토리 테이블에 사용 내역이 정확히 기록됨
        verify(exactly = 1) {
            pointHistoryTable.insert(userId, usePointAmount, TransactionType.USE, any())
        }
    }
}
