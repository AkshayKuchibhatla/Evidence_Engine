package edu.gmu.searchbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.jayway.jsonpath.JsonPath;

public class SearchService {

	 private static final String BASE_URL =
	            "https://wrlc-gm.primo.exlibrisgroup.com/primaws/rest/pub/pnxs";
	 
	    public static void main(String[] args) {
	        printTitles("American Revolution");
	    }
	    
	    public static void printTitles(String searchQuery) {
	    	// Request parameters (LinkedHashMap keeps them in insertion order)
	        Map<String, String> params = new LinkedHashMap<>();
	        params.put("q", "any,contains," + searchQuery);
	        params.put("inst", "01WRLC_GML");
	        params.put("vid", "01WRLC_GML:01WRLC_GML");
	        params.put("scope", "MyInst_and_CI");
	        params.put("tab", "Everything");
	        params.put("limit", "10");
	        params.put("offset", "0");
	 
	        try {
	            String response = sendGet(BASE_URL, params);
	            System.out.println(response);
	            
	            List<String> titles = JsonPath.read(response, "$.docs[*].pnx.display.title[0]");
	            List<String> types = JsonPath.read(response, "$.docs[*].pnx.display.type[0]");
	            List<String> creationDates = JsonPath.read(response, "$.docs[*].pnx.display.creationdate[0]");
	            
	            for(int i = 0; i < titles.size(); i++) {
	            	System.out.println("Title : " + titles.get(i) );
	            	System.out.println("Type : " + types.get(i) );
	            	System.out.println("Creation Date : " + creationDates.get(i) );
	            	System.out.println("");
	            }
	            System.out.println();
	            	            
	        } catch (IOException e) {
	            System.err.println("Request failed: " + e.getMessage());
	        }
	    }
	 
	    /** Sends a GET request with the given query parameters and returns the response body. */
	    public static String sendGet(String baseUrl, Map<String, String> params) throws IOException {
	        URL url = new URL(baseUrl + "?" + buildQueryString(params));
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	 
	        try {
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Accept", "application/json");
	            conn.setRequestProperty("User-Agent", "Java-HttpURLConnection");
	            conn.setConnectTimeout(10_000); // 10 seconds
	            conn.setReadTimeout(15_000);    // 15 seconds
	 
	            int status = conn.getResponseCode();
	            System.out.println("Request URL: " + url);
	            System.out.println("HTTP status: " + status);
	 
	            // Error responses (4xx/5xx) come through the error stream instead
	            InputStream stream = (status >= 200 && status < 300)
	                    ? conn.getInputStream()
	                    : conn.getErrorStream();
	 
	            return readStream(stream);
	        } finally {
	            conn.disconnect();
	        }
	    }
	 
	    /** URL-encodes each key and value and joins them as key=value&key=value. */
	    private static String buildQueryString(Map<String, String> params) {
	        StringJoiner joiner = new StringJoiner("&");
	        for (Map.Entry<String, String> entry : params.entrySet()) {
	            joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
	                    + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
	        }
	        return joiner.toString();
	    }
	 
	    /** Reads an InputStream fully into a String. */
	    private static String readStream(InputStream stream) throws IOException {
	        if (stream == null) {
	            return "";
	        }
	        StringBuilder sb = new StringBuilder();
	        try (BufferedReader reader = new BufferedReader(
	                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line).append('\n');
	            }
	        }
	        return sb.toString();
	    }
}
