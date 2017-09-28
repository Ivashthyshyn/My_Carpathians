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
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;


/**
 * Created by key on 27.09.17.
 */

public class AltitudeFinder {

	private static final String LOG_TAG = AltitudeFinder.class.getSimpleName();
	private static final String USGS_REQUEST_URL = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
	private static final String YOUR_API_KEY = "&key=AIzaSyD2BCy-prEsCoH9NOe2wnmNDKKBnSMe3do;";
	public AltitudeFinder(){

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
		GetHttpToServer(createdUri);
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

	public static int GetHttpToServer(String urlLink) {
		try {
			URL obj = new URL(urlLink);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();

			BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				//response.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException ex) {
			Log.e("GetHttp", Log.getStackTraceString(ex));
			return 2;
		} catch (NoRouteToHostException ex) {
			Log.e("GetHttp", Log.getStackTraceString(ex));
			return 3;
		} catch (SocketTimeoutException ex){
			Log.e("GetHttp", Log.getStackTraceString(ex));
			return 4;
		} catch (SSLException ex){
			Log.e("GetHttp", Log.getStackTraceString(ex));
			return 5;

		} catch (IOException ex) {
			Log.e("GetHttp", Log.getStackTraceString(ex));
			return 6;
		} catch (Exception e){
			Log.e("GetHttp", Log.getStackTraceString(e));
			return 7;

		}
		return 0;
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
