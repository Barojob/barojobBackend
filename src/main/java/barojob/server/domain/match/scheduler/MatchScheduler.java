package barojob.server.domain.match.scheduler;

import barojob.server.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchScheduler {

    private final MatchService matchService;

    /**
     * 매일 18시 00분 00초에 자동으로 배치 매칭 실행 (한국 시간 기준)
     */
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void runDailyMatchingJob() {
        log.info("===== 일일 자동 배치 매칭 시작 =====");
        try {
            matchService.performDailyBatchMatching(LocalDateTime.now());

            log.info("===== 일일 자동 배치 매칭 성공적으로 완료 =====");
        } catch (Exception e) {
            log.error("!!!!! 일일 자동 배치 매칭 중 심각한 오류 발생 !!!!!", e);
        }
    }
}