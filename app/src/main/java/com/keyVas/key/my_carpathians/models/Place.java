package com.keyVas.key.my_carpathians.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by key on 21.03.18.
 */
@IgnoreExtraProperties
public class Place implements Serializable {
	public static final String UA = "uk";
	public static final String RU = "ru";
	public static final String EN = "en";

	private HashMap<String, String> name;
	private HashMap<String, String> title;
	private String urlPlace;
	private int region;
	private String publisher;
	private int typePlace;
	private Position positionPlace;

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


	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getNamePlace(String languageKey) {

		return name.get(languageKey);
	}
	public String placeKey() {
		return name.get(EN);
	}
	public void setNamePlace(String name) {
		if (name != null) {
			this.name = new HashMap<>();
			this.name.put(UA, name);
			this.name.put(RU, name);
			this.name.put(EN, name);
		}
	}
	public void setNamePlace(String name, String LanguageKey) {
		if (name != null) {
			if (this.name == null) {
				this.name = new HashMap<>();
			}
			this.name.put(LanguageKey, name);
			this.name.put(EN, name);
		}
	}
	public String getTitlePlace(String languageKey) {
		if (title != null) {
			return title.get(languageKey);
		}
		return null;
	}
	public void setTitlePlace(String title){
		if (this.title != null) {
			this.title.clear();
		}else {
			this.title = new HashMap<>();
		}
			this.title.put(UA, title);
			this.title.put(RU, title);
			this.title.put(EN, title);

	}

	public void setTitlePlace(String title, String LanguageKey) {
		if (title != null) {

			if (this.title == null) {
				this.title = new HashMap<>();
			}
			this.title.put(LanguageKey, title);
			this.title.put(EN, title);
		}
	}

	public void setUrlPlace(String urlPlace){
		this.urlPlace = urlPlace;

	}
	public String getUrlPlace(){
		return urlPlace;
	}

	public int getRegion() {
		return region;
	}

	public void setRegion(int region) {
		this.region = region;
	}

	public HashMap<String, String> getName() {
		return name;
	}

	public void setName(HashMap<String, String> name) {
		this.name = name;
	}

	public HashMap<String, String> getTitle() {
		return title;
	}

	public void setTitle(HashMap<String, String> title) {
		this.title = title;
	}
}
