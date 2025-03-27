package io.hhplus.tdd.point.domain

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    companion object {
        const val MAX_POINT_LIMIT = 2_000_000L
    }
    @Throws(RuntimeException::class)
    fun charge(amount: Long): UserPoint {
        if (this.point + amount > MAX_POINT_LIMIT) throw RuntimeException("포인트 최대 보유 금액은 200만원 입니다.")

        return copy(
            point = this.point + amount,
            updateMillis = System.currentTimeMillis(),
        )
    }

    @Throws(RuntimeException::class)
    fun use(amount: Long): UserPoint {
        if (this.point < amount) throw RuntimeException("포인트 잔고가 부족합니다.")

        return copy(
            point = this.point - amount,
            updateMillis = System.currentTimeMillis(),
        )
    }
}
