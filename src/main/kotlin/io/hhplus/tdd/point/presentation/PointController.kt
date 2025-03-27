package io.hhplus.tdd.point.presentation

import io.hhplus.tdd.point.PointService
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.presentation.model.PointChargeRequest
import io.hhplus.tdd.point.presentation.model.PointUseRequest
import io.hhplus.tdd.point.presentation.model.UserRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointService
) {

    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        UserRequest(id).validate()
        return pointService.getUserPoint(id)
    }

    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        UserRequest(id).validate()
        return pointService.getUserPointHistories(id)
    }

    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        PointChargeRequest(id, amount).validate()
        return pointService.chargeUserPoint(id, amount)
    }

    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        PointUseRequest(id, amount).validate()
        return pointService.useUserPoint(id, amount)
    }
}
