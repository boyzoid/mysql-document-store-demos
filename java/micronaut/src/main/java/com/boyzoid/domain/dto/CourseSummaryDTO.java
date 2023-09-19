package com.boyzoid.domain.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class CourseSummaryDTO {

    private String courseName;
    private Integer bestScore;

    public CourseSummaryDTO() {
    }

    public CourseSummaryDTO(String courseName, Integer bestScore) {
        this.courseName = courseName;
        this.bestScore = bestScore;
    }

    public Integer getBestScore() {
        return bestScore;
    }

    public void setBestScore(Integer score) {
        this.bestScore = score;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
