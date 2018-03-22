package com.keyVas.key.my_carpathians.utils;

import com.cocoahero.android.geojson.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by key on 16.03.18.
 */

public class TrackContainer {
	private static final TrackContainer ourInstance = new TrackContainer();
	private List<Position> positionList = new ArrayList<>();
	private boolean enabledGPSRecording = false;

	public static TrackContainer getInstance() {
		return ourInstance;
	}

	private TrackContainer() {
	}

	public boolean isEnabledGPSRecording() {
		return enabledGPSRecording;
	}

	public void setEnabledGPSRecording(boolean enabledGPSRecording) {
		this.enabledGPSRecording = enabledGPSRecording;
	}

	public List<Position> getPositionList() {
		return positionList;
	}

	public void setPositionList(List<Position> positionList) {
		this.positionList = positionList;
	}
}
