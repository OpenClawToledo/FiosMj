package com.fiosmj.app.controller;

import com.fiosmj.app.entity.SiteStats;
import com.fiosmj.app.repository.SiteStatsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final SiteStatsRepository repo;

    public StatsController(SiteStatsRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/visits")
    public ResponseEntity<Map<String, Long>> getVisits() {
        SiteStats stats = repo.findById("visit_count").orElse(new SiteStats("visit_count", 0L));
        return ResponseEntity.ok(Map.of("count", stats.getValue()));
    }

    @PostMapping("/visits")
    @Transactional
    public ResponseEntity<Map<String, Long>> incrementVisits() {
        SiteStats stats = repo.findById("visit_count").orElse(new SiteStats("visit_count", 0L));
        stats.setValue(stats.getValue() + 1);
        repo.save(stats);
        return ResponseEntity.ok(Map.of("count", stats.getValue()));
    }
}
