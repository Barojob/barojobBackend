package barojob.server.domain.match.controller;

import barojob.server.domain.match.dto.MatchingDto;
import barojob.server.domain.match.repository.MatchRepository;
import barojob.server.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class TestMatchController {

    private final MatchService matchService;
    private final MatchRepository matchRepository;

    /**
     * 테스트 데이터 생성 후 매칭 실행 및 결과 반환 API.
     * 기초 데이터(동네, 직종) 자동 생성 시도 포함.
     */
//    @PostMapping("/test-match")
//    public ResponseEntity<MatchingDto.Response> runTestMatch() {
//        log.info("POST /api/match/test-match 요청 수신");
//        try {
//            MatchingDto.Response result = testMatchService.runTestMatching();
//            log.info("테스트 매칭 실행 완료. 결과 반환.");
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            log.error("테스트 매칭 실행 중 오류 발생", e);
//            MatchingDto.Response errorResponse = MatchingDto.Response.builder()
//                    .targetDate(LocalDate.now().plusDays(1))
//                    .totalMatchesMade(0)
//                    .matches(List.of())
//                    .message("테스트 매칭 실행 중 오류 발생: " + e.getMessage())
//                    .build();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//        }
//    }
    @PostMapping("/run")
    public ResponseEntity<MatchingDto.Response> runFullTestMatch() {
        MatchingDto.Response response = matchService.performDailyBatchMatching(LocalDateTime.now().minusDays(1));
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜에 생성된 매칭 결과 조회 API.
     */
    @GetMapping("/results")
    public ResponseEntity<MatchingDto.Response> getMatchResultsByDate(@RequestParam("date") String date) {
        log.info("GET /api/match/results?date={} 요청 수신", date);
        try {
            LocalDate queryDate = LocalDate.parse(date);
            LocalDateTime startOfDay = queryDate.atStartOfDay();
            // TODO: MatchRepository 에 범위 조회 메서드 추가하고 사용하도록 수정 권장
            // 현재는 임시로 특정 시각(startOfDay)의 매칭만 조회
            List<barojob.server.domain.match.entity.Match> matches = matchRepository.findDetailByMatchDatetime(startOfDay);

            List<MatchingDto.MatchInfo> matchInfos = matches.stream()
                    .map(this::mapMatchToMatchInfo)
                    .collect(Collectors.toList());

            MatchingDto.Response response = MatchingDto.Response.builder()
                    .targetDate(queryDate)
                    .totalMatchesMade(matchInfos.size())
                    .matches(matchInfos)
                    .message("조회된 매칭 결과입니다. (조회 기준: " + queryDate + ")")
                    .build();

            log.info("매칭 결과 조회 완료. {}건 반환.", matchInfos.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("매칭 결과 조회 중 오류 발생 (date={})", date, e);
            MatchingDto.Response errorResponse = MatchingDto.Response.builder()
                    .targetDate(LocalDate.parse(date))
                    .totalMatchesMade(0)
                    .matches(List.of())
                    .message("매칭 결과 조회 중 오류 발생: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private MatchingDto.MatchInfo mapMatchToMatchInfo(barojob.server.domain.match.entity.Match match) {
        if (match == null) return null;

        barojob.server.domain.worker.entity.Worker worker = Optional.ofNullable(match.getWorker()).orElseGet(barojob.server.domain.worker.entity.Worker::new);
        barojob.server.domain.employer.entity.EmployerRequestDetail detail = Optional.ofNullable(match.getEmployerRequestDetail()).orElseGet(barojob.server.domain.employer.entity.EmployerRequestDetail::new);
        barojob.server.domain.employer.entity.EmployerRequest request = Optional.ofNullable(detail.getEmployerRequest()).orElseGet(barojob.server.domain.employer.entity.EmployerRequest::new);
        barojob.server.domain.employer.entity.Employer employer = Optional.ofNullable(request.getEmployer()).orElseGet(barojob.server.domain.employer.entity.Employer::new);
        barojob.server.domain.location.entity.Neighborhood neighborhood = Optional.ofNullable(request.getLocationNeighborhood()).orElseGet(barojob.server.domain.location.entity.Neighborhood::new);
        barojob.server.domain.jobType.entity.JobType jobType = Optional.ofNullable(detail.getJobType()).orElseGet(barojob.server.domain.jobType.entity.JobType::new);

        return MatchingDto.MatchInfo.builder()
                .matchId(match.getMatchId())
                .workerId(worker.getId())
                .workerName(worker.getName())
                .workerPhoneNumber(worker.getPhoneNumber())
                .employerId(employer.getId())
                .businessName(employer.getBusinessName())
                .jobTypeId(jobType.getJobTypeId())
                .jobTypeName(jobType.getName())
                .neighborhoodId(neighborhood.getNeighborhoodId())
                .neighborhoodName(neighborhood.getNeighborhoodName())
                .matchDateTime(match.getMatchDatetime())
                .build();
    }
}