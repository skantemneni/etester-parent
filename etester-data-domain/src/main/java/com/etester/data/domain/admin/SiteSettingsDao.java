package com.etester.data.domain.admin;

import java.util.List;

public interface SiteSettingsDao {

    public List<SiteSettings> findAll();

    public SiteSettings findBySettingName(String settingName);
    
    public String findBySettingValueBySettingName(String settingName);
}

