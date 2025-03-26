package barojob.server.system.exception.model;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    GLOBAL_BAD_REQUEST(400, "올바르지 않은 요청입니다."),
    GLOBAL_NOT_FOUND(404, "요청한 사항을 찾을 수 없습니다."),
    GLOBAL_ALREADY_EXIST(400, "요청의 대상이 이미 존재합니다."),
    GLOBAL_METHOD_NOT_ALLOWED(405, "허용되지 않는 Method 입니다."),
    FAILED_TO_REMOVE_AVAILABLE_ROOM(500, "사용 가능한 방을 제거하는데 실패했습니다."),
    USER_NOT_FOUND(400, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_IN_ROOM(400, "이미 방에 참여중인 사용자입니다."),
    USER_NOT_IN_ROOM(400, "사용자가 방에 참여중이 아닙니다."),
    NOT_ROOM_OWNER(400, "방의 소유자가 아닙니다."),

    INTERNAL_SERVER_ERROR(500, "서버 내부 오류입니다."),
    AUTH_USER_NOT_FOUND(400, "사용자를 찾을 수 없습니다."),
    AUTH_BAD_CREDENTIALS(400, "비밀번호가 일치하지 않습니다."),
    GLOBAL_UNAUTHORIZED(401, "인증되지 않은 사용자입니다."),
    ROOM_FULL(400, "방이 꽉 찼습니다."),
    ROOM_NOT_FOUND(400, "방을 찾을 수 없습니다."),
    INVALID_ROOM_NUMBER(400, "올바르지 않은 방 번호입니다."),
    NO_AVAILABLE_ROOM(400, "사용 가능한 방이 없습니다."),
    NO_ROOMS(400, "방이 존재하지 않습니다."),
    ROOM_ALREADY_JOINED(400, "이미 참여중인 방입니다."),
    AUTH_ALREADY_LOGGED_IN(400, "이미 로그인 되어있습니다."),
    LOCK_INTERRUPTED(500, "Lock이 중단되었습니다."),
    LOCK_ACQUIRE_FAILED(500, "Lock을 얻는데 실패했습니다."),

    USER_ALREADY_EXIST(400, "이미 존재하는 사용자입니다.");



    private final int statusCode;
    private final String message;


}
