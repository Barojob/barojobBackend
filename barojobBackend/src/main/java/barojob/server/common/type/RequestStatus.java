package barojob.server.common.type;

public enum RequestStatus {
    PENDING,      // 대기중
    PROCESSING,   // 처리중
    PARTIALLY_MATCHED, // 부분 매칭
    FULLY_MATCHED,     // 완전 매칭
    CLOSED,       // 마감
    CANCELLED     // 취소
}