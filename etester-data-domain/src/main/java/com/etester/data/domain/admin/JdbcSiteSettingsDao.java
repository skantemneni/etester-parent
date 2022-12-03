package com.etester.data.domain.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class JdbcSiteSettingsDao extends NamedParameterJdbcDaoSupport implements SiteSettingsDao {

	Map<String, String> settingsMap = new HashMap<String, String>();
	
	@Override
	public List<SiteSettings> findAll() {
        String sql = "SELECT * FROM site_settings";
        BeanPropertyRowMapper<SiteSettings> siteSettingsRowMapper = BeanPropertyRowMapper.newInstance(SiteSettings.class);
        List<SiteSettings> siteSettings = getNamedParameterJdbcTemplate().query(sql, siteSettingsRowMapper);
        return siteSettings;
	}

	@Override
	public SiteSettings findBySettingName(String settingName) {
        String sql = "SELECT * FROM site_settings WHERE setting_name = :settingName";
        BeanPropertyRowMapper<SiteSettings> siteSettingsRowMapper = BeanPropertyRowMapper.newInstance(SiteSettings.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("settingName", settingName);
		// queryForObject throws an exception when the Level is missing.  this should be ignored/swallowed
        SiteSettings siteSetting = null;
        try {
        	siteSetting = getNamedParameterJdbcTemplate().queryForObject(sql, args, siteSettingsRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {}
        return siteSetting;
	}

	@Override
	public String findBySettingValueBySettingName(String settingName) {
		
		if (settingName == null || settingName.trim().length() == 0) {
			return null;
		}
		
		settingName = settingName.trim().toUpperCase();
		
		if (settingsMap.containsKey(settingName)) {
			return settingsMap.get(settingName);
		} else {
			SiteSettings siteSettings = findBySettingName(settingName);
			settingsMap.put(settingName, siteSettings == null ? "" : siteSettings.getSettingValue());
			return settingsMap.get(settingName);
		}
	}

}
