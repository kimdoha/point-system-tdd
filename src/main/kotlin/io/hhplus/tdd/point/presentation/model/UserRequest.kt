package io.hhplus.tdd.point.presentation.model

/**
 * @author Doha Kim
 */
class UserRequest (
    val id: Long
) {
    @Throws(IllegalArgumentException::class)
    fun validate() {
        if (id <= 0) throw IllegalArgumentException("유효하지 않은 유저 입니다.")
    }
}
