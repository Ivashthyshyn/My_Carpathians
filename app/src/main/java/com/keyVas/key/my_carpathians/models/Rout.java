package com.keyVas.key.my_carpathians.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;

import static com.keyVas.key.my_carpathians.models.Place.EN;
import static com.keyVas.key.my_carpathians.models.Place.RU;
import static com.keyVas.key.my_carpathians.models.Place.UA;

/**
 * Created by key on 21.03.18.
 */
@IgnoreExtraProperties
public class Rout implements Serializable {
	private HashMap name;
	private HashMap title;
	private String publisher;
	private String urlRout;
	private int region;
	private String urlRoutsTrack;
	private String lengthRout;
	private int routsLevel;
	private Position positionRout;



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



	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getUrlRout() {
		return urlRout;
	}


	public String getNameRout(String languageKey) {

		return (String)name.get(languageKey);
	}
	public String routKey() {
		return (String)name.get(EN);
	}
	public void setNameRout(String name) {
		if (name != null) {
			this.name = new HashMap<>();
			this.name.put(UA, name);
			this.name.put(RU, name);
			this.name.put(EN, name);
		}
	}
	public void setNameRout(String name, String LanguageKey) {
		if (name != null) {
			if (this.name == null) {
				this.name = new HashMap<>();
			}
			this.name.put(LanguageKey, name);
			this.name.put(EN, name);
		}
	}
	public String getTitleRout(String languageKey) {
		return (String)title.get(languageKey);
	}

	public void setTitleRout(String title){
		if (title != null) {
			this.title = new HashMap<>();
			this.title.put(UA, title);
			this.title.put(RU, title);
			this.title.put(EN, title);
		}
	}
	public void setTitleRout(String title, String LanguageKey) {
		if (title != null) {

			if (this.title == null) {
				this.title = new HashMap<>();
			}
			this.title.put(LanguageKey, title);
			this.title.put(EN, title);
		}
	}

	public void setUrlRout(String urlRout) {
		this.urlRout = urlRout;
	}

	public HashMap getName() {
		return name;
	}

	public void setName(HashMap name) {
		this.name = name;
	}

	public HashMap getTitle() {
		return title;
	}

	public void setTitle(HashMap title) {
		this.title = title;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}
}
