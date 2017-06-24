package com.example.key.my_carpathians.database;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Key on 14.06.2017.
 */

@IgnoreExtraProperties
public class Places  {
    public String titlePlace;
    public String urlPlace;
    public String namePlace;
    public String positionPlace;
    public Places() {
    }

    public String getNamePlace() {
        return namePlace;
    }

    public void setPositionPlace(String positionPlace) {
        this.positionPlace = positionPlace;
    }

    public void setNamePlace(String namePlace) {
        this.namePlace = namePlace;
    }

    public void setTitlePlace(String titlePlace){
    this.titlePlace = titlePlace;

    }
    public String getTitlePlace(){
    return titlePlace;
}

    public String getPositionPlace() {
        return positionPlace;
    }

    public void setUrlPlace(String urlPlace){
        this.urlPlace = urlPlace;

    }
    public String getUrlPlace(){
        return urlPlace;
    }
}
