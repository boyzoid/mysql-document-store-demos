package com.boyzoid.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "scores")
public class Score {
    @Id
    @Column(name = "_id")
    private String _id;

    @Column
    private String doc;


    public void setId(String id) {
        this._id = id;
    }

    public String getId() {
        return _id;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

}
