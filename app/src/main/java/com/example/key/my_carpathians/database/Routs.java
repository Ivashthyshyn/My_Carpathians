package com.example.key.my_carpathians.database;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Key on 15.06.2017.
 */
@IgnoreExtraProperties
public class Routs {
    public String nameRout;
    public String titleRout;
    public String urlRout;

    public Routs() {
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
