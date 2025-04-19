package barojob.server.domain.match.dto;

import java.util.Set;

public class MatchingDataDto {

    public record WorkerInfo(Long workerRequestId, Long workerId, Double priorityScore, Set<Long> neighborhoodIds, Set<Long> jobTypeIds) {}
    public record EmployerDetailInfo(Long detailId, Long requestId, Long employerId, Integer spotsAvailable, Long locationId, Long jobTypeId) {}
}