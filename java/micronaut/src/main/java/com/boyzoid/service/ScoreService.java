package com.boyzoid.service;

import com.boyzoid.config.DocumentStoreConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.xdevapi.*;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ScoreService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientFactory clientFactory = new ClientFactory();
    private final DocumentStoreConfig documentStoreConfig;
    private Client cli;
    private String url;
    private String schema;
    private String collection;

    public ScoreService(DocumentStoreConfig documentStoreConfig) throws JsonProcessingException {
        this.documentStoreConfig = documentStoreConfig;
        this.url = "mysqlx://" + documentStoreConfig.getUser() +
                ":" + documentStoreConfig.getPassword() +
                "@" + documentStoreConfig.getHost() +
                ":" + documentStoreConfig.getPort().toString() +
                "/" + documentStoreConfig.getSchema();
        this.schema = documentStoreConfig.getSchema();
        this.collection = documentStoreConfig.getCollection();
        Map options = CollectionUtils.mapOf(
                "pooling", CollectionUtils.mapOf(
                        "enabled", true,
                        "maxSize", 8,
                        "maxIdleTime", 30000,
                        "queueTimeout", 10000
                        )
        );
        this.cli = clientFactory.getClient(this.url, objectMapper.writeValueAsString(options));
    }

    public ArrayList<Object> getAllScores() throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find().execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }

    public ArrayList<Object> getScores(Integer limit, Integer offset) throws JsonProcessingException {
       Session session = getSession();
       Schema schema = session.getSchema(this.schema);
       Collection col = schema.getCollection(this.collection);
       DocResult result = col.find()
               .limit(limit)
               .offset(offset)
               .execute();
       ArrayList<Object> docs = cleanResults(result.fetchAll());
       session.close();
       return docs;
    }

    public ArrayList<Object> getBestScores(Integer limit) throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find()
                .fields("firstName as firstName, " +
                        "lastName as lastName, " +
                        "score as score, " +
                        "course as course, " +
                        "`date` as datePlayed")
                .sort("score asc, `date` desc")
                .limit(limit)
                .execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }

    public ArrayList<Object> getRoundsUnderPar() throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find("score < course.par")
                .fields("firstName as firstName, " +
                        "lastName as lastName, " +
                        "score as score, " +
                        "course.name as course, " +
                        "`date` as datePlayed")
                .sort("`date` desc")
                .execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }

    public ArrayList<Object> getByScore(Integer score) throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find("score = :scoreParam")
                .bind("scoreParam", score)
                .fields("concat(firstName, \" \", lastName) as golfer, " +
                        "score as score, " +
                        "course.name as course, " +
                        "`date` as datePlayed")
                .sort("score asc, `date` desc")
                .execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }

    public ArrayList<Object> getByGolfer(String lastName) throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find("lower(lastName) like :lastNameParam")
                .bind("lastNameParam", lastName.toLowerCase()+"%")
                .sort("lastName, firstName, `date` desc")
                .execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }

    public ArrayList<Object> getCourseScoringData() throws JsonProcessingException {
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        DocResult result = col.find()
                .fields("course.name as courseName, " +
                        "round(avg(score), 2)  as avg, " +
                        "min(cast(score as unsigned)) as lowestScore," +
                        "max(cast(score as unsigned)) as highestScore, " +
                        "count(score) as numberOfRounds")
                .groupBy("course.name")
                .sort("course.name desc")
                .execute();
        ArrayList<Object> docs = cleanResults(result.fetchAll());
        session.close();
        return docs;
    }
    
    public ArrayList<Object> getAggregateCourseScore() throws JsonProcessingException {
        Session session = getSession();
        String sql = "WITH aggScores AS " +
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
                "        SELECT JSON_OBJECT('courseName', course, 'bestScore', sum(minScore)) agg" +
                "        FROM aggScores " +
                "        GROUP BY course " +
                "        ORDER BY course;";
        SqlStatement query = session.sql(sql);
        SqlResult result = query.execute();
        ArrayList<Object> docs = cleanSqlResults(result.fetchAll(), "agg");
        session.close();
        return docs;
    }

    public Boolean addScore(String score) {
        Boolean success = true;
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        try {
            col.add(score).execute();
        }
        catch (Exception e) {
            success = false;
        }
        session.close();
        return success;
    }
    public Boolean addHoleScores(String data) throws IOException {
        DbDoc doc = JsonParser.parseDoc(data);
        JsonArray holeScores = JsonParser.parseArray(new StringReader(doc.get("holeScores").toString()));
        var id = doc.get("_id");
        Boolean success = true;
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        try {
            col.modify("_id = :idParam")
                    .set("holeScores", holeScores )
                    .bind("idParam", id)
                    .execute();
        }
        catch (Exception e) {
            success = false;
        }
        session.close();
        return success;
    }

    public Boolean removeScore(String id)  {
        Boolean success = true;
        Session session = getSession();
        Schema schema = session.getSchema(this.schema);
        Collection col = schema.getCollection(this.collection);
        try {
            col.remove("_id = :idParam")
                    .bind("idParam", id)
                    .execute();
        }
        catch (Exception e) {
            success = false;
        }
        session.close();
        return success;
    }


    private Session getSession(){
        return cli.getSession();
    }

    private ArrayList<Object> cleanResults(List<DbDoc> docs) throws JsonProcessingException {
        ArrayList<Object> cleaned = new ArrayList<>();
        for( DbDoc doc : docs){
            cleaned.add( objectMapper.readTree(doc.toString()));
        }
        return cleaned;
    }

    private ArrayList<Object> cleanSqlResults(List<Row> rows, String field) throws JsonProcessingException {
        ArrayList<Object> cleaned = new ArrayList<>();
        for( Row row : rows){
            cleaned.add( objectMapper.readTree(row.getString(field)));
        }
        return cleaned;
    }

}
