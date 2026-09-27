package com.fiosmj.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "site_stats")
public class SiteStats {

    @Id
    @Column(name = "stat_key", nullable = false, unique = true)
    private String key;

    @Column(name = "stat_value", nullable = false)
    private Long value;

    public SiteStats() {}

    public SiteStats(String key, Long value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}
