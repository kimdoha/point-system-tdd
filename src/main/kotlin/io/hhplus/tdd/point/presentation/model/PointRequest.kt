package io.hhplus.tdd.point.presentation.model

/**
 * @author Doha Kim
 */
// 공통 검증 로직을 포함한 인터페이스
interface PointRequest {
    val userId: Long
    val amount: Long

    fun validateCommon() {
        if (userId <= 0) throw IllegalArgumentException("유효하지 않은 유저 입니다.")
        if (amount <= 0) throw IllegalArgumentException("포인트 금액이 0보다 커야 합니다.")
    }
}

data class PointChargeRequest(
    override val userId: Long,
    override val amount: Long,
) : PointRequest {

    @Throws(IllegalArgumentException::class)
    fun validate() {
        validateCommon()
        validateForCharge()
    }

    private fun validateForCharge() {
        if (amount > MAX_POINT_AMOUNT) throw IllegalArgumentException("포인트 충전 금액은 최대 100만원 입니다.")
    }

    companion object {
        private const val MAX_POINT_AMOUNT = 1_000_000L
    }
}

data class PointUseRequest(
    override val userId: Long,
    override val amount: Long,
) : PointRequest {

    @Throws(IllegalArgumentException::class)
    fun validate() {
        validateCommon()
        validateForUse()
    }

    private fun validateForUse() {
        if (amount > MAX_POINT_AMOUNT) throw IllegalArgumentException("포인트 사용 금액은 최대 100만원 입니다.")
    }

    companion object {
        private const val MAX_POINT_AMOUNT = 1_000_000L
    }
}
