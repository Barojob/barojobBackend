package barojob.server.domain.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TestRequestDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class ManualMatchingRequest{
        private Long neighborhoodId;
        private Long jobTypeId;
        private int page;

        public static ManualMatchingRequest of(Long neighborhoodId, Long jobTypeId, int page) {
            return ManualMatchingRequest.builder()
                    .neighborhoodId(neighborhoodId)
                    .jobTypeId(jobTypeId)
                    .page(page)
                    .build();
        }
    }
}
