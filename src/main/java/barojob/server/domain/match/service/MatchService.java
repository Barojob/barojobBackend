package barojob.server.domain.match.service;

import barojob.server.domain.employer.entity.Employer;
import barojob.server.domain.employer.entity.EmployerRequest;
import barojob.server.domain.employer.entity.EmployerRequestDetail;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.match.dto.MatchDto;
import barojob.server.domain.match.entity.Match;
import barojob.server.domain.match.repository.BatchMatchingRepository;
import barojob.server.domain.worker.entity.Worker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final BatchMatchingRepository batchMatchingRepository;

    @Transactional
    public MatchDto.Response performMatching(LocalDate targetDate) {
        LocalDateTime matchTime = LocalDateTime.now();
        long matchesMadeCount = 0;
        List<Match> createdMatches = List.of();

        try {
            matchesMadeCount = batchMatchingRepository.insertMatchesNative(targetDate, matchTime);

            if (matchesMadeCount > 0) {
                createdMatches = batchMatchingRepository.findDetailByMatchDatetime(matchTime);
                matchesMadeCount = createdMatches.size();
            }

            List<MatchDto.MatchInfo> matchInfos = createdMatches.stream()
                    .map(this::mapMatchToMatchInfo)
                    .collect(Collectors.toList());

            return new MatchDto.Response(targetDate, matchesMadeCount, matchInfos);

        } catch (Exception e) {
            throw new RuntimeException("매칭 프로세스 실패 (날짜: " + targetDate + "): " + e.getMessage(), e);
        }
    }

    private MatchDto.MatchInfo mapMatchToMatchInfo(Match match) {
        Worker matchedWorker = Optional.ofNullable(match.getWorker()).orElseGet(Worker::new);
        EmployerRequestDetail matchedDetail = Optional.ofNullable(match.getEmployerRequestDetail()).orElseGet(EmployerRequestDetail::new);
        EmployerRequest matchedRequest = Optional.ofNullable(matchedDetail.getEmployerRequest()).orElseGet(EmployerRequest::new);
        Employer matchedEmployer = Optional.ofNullable(matchedRequest.getEmployer()).orElseGet(Employer::new);
        Neighborhood matchedNeighborhood = Optional.ofNullable(matchedRequest.getLocationNeighborhood()).orElseGet(Neighborhood::new);
        JobType matchedJobType = Optional.ofNullable(matchedDetail.getJobType()).orElseGet(JobType::new);

        return MatchDto.MatchInfo.builder()
                .matchId(match.getMatchId())
                .workerId(matchedWorker.getId())
                .workerName(matchedWorker.getName())
                .workerPhoneNumber(matchedWorker.getPhoneNumber())
                .employerId(matchedEmployer.getId())
                .businessName(matchedEmployer.getBusinessName())
                .jobTypeId(matchedJobType.getJobTypeId())
                .jobTypeName(matchedJobType.getName())
                .neighborhoodId(matchedNeighborhood.getNeighborhoodId())
                .neighborhoodName(matchedNeighborhood.getNeighborhoodName())
                .matchDateTime(match.getMatchDatetime())
                .build();
    }
}
