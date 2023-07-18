package skywolf46.atmospherereentry.netsync.api

enum class NetSyncStrategy {
    /**
     * 지연된 네트워크 동기화
     *
     * LAZY 타입의 동기화 전략은 스트림을 점유하지 않으며, 각 계산마다 네트워크 서버에 동기화를 요청합니다.
     * 더 정확하며, 동기화에 문제가 생기지 않을 확률이 높지만 부작용으로 연산 시간이 대폭 증가합니다.
     *
     * 예를 들어, A 인스턴스 데이터 동기화를 한다고 가정합니다.
     * 호스트 -->                      데이터 동기화, 값 반환 -->                    --> 갱신 완료 응답
     * 사용자 --> 데이터 동기화 요청 -->                          값 처리, 값 갱신 요청
     *
     * 이러한 플로우를 거치기 때문에, 네트워크 지연 시간에 따라 지연이 발생할 수 있습니다.
     */
    LAZY,

    /**
     * 공유된 네트워크 동기화
     *
     * SHARED 타입의 동기화 전략은 스트림을 점유하며, BROADCAST 요청이 올때마다 값을 캐싱합니다.
     * 일반적으로 값의 동기화는 직접적으로 이루어지지 않으며, 변수의 값이 동기화되었을떄 요청을 보냅니다.
     *
     * 동기화에 문제가 발생할 수 있으나, 단일 요청만으로 이루어지기 때문에 빠른 속도를 가집니다.
     * SHARED 타입은 사용에 주의를 요합니다.
     */
    SHARED
}