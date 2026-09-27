package com.fiosmj.app.repository;

import com.fiosmj.app.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
