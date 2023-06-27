package com.boyzoid.controller;

import com.boyzoid.service.ScoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;


@Controller
public class ScoreController {
    private final ScoreService scoreService;
    private final Integer defaultLimit = 50;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Get(value="/", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object> home() {
        return HttpResponse.ok(CollectionUtils.mapOf("message", "Micronaut/Java Demo main endpoint"));
    }
    @Get(value = "/list", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object> listAll(@Nullable Integer limit ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getAllScores();
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/list/{limit}{/offset}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object> list(Integer limit, @Nullable Integer offset ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getScores(limit, !Objects.isNull(offset) ? offset : 0 );
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/bestScores{/limit}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object> bestScores(@Nullable Integer limit ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getBestScores(!Objects.isNull(limit) ? limit : defaultLimit );
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/getRoundsUnderPar", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object> roundsUnderPar( ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getRoundsUnderPar();
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/getByScore{/score}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>getByScore(@Nullable Integer score ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getByScore(!Objects.isNull(score) ? score : 45 );
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/getByGolfer{/lastName}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>getByGolfer(@Nullable String lastName ) throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getByGolfer(!Objects.isNull(lastName) ? lastName : "" );
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/getCourseScoringData", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>getCourseScoringData() throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getCourseScoringData();
        return HttpResponse.ok(getResult(scores));
    }

    @Get(value = "/getAggregateCourseScore", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>getAggregateCourseScore() throws JsonProcessingException {
        ArrayList<Object> scores = scoreService.getAggregateCourseScore();
        return HttpResponse.ok(getResult(scores));
    }
    @Post( value="/score", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>addScore(@Body JSONObject score){
        Boolean success = scoreService.addScore(score.toJSONString());
        return HttpResponse.ok(CollectionUtils.mapOf("success", success));
    }

    @Post( value="/holeScores", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>addHoleScores(@Body String score) throws IOException{
        Boolean success = scoreService.addHoleScores(score);
        return HttpResponse.ok(CollectionUtils.mapOf("success", success));
    }

    @Get(value = "/removeScore/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<Object>removeScore(String id) throws JsonProcessingException {
        Boolean success = scoreService.removeScore(id);
        return HttpResponse.ok(CollectionUtils.mapOf("success", success));
    }

    private static Map getResult(ArrayList<?> scores){
        return CollectionUtils.mapOf("count", scores.size(), "scores", scores);
    }
}
