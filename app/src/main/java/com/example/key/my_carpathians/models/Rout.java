package com.example.key.my_carpathians.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/**
 * Created by Key on 15.06.2017.
 */
@IgnoreExtraProperties
public class Rout implements Serializable {
    public String nameRout;
    public String titleRout;
    public String publisher;
    public String urlRout;
    public String urlRoutsTrack;
    public String lengthRout;
    public int routsLevel;
    public Position positionRout;



    public Rout() {
    }

    public String getLengthRout() {
        return lengthRout;
    }

    public void setLengthRout(String lengthRout) {
        this.lengthRout = lengthRout;
    }

    public Position getPositionRout() {
        return positionRout;
    }

    public void setPositionRout(Position positionRout) {
        this.positionRout = positionRout;
    }

    public String getUrlRoutsTrack() {
        return urlRoutsTrack;
    }

    public void setUrlRoutsTrack(String urlRoutsTrack) {
        this.urlRoutsTrack = urlRoutsTrack;
    }

    public int getRoutsLevel() {
        return routsLevel;
    }

    public void setRoutsLevel(int routsLevel) {
        this.routsLevel = routsLevel;
    }

    public String getNameRout() {
        return nameRout;
    }

    public String getTitleRout() {
        return titleRout;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getUrlRout() {
        return urlRout;
    }



    public void setNameRout(String nameRout) {
        this.nameRout = nameRout;
    }

    public void setTitleRout(String titleRout) {
        this.titleRout = titleRout;
    }



    public void setUrlRout(String urlRout) {
        this.urlRout = urlRout;
    }
}