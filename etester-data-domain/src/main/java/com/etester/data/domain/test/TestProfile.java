package com.etester.data.domain.test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name="anal_test_rollup")
public class TestProfile {

	@Id
	@NotNull
    @Column(name = "id_test")
	private Long idTest;

	@Column(name = "correct_count_average")
    private Integer correctCountAverage;
	
	@Column(name = "correct_points_average")
    private Float correctPointsAverage;
	
	@Column(name = "percentage_average")
    private Float percentageAverage;
	
	@Column(name = "sd1")
    private Float sd1;
	
    @Column(name = "percentile_distribution")
	private String percentileDistribution;

	/**
	 * @return the idTest
	 */
	public Long getIdTest() {
		return idTest;
	}

	/**
	 * @param idTest the idTest to set
	 */
	public void setIdTest(Long idTest) {
		this.idTest = idTest;
	}

	/**
	 * @return the correctCountAverage
	 */
	public Integer getCorrectCountAverage() {
		return correctCountAverage;
	}

	/**
	 * @param correctCountAverage the correctCountAverage to set
	 */
	public void setCorrectCountAverage(Integer correctCountAverage) {
		this.correctCountAverage = correctCountAverage;
	}

	/**
	 * @return the correctPointsAverage
	 */
	public Float getCorrectPointsAverage() {
		return correctPointsAverage;
	}

	/**
	 * @param correctPointsAverage the correctPointsAverage to set
	 */
	public void setCorrectPointsAverage(Float correctPointsAverage) {
		this.correctPointsAverage = correctPointsAverage;
	}

	/**
	 * @return the percentageAverage
	 */
	public Float getPercentageAverage() {
		return percentageAverage;
	}

	/**
	 * @param percentageAverage the percentageAverage to set
	 */
	public void setPercentageAverage(Float percentageAverage) {
		this.percentageAverage = percentageAverage;
	}

	/**
	 * @return the sd1
	 */
	public Float getSd1() {
		return sd1;
	}

	/**
	 * @param sd1 the sd1 to set
	 */
	public void setSd1(Float sd1) {
		this.sd1 = sd1;
	}

	/**
	 * @return the percentileDistribution
	 */
	public String getPercentileDistribution() {
		return percentileDistribution;
	}

	/**
	 * @param percentileDistribution the percentileDistribution to set
	 */
	public void setPercentileDistribution(String percentileDistribution) {
		this.percentileDistribution = percentileDistribution;
	}
}
