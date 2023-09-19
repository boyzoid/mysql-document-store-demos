package com.boyzoid.repository;

import com.boyzoid.domain.dto.CourseSummaryDTO;
import com.boyzoid.domain.entity.Score;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.GenericRepository;

import java.util.ArrayList;

@Repository
public interface ScoreRepository extends GenericRepository<Score, String> {

    @Query(value = "WITH aggScores AS " +
            "            (SELECT doc ->> '$.course.name' course, " +
            "                MIN(score)              minScore, " +
            "                MAX(score)              maxScore, " +
            "                number " +
            "            FROM scores, " +
            "                JSON_TABLE(doc, '$.holeScores[*]' " +
            "                    COLUMNS ( " +
            "                        score INT PATH '$.score', " +
            "                        number INT PATH '$.number')) AS scores " +
            "            GROUP BY course, number " +
            "            ORDER BY course, number) " +
            "        SELECT course as courseName, sum(minScore) as bestScore" +
            "        FROM aggScores " +
            "        GROUP BY course " +
            "        ORDER BY course;",
            nativeQuery = true)
    ArrayList<CourseSummaryDTO> courseSummary();
}
