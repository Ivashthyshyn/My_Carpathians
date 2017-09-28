package com.example.key.my_carpathians.utils;

import android.text.TextUtils;
import android.util.Log;

import com.mapbox.services.commons.models.Position;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * Created by key on 27.09.17.
 */

public class AltitudeFinder {

	private static final String LOG_TAG = AltitudeFinder.class.getSimpleName();
	private static final String USGS_REQUEST_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
	private static final String YOUR_API_KEY = "&key=AIzaSyD2BCy-prEsCoH9NOe2wnmNDKKBnSMe3do";
	public AltitudeFinder(){

	}

	public static final int cTimeOutMs = 30 * 1000;

	public static List<com.cocoahero.android.geojson.Position> getElevation(List<Position> location ) throws IOException, JSONException {
		List<com.cocoahero.android.geojson.Position> pos = new ArrayList<>();
		// https://developers.google.com/maps/documentation/elevation/
		String valueLocation = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
		for (int i = 0 ; i < location.size() - 1; i++){
			valueLocation = valueLocation + String.valueOf(location.get(i).getLatitude()) + "," +
					String.valueOf(location.get(i).getLongitude()) +"|";
		}

		URL url = new URL(valueLocation +
				String.valueOf(location.get(location.size() - 1).getLatitude()) + "," +
				String.valueOf(location.get(location.size() - 1).getLongitude()) +
				"&key=AIzaSyCW-3Yyo8aeNIj-Bj9LK-Z1g97MYf9lWlg");
		Log.d(LOG_TAG, "url=" + url);
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

		urlConnection.setConnectTimeout(cTimeOutMs);
		urlConnection.setReadTimeout(cTimeOutMs);
		urlConnection.setRequestProperty("Accept", "application/json");

		// Set request type
		urlConnection.setRequestMethod("GET");
		urlConnection.setDoOutput(false);
		urlConnection.setDoInput(true);

		try {
			// Check for errors
			int code = urlConnection.getResponseCode();
			if (code != HttpsURLConnection.HTTP_OK)
				throw new IOException("HTTP error " + urlConnection.getResponseCode());

			// Get response
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder json = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				json.append(line);
			Log.d(LOG_TAG, json.toString());

			// Decode result
			JSONObject jroot = new JSONObject(json.toString());
			String status = jroot.getString("status");
			if ("OK".equals(status)) {
				JSONArray results = jroot.getJSONArray("results");
				if (results.length() > 0) {
					for (int lc = 0; lc < results.length(); lc++) {
						double elevation = results.getJSONObject(lc).getDouble("elevation");
						double lat = results.getJSONObject(lc).getJSONObject("location").getDouble("lat");
						double lng = results.getJSONObject(lc).getJSONObject("location").getDouble("lng");
						com.cocoahero.android.geojson.Position p = new com.cocoahero.android.geojson.Position(lat, lng, elevation);
						pos.add(p);
						Log.i(LOG_TAG, "Elevation " + location);
					}
				} else
					throw new IOException("JSON no results");
			} else
				throw new IOException("JSON status " + status);
		} finally {
			urlConnection.disconnect();
		}
		return pos;
	}

	public static List<com.cocoahero.android.geojson.Position> extractAltitude(List<Position> positionList) {
		//get url for String data
		URL url = createUrl(positionList);

		// Perform HTTP request to the URL and receive a JSON response back
		String jsonResponse = null;
		try {
			jsonResponse = makeHttpRequest(url);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error closing input stream", e);
		}

		// Extract relevant fields from the JSON response and create an {@link Event} object
		List <com.cocoahero.android.geojson.Position> positions = extractFeatureFromJson(jsonResponse);

		// Return the list of earthquakes
		return positions;
	}
	/**
	 * Returns new URL object from the given string URL.
	 */
	private static URL createUrl(List<Position> positions) {

		URL url = null;
		StringBuilder baseUri = new StringBuilder();
		baseUri.append(USGS_REQUEST_URL);
		for(int i = 0; i < 1; i++){
			if (i <  1) {
				baseUri.append(positions.get(i).getLatitude()).append(",").append(positions.get(i).getLongitude());
			}else {
				baseUri.append(positions.get(i).getLatitude()).append(",").append(positions.get(i).getLongitude());
			}
		}
		String createdUri = String.valueOf(baseUri.append(YOUR_API_KEY));
		String ura =  "https://maps.googleapis.com/maps/api/elevation/json?locations=" + 49.1313733 + "," +24.3051567 + "&key="+"AIzaSyD2BCy-prEsCoH9NOe2wnmNDKKBnSMe3do";
		try {
			url = new URL(createdUri);
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Error with creating URL ", e);
		}
		return url;
	}

	/**
	 * Make an HTTP request to the given URL and return a String as the response.
	 */
	private static String makeHttpRequest(URL url) throws IOException {
		String jsonResponse = "";

		// If the URL is null, then return early.
		if (url == null) {
			return jsonResponse;
		}

		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(5000 /* milliseconds */);
			urlConnection.setConnectTimeout(5000 /* milliseconds */);
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			// If the request was successful (response code 200),
			// then read the input stream and parse the response.
			if (urlConnection.getResponseCode() == 200) {
				inputStream = urlConnection.getInputStream();
				jsonResponse = readFromStream(inputStream);
			} else {
				Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return jsonResponse;
	}


	/**
	 * Convert the {@link InputStream} into a String which contains the
	 * whole JSON response from the server.
	 */
	private static String readFromStream(InputStream inputStream) throws IOException {
		StringBuilder output = new StringBuilder();
		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = reader.readLine();
			while (line != null) {
				output.append(line);
				line = reader.readLine();
			}
		}
		return output.toString();
	}
	/**

	 * about the first earthquake from the input earthquakeJSON string.
	 */
	private static List <com.cocoahero.android.geojson.Position> extractFeatureFromJson(String resultJSON) {
		// Create an empty ArrayList that we can start adding news to
		List<com.cocoahero.android.geojson.Position> trackPositions = new ArrayList<>();


		// If the JSON string is empty or null, then return early.
		if (TextUtils.isEmpty(resultJSON)) {
			return null;
		}
		// Try to parse the JSON response string. If there's a problem with the way the JSON
		// is formatted, a JSONException exception object will be thrown.
		// Catch the exception so the app doesn't crash, and print the error message to the logs.
		try {
			JSONObject baseJsonResponse = new JSONObject(resultJSON);
			// Extract the JSONArray associated with the key called "response",
			// which represents a list of response
			JSONArray response = baseJsonResponse.getJSONArray("results");
			if (response.length() > 0) {
			for (int lc = 0; lc < response.length(); lc++) {
				JSONObject coord = response.getJSONObject(lc);
				JSONObject locationJSON = coord.getJSONObject("location");
				com.cocoahero.android.geojson.Position pos = new com.cocoahero.android.geojson.Position(locationJSON.getDouble("lng"), locationJSON.getDouble("lat"), coord.getDouble("elevation"));
				trackPositions.add(pos);
				}
				
				}

		} catch (JSONException e) {
			// If an error is thrown when executing any of the above statements in the "try" block,
			// catch the exception here, so the app doesn't crash. Print a log message
			// with the message from the exception.
			Log.e(LOG_TAG, "Problem parsing the news JSON results", e);
		}
		// Return the list of
		return trackPositions;
	}
}
