package barojob.server.config;

import barojob.server.domain.worker.entity.*;
import barojob.server.domain.jobType.entity.JobType;
import barojob.server.domain.location.entity.Neighborhood;
import barojob.server.domain.worker.repository.WorkerRepository;
import barojob.server.domain.worker.repository.WorkerRequestRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataLoader /* implements CommandLineRunner */ {
//
//    private final WorkerRepository workerRepository;
//    private final WorkerRequestRepository workerRequestRepository;
//    private final EntityManager em;
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        // 1) JobType(1~3)와 Neighborhood(1~3) 마스터 데이터 삽입
//        //    이미 있으면 skip
//        if (em.createQuery("select count(j) from JobType j", Long.class).getSingleResult() < 3) {
//            em.persist(new JobType(null, "청소", BigDecimal.valueOf(10000)));
//            em.persist(new JobType(null, "설치", BigDecimal.valueOf(20000)));
//            em.persist(new JobType(null, "배송", BigDecimal.valueOf(30000)));
//        }
//        if (em.createQuery("select count(n) from Neighborhood n", Long.class).getSingleResult() < 3) {
//            em.persist(Neighborhood.builder()
//                    .neighborhoodName("강남구")
//                    .build());
//            em.persist(Neighborhood.builder()
//                    .neighborhoodName("서초구")
//                    .build());
//            em.persist(Neighborhood.builder()
//                    .neighborhoodName("마포구")
//                    .build());
//        }
//
//
//        Random rnd = new Random();
//
//        // 2) Worker + WorkerRequest 50건 생성
//        for (int i = 1; i <= 50; i++) {
//            // 2-1) Worker(User 상속) 생성
//            Worker w = Worker.builder()
//                    .email("worker" + i + "@example.com")
//                    .nickname("worker" + i)
//                    .password("password")
//                    .name("이름" + i)
//                    .phoneNumber(String.format("010-0000-%04d", i))
//                    .priorityScore(rnd.nextDouble() * 100)
//                    .build();
//            workerRepository.save(w);
//
//            // 2-2) WorkerRequest 생성 (오늘: 2025‑04‑19)
//            WorkerRequest wr = WorkerRequest.builder()
//                    .worker(w)
//                    .requestDate(LocalDate.of(2025, 4, 19))
//                    .build();
//
//            // 2-3) jobTypeId 1~3 중 랜덤으로 프록시 참조 (SELECT 없이)
//            long randomJobTypeId = rnd.nextInt(3) + 1;
//            JobType jtProxy = em.getReference(JobType.class, randomJobTypeId);
//            wr.addJobType(WorkerRequestJobType.builder()
//                    .jobType(jtProxy)
//                    .build());
//
//            // 2-4) neighborhoodId 1~3 중 랜덤으로 프록시 참조 (SELECT 없이)
//            long randomNeighborhoodId = rnd.nextInt(3) + 1;
//            Neighborhood nbProxy = em.getReference(Neighborhood.class, randomNeighborhoodId);
//            wr.addLocation(WorkerRequestLocation.builder()
//                    .neighborhood(nbProxy)
//                    .build());
//
//            // 2-5) 저장 (cascade ALL)
//            workerRequestRepository.save(wr);
//        }
//    }
}