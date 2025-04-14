package barojob.server.domain.match.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MatchDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private LocalDate targetDate;
        private long totalMatchesMade;
        private List<MatchInfo> matches;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MatchInfo {
        private Long matchId;
        private Long workerId;
        private String workerName;
        private String workerPhoneNumber;
        private Long employerId;
        private String businessName;
        private Long jobTypeId;
        private String jobTypeName;
        private Long neighborhoodId;
        private String neighborhoodName;
        private LocalDateTime matchDateTime;
    }
}
