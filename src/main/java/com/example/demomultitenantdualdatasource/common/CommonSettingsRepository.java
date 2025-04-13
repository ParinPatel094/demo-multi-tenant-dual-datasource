package com.example.demomultitenantdualdatasource.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonSettingsRepository extends JpaRepository<CommonSettings, Long> {
    CommonSettings findBySettingKey(String key);
}