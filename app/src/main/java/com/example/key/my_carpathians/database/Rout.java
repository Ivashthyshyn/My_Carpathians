package com.example.key.my_carpathians.database;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Key on 15.06.2017.
 */
@IgnoreExtraProperties
public class Rout {
    public String nameRout;
    public String titleRout;
    public String urlRout;
    public String urlRoutsTrack;
    public int routsLevel;
    public Position positionRout;



    public Rout() {
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
