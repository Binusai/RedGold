package com.brick.billing.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseStatsController {

    private final JdbcTemplate jdbc;

    // Neon free tier = 0.5 GB = 536870912 bytes
    private static final long MAX_BYTES = 536870912L;

    public DatabaseStatsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/db-stats")
    public Map<String, Object> getDbStats() {

        Long usedBytes = jdbc.queryForObject(
                "SELECT pg_database_size(current_database())",
                Long.class
        );

        double percent = (usedBytes * 100.0) / MAX_BYTES;

        Map<String, Object> res = new HashMap<>();
        res.put("usedBytes", usedBytes);
        res.put("maxBytes", MAX_BYTES);
        res.put("percent", Math.min(percent, 100));

        return res;
    }
}
