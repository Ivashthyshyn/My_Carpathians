package com.example.key.my_carpathians.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;


/**
 * Created by Key on 14.06.2017.
 */

@IgnoreExtraProperties
public class Place implements Serializable {
    public String titlePlace;
    public String urlPlace;
    public String namePlace;
    public String publisher;
    public int typePlace;
    public Position positionPlace;



    public Place() {
    }

    public int getTypePlace() {
        return typePlace;
    }

    public void setTypePlace(int typePlace) {
        this.typePlace = typePlace;
    }

    public Position getPositionPlace() {
        return positionPlace;
    }

    public void setPositionPlace(Position positionPlace) {
        this.positionPlace = positionPlace;
    }

    public String getNamePlace() {
        return namePlace;
    }
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
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


    public void setUrlPlace(String urlPlace){
        this.urlPlace = urlPlace;

    }
    public String getUrlPlace(){
        return urlPlace;
    }


}
