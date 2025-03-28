package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * @author Doha Kim
 */
@Service
class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable,
) {
    private val lockMap: ConcurrentHashMap<Long, ReentrantLock> = ConcurrentHashMap()

    fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    fun getUserPointHistories(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    fun chargeUserPoint(id: Long, amount: Long): UserPoint {
        val lock = lockMap.computeIfAbsent(id) { ReentrantLock() }
        lock.lock()
        try {
            val userPoint = getUserPoint(id)
            val updatedUserPoint = userPoint.charge(amount)

            pointHistoryTable.insert(
                id = userPoint.id,
                amount = amount,
                transactionType = TransactionType.CHARGE,
                updateMillis = userPoint.updateMillis,
            )

            return userPointTable.insertOrUpdate(id, updatedUserPoint.point)
        } finally {
            lock.unlock()
        }
    }

    fun useUserPoint(id: Long, amount: Long): UserPoint {
        val lock = lockMap.computeIfAbsent(id) { ReentrantLock() }
        lock.lock()
        try {
            val userPoint = getUserPoint(id)
            val updatedUserPoint = userPoint.use(amount)

            pointHistoryTable.insert(
                id = userPoint.id,
                amount = amount,
                transactionType = TransactionType.USE,
                updateMillis = userPoint.updateMillis,
            )

            return userPointTable.insertOrUpdate(id, updatedUserPoint.point)
        } finally {
            lock.unlock()
        }

    }
}
