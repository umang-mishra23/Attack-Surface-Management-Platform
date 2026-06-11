package com.sentinelasm.risk;

import com.sentinelasm.domain.Severity;
import com.sentinelasm.finding.Finding;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiskService {
    public int score(List<Finding> findings) {
        int raw = findings.stream().mapToInt(finding -> switch (finding.getSeverity()) {
            case CRITICAL -> 10;
            case HIGH -> 7;
            case MEDIUM -> 4;
            case LOW -> 2;
            case INFORMATIONAL -> 1;
        }).sum();
        return Math.min(100, raw * 4);
    }

    public int assetScore(List<Finding> findings) {
        return Math.min(100, score(findings) + 10);
    }
}
