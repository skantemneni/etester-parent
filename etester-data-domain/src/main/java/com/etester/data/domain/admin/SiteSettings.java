package com.etester.data.domain.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="site_settings")
public class SiteSettings {

    @Id
    @Column(name = "setting_name", length = 200, nullable = false)
    private String settingName;
    @Column(name = "setting_value", length = 400, nullable = false)
    private String settingValue;

}
