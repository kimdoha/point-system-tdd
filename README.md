## README
- 테스트 가능한 코드와 테스트 코드 작성에 집중, TDD 기반의 요구사항 기능 개발

### 테스트 구현
- [x] 1. 각 기능에 대한 Testable 코드 & 단위 테스트 작성
  - [x] 유저 포인트 조회
    - 유효한 유저 ID로 포인트 정보를 반환한다.
    - 존재하지 않는 ID로 조회하면 해당 ID의 포인트 0인 유저 정보를 생성하여 반환한다.
  - [x] 유저 포인트 충전/사용 내역 조회
    - 유효한 유저 ID로 포인트 히스토리를 조회하면 해당 내역 목록을 반환한다
    - 포인트 내역이 없는 유저의 히스토리를 조회하면 빈 목록을 반환한다
    - 충전과 사용 내역이 모두 포함된 히스토리를 정확히 반환한다
  -[x] 유저 포인트 충전
    - 유효한 유저 ID와 금액으로 충전하면 포인트가 정상적으로 증가한다
    - 충전 성공 시 포인트 히스토리에 내역이 기록된다 (트랜잭션 기록 검증)
  -[x] 유저 포인트 사용
    - 유효한 유저 ID와 금액으로 포인트를 사용하면 잔액이 정상적으로 차감된다
    - 포인트 사용 성공 시 포인트 히스토리에 내역이 기록된다 (트랜잭션 기록 검증)

- [x] 2-1. 각 기능의 예외 케이스를 검증하는 단위 테스트 작성
  - [x] 유저 포인트 조회 / 유저 포인트 충전/사용 내역 조회
  - [x] 유저 포인트 충전 / 유저 포인트 사용
    - 음수 또는 0인 유저 ID로 조회하면 IllegalArgumentException이 발생한다 (userRequest 도메인 모델 책임)
  - [x] 유저 포인트 충전 / 유저 포인트 사용
    - 0 이하의 금액으로 충전 시 IllegalArgumentException이 발생한다
- [x] 2-2. 각 기능의 정책 관련 실패 케이스 단위 테스트 작성 (잔액 부족 시나리오 / 금액 제한 시나리오)
  - 포인트 최대 보유 금액 `200만원` 설정, 포인트 최대 충전/사용 금액 `100만원` 설정
  - [x] 유저 포인트 충전
    - 충전 후 잔액이 200만원을 초과하면 RunTimeException이 발생한다
    - 100만원 초과 금액으로 충전 시 IllegalArgumentException이 발생한다
  - [x] 유저 포인트 사용
    - 잔액보다 많은 금액을 사용하려 할 때 RunTimeException이 발생한다
    - 100만원 초과 금액으로 사용 시 IllegalArgumentException이 발생한다

- [x] 3. 각 기능별 통합 테스트 작성 여부 (심화)
  - `API 요구사항` 요청-응답 잘 반환되는지
    - PATCH `/point/{id}/charge`: 포인트를 충전
    - PATCH `/point/{id}/use`: 포인트를 사용
    - GET `/point/{id}`: 포인트를 조회
    - GET `/point/{id}/histories`: 포인트 내역 조회
  - 메소드 호출 횟수 확인
- [x] 4. 도메인 단위 테스트 (eg. `UserPoint` charge, use 메소드)
  - 충전 및 사용 동작이 제대로 작동하는지 확인
  - 최대 한도 초과나 잔액 부족 시 예외가 제대로 발생하는지 확인

### 동시성 선택 기술: `ConcurrentHashMap` + `ReentrantLock`
  - 선택한 이유: 사용자 별로 독립적인 락을 관리허여 동시에 동일 자원 접근 시 발생할 수 있는 문제 해결
    - 사용자별 독립적 락: 각 사용자 ID에 대해 별도의 락을 생성 해서 서로 다른 사용자 트랜잭션 영향 받지 않음
    - 동시 요청 시 데이터 일관성 보장: 같은 사용자에 대해 충전 및 사용 시 동시 요청이 들어와도 락을 통해 순차 처리되어 계산 오류 방지
    
1. `ConcurrentHashMap` + `ReentrantLock` 구현 방식
  ```kotlin
  val lock = lockMap.computeIfAbsent(id) { ReentrantLock() }
  lock.lock()
  try {
  
  } finally {
    lock.unlock()
  }
  ```
- 장단점
  - 장점
    - 1. 명시적인 락 획득 및 해제 제공 (블로킹 동기화 방식)
    - 2. 락 상태 조회 기능 등 제공
  - 단점
    - 1. 락 해제를 명시적으로 해야함
    - 2. 블로킹 방식으로 성능 저하 이슈
      스레드 컨텍스트 스위칭 오버헤드
        - 락을 획득하지 못한 스레드는 대기 상태로 전환, OS는 이 스레드를 제외하고 다른 스레드를 컨텍스트 스위칭
      락 경쟁 상황 발생 
        - 많은 스레드가 동시에 같은 락을 획득하려 할 때 경쟁 상황 발생
      락 획득 대기 큐 관리
        - ReentrantLock은 대기 중인 스레드를 관리하기 위한 큐 구조를 유지 -> 오버헤드 발생
    - 3. 데드락 가능성 있음
  
2. `synchronized` 구현 비교
     ```kotlin
       @Synchronized fun charge(): UserPoint {}
       synchronized(this) {}
     ```
     - 장점
       - 1. 문법이 간단하고 직관적
       - 2. 자동으로 락 해제(스코프 기반)
     - 단점
       - 스레드 별로 락 발생 (모든 메소드 사용 시 과도한 락 발생)
       - 사용자 별 독립적 락 구현이 복잡함  
  
3. Mutex와 Semaphore
     ``` kotlin
     // mutex
     private val mutex = Mutex()
    
     suspend fun chargePoint(id: Long, amount: Long): UserPoint {
       mutex.withLock {
           ...
       }
     }
    
     // semaphore
     private val semaphore = Semaphore(1)
    
     fun chargePoint(id: Long, amount: Long): UserPoint {
       semaphore.acquire()
       try {
    
       } finally {
          semaphore.release() 
       }
     }
   ```
     - 장점
       1. Mutex: 상호 배제 보장, 코루틴과 함께 효율적
       2. Semaphore: 동시 접근 제한 수 조절 가능
     - 단점
       1. Mutex: 코루틴 컨텍스트 필요
       2. Semaphore: 재진입 불가능
       3. 둘 다 오용 시 데드락 가능성

###  ReentrandLock vs synchronized 비교

  | 특성 | ReentrantLock | synchronized |
  |------|---------------|--------------|
  | 락 획득/해제 | 명시적 (lock/unlock) | 암시적 (블록/메소드 진입/종료) |
  | 타임아웃 지원 | 가능 (tryLock 메소드) | 불가능 |
  | 인터럽트 지원 | 가능 (lockInterruptibly) | 불가능 |
  | 공정성 옵션 | 지원 | 미지원 |
  | 조건 변수 | 여러 개 지원 (Condition) | 하나만 지원 (wait/notify) |
  | 성능 | 고수준 기능으로 약간 느릴 수 있음 | JVM 최적화로 단순 케이스에서 우수 |
  | 유연성 | 높음 (다양한 락 획득 방식) | 낮음 (고정된 방식) |
  | 재진입 | 지원 | 지원 |
  | 사용 복잡성 | 높음 (명시적 해제 필요) | 낮음 (자동 해제) |
