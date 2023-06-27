package com.boyzoid.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("docstore")
public class DocumentStoreConfig {
    private String user;
    private String password;
    private String host;
    private Integer port;
    private String schema;
    private String collection;

    public String getUser(){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getHost(){
        return host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public Integer getPort(){
        return port;
    }

    public void setPort(Integer port){
        this.port = port;
    }

    public String getSchema(){
        return schema;
    }

    public void setSchema(String schema){
        this.schema = schema;
    }

    public String getCollection(){
        return collection;
    }

    public void setCollection(String collection){
        this.collection = collection;
    }
}
