package com.etester.data.domain.test;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name="serialized_testinstance")
public class SerializedTestinstance {

	@Id
    @Column(name = "id_testinstance")
    private Long idTestinstance;
	
	@NotNull
    @Column(name = "testinstance_string_json")
	private String testinstanceStringJson;

	@Column(name = "date_saved")
	private Date dateSaved;

}
