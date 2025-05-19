package barojob.server.domain.worker.repository;

import barojob.server.domain.match.dto.MatchingDataDto;

import java.time.LocalDate;
import java.util.List;

import barojob.server.domain.worker.dto.CursorPagingWorkerRequestDto;
import barojob.server.domain.worker.dto.JTCursorPagingWorkerRequestDto;
import barojob.server.domain.worker.dto.WorkerRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkerRequestRepositoryCustom {
    Page<WorkerRequestDto.ManualMatchingResponse> findWorkerRequestPageByNeighborhoodAndJobType(
            Long neighborhoodId,
            Long jobTypeId,
            Pageable pageable
    );

    List<MatchingDataDto.WorkerInfo> findEligibleWorkerInfoForMatching(LocalDate targetDate);

    List<CursorPagingWorkerRequestDto> findTopRequests(
            List<Long> neighborhoodIds,
            LocalDate requestDate,
            String status,
            int limit,
            Double cursorPriorityScore,
            Long cursorWorkerId
    );

    List<JTCursorPagingWorkerRequestDto> findTopRequestsJT(
            List<Long> neighborhoodIds,
            List<LocalDate> requestDates,
            List<String> status,
            int limit,
            Double cursorPriorityScore,
            Long cursorWorkerId,
            List<Long> jobTypeIds
    );
//    List<WorkerRequestDto.ManualMatchingResponse> findByCursorPagination(
//            Long neighborhoodId,
//            LocalDate requestDate,
//            Double cursorPriority,
//            Long cursorId,
//            int pageSize
//    );
}

