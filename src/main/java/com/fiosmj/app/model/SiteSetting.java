package com.fiosmj.app.model;

import jakarta.persistence.*;

/** Texto editável do site (chave → valor), gerido pela aba "Site" do painel admin. */
@Entity
@Table(name = "site_settings")
public class SiteSetting {

    @Id
    @Column(name = "setting_key", length = 60)
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    public SiteSetting() {}

    public SiteSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
