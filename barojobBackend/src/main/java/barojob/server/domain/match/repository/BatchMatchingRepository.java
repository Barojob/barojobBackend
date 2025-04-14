package barojob.server.domain.match.repository;

import barojob.server.domain.match.entity.Match;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchMatchingRepository extends JpaRepository<Match, Long> {

    /**
     * STEP 1: 최종 선택된 매칭 결과를 matches 테이블에 INSERT
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO matches (employer_request_detail_id, worker_request_id, worker_id, match_datetime) 
        WITH EligibleWorkers AS (
            SELECT wr.worker_request_id, wr.worker_id, w.priority_score
            FROM worker_requests wr JOIN users w ON wr.worker_id = w.id 
            WHERE wr.request_date = :targetDate AND wr.status IN ('PENDING', 'PARTIALLY_MATCHED')
        ), EligibleWorkerLocations AS (
            SELECT ew.worker_request_id, wrl.neighborhood_id -- Neighborhood ID 사용
            FROM EligibleWorkers ew JOIN worker_request_locations wrl ON ew.worker_request_id = wrl.worker_request_id
        ), EligibleWorkerJobTypes AS (
            SELECT ew.worker_request_id, wrjt.job_type_id
            FROM EligibleWorkers ew JOIN worker_request_job_types wrjt ON ew.worker_request_id = wrjt.worker_request_id
        ), EligibleJobDetails AS (
            SELECT er.request_id AS employer_request_id, erd.request_detail_id, er.location_neighborhood_id, 
                   erd.job_type_id, (erd.required_count - erd.matched_count) AS spots_available
            FROM employer_requests er JOIN employer_request_details erd ON er.request_id = erd.request_id
            WHERE er.request_date = :targetDate AND er.status IN ('PENDING', 'PROCESSING', 'PARTIALLY_MATCHED')
              AND erd.required_count > erd.matched_count
        ), PotentialMatches AS (
             SELECT ew.worker_request_id, ew.worker_id, ew.priority_score, ejd.request_detail_id, ejd.spots_available
             FROM EligibleJobDetails ejd
             JOIN EligibleWorkerLocations ewl ON ejd.location_neighborhood_id = ewl.neighborhood_id 
             JOIN EligibleWorkerJobTypes ewjt ON ewl.worker_request_id = ewjt.worker_request_id AND ejd.job_type_id = ewjt.job_type_id
             JOIN EligibleWorkers ew ON ewjt.worker_request_id = ew.worker_request_id
             WHERE NOT EXISTS (
                SELECT 1 FROM matches m JOIN users u_match ON m.worker_id = u_match.id 
                WHERE u_match.id = ew.worker_id AND DATE(m.match_datetime) = :targetDate 
             )
        ), RankedWorkersForJobs AS (
             SELECT pm.*, ROW_NUMBER() OVER(PARTITION BY pm.request_detail_id ORDER BY pm.priority_score DESC, RAND()) as rn_job
             FROM PotentialMatches pm
        ), SelectedWorkersForJobs AS (
             SELECT rwj.*
             FROM RankedWorkersForJobs rwj WHERE rwj.rn_job <= rwj.spots_available
        ), FinalMatchSelection AS (
             SELECT swj.*, ROW_NUMBER() OVER(PARTITION BY swj.worker_id ORDER BY swj.priority_score DESC, RAND()) as rn_worker
             FROM SelectedWorkersForJobs swj
        )
        SELECT fms.request_detail_id, fms.worker_request_id, fms.worker_id, :matchTime 
        FROM FinalMatchSelection fms
        WHERE fms.rn_worker = 1
        """, nativeQuery = true)
    int insertMatchesNative(@Param("targetDate") LocalDate targetDate, @Param("matchTime") LocalDateTime matchTime);


    /**
     * STEP 2: EmployerRequestDetail 업데이트
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE employer_request_details erd
        JOIN (
            SELECT employer_request_detail_id, COUNT(*) as newly_matched_count
            FROM matches
            WHERE match_datetime = :matchTime
            GROUP BY employer_request_detail_id
        ) AS NewMatches ON erd.request_detail_id = NewMatches.employer_request_detail_id
        SET erd.matched_count = erd.matched_count + NewMatches.newly_matched_count;
        """, nativeQuery = true)
    int updateEmployerRequestDetailsNative(@Param("matchTime") LocalDateTime matchTime);

    /**
     * STEP 3: WorkerRequest 상태 업데이트
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE worker_requests wr
        JOIN (
            SELECT DISTINCT worker_request_id
            FROM matches
            WHERE match_datetime = :matchTime
        ) AS MatchedWorkers ON wr.worker_request_id = MatchedWorkers.worker_request_id
        SET wr.status = 'FULLY_MATCHED'
        WHERE wr.status NOT IN ('CLOSED', 'CANCELLED');
        """, nativeQuery = true)
    int updateWorkerRequestsNative(@Param("matchTime") LocalDateTime matchTime);

    /**
     * STEP 4: EmployerRequest 상태 업데이트
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE employer_requests er
        JOIN (
            SELECT erd.request_id, SUM(erd.required_count) as total_required, SUM(erd.matched_count) as total_matched
            FROM employer_request_details erd
            WHERE erd.request_id IN (
                SELECT DISTINCT erd_inner.request_id
                FROM employer_request_details erd_inner
                JOIN matches m ON erd_inner.request_detail_id = m.employer_request_detail_id
                WHERE m.match_datetime = :matchTime
            )
            GROUP BY erd.request_id
        ) AS DetailStatus ON er.request_id = DetailStatus.request_id
        SET er.status = CASE
                            WHEN DetailStatus.total_matched >= DetailStatus.total_required THEN 'FULLY_MATCHED'
                            WHEN DetailStatus.total_matched > 0 THEN 'PARTIALLY_MATCHED'
                            ELSE er.status
                        END
        WHERE er.status NOT IN ('CLOSED', 'CANCELLED', 'FULLY_MATCHED');
        """, nativeQuery = true)
    int updateEmployerRequestsNative(@Param("matchTime") LocalDateTime matchTime);


    @EntityGraph(attributePaths = {"worker",
            "employerRequestDetail.employerRequest.employer",
            "employerRequestDetail.employerRequest.locationNeighborhood",
            "employerRequestDetail.jobType"})
    List<Match> findDetailByMatchDatetime(@Param("matchTime") LocalDateTime matchTime);

}
