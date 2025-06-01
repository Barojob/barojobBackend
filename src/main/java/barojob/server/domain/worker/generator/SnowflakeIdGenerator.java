package barojob.server.domain.worker.generator;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SnowflakeIdGenerator {

    // 2025-01-01 00:00:00 UTC 기준 밀리초 (예시)
    private final long epoch = 1704067200000L;

    // 데이터센터 ID (0~31)
    private long datacenterId;
    // 머신 ID (0~31)
    private long machineId;

    // 시퀀스 번호 (0~4095)
    private long sequence = 0L;
    // 이전에 ID를 생성했던 타임스탬프 (밀리초)
    private long lastTimestamp = -1L;

    // 비트 이동 값
    private final long datacenterIdBits = 5L;
    private final long machineIdBits = 5L;
    private final long sequenceBits = 12L;

    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 31
    private final long maxMachineId = -1L ^ (-1L << machineIdBits);       // 31

    private final long machineIdShift = sequenceBits;                              // 12
    private final long datacenterIdShift = sequenceBits + machineIdBits;            // 12 + 5 = 17
    private final long timestampLeftShift = sequenceBits + machineIdBits + datacenterIdBits; // 12+5+5 =22
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);                  // 4095

    private long configuredDatacenterId = 1;

    private long configuredMachineId = 1;

    @PostConstruct
    private void init() {
        if (configuredDatacenterId < 0 || configuredDatacenterId > maxDatacenterId) {
            throw new IllegalArgumentException("Datacenter ID must be between 0 and " + maxDatacenterId);
        }
        if (configuredMachineId < 0 || configuredMachineId > maxMachineId) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + maxMachineId);
        }
        this.datacenterId = configuredDatacenterId;
        this.machineId = configuredMachineId;
    }

    public synchronized long nextId() {
        long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (lastTimestamp - timestamp) + "ms");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (machineId << machineIdShift)
                | sequence;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long lastTs) {
        long ts = currentTime();
        while (ts <= lastTs) {
            ts = currentTime();
        }
        return ts;
    }
}