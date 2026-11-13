package com.evidence_engine.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.evidence_engine.ai.QueryPlan;
import com.evidence_engine.ai.QueryPlanner;
import com.evidence_engine.ai.AiException;

@Service
public class SearchRequestService {

	private static final Logger log = LoggerFactory.getLogger(SearchRequestService.class);

	// Placeholder store. Swap for a JPA repository when you add a database.
	private final Map<UUID, SearchRequest> store = new ConcurrentHashMap<>();
	private final QueryPlanner queryPlanner;

	// Spring passes in the QueryPlanner bean from AiConfig automatically
	public SearchRequestService(QueryPlanner queryPlanner) {
		this.queryPlanner = queryPlanner;
	}

	/** Reads an InputStream fully into a String. */
	private static String readStream(InputStream stream) throws IOException {
		if (stream == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		}
		return sb.toString();
	}

	/** Sends a GET request to the query URL and returns the response body. */
	public static String sendGet(String qUrl) throws IOException {
		URL url = null;
		try {
			url = new URI(qUrl).toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		try {
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("User-Agent", "Java-HttpURLConnection");
			conn.setConnectTimeout(10_000); // 10 seconds
			conn.setReadTimeout(15_000); // 15 seconds

			int status = conn.getResponseCode();
			System.out.println("Request URL: " + url);
			System.out.println("HTTP status: " + status);

			// Error responses (4xx/5xx) come through the error stream instead
			InputStream stream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

			return readStream(stream);
		} finally {
			conn.disconnect();
		}
	}

	private static final String PRIMO_API_URL = "https://wrlc-gm.primo.exlibrisgroup.com/primaws/rest/pub/pnxs";

	/**
	 * Turns the library website URL from QueryPlan into the matching Primo JSON API
	 * URL.
	 */
	private static String toPrimoApiUrl(String websiteUrl) {
		String rawQuery = URI.create(websiteUrl).getRawQuery(); // everything after "?", still encoded

		Map<String, String> api = new LinkedHashMap<>();
		List<String> includes = new ArrayList<>();

		// Split on & and = BEFORE decoding, so a & or = inside the search text can't
		// break it
		for (String pair : rawQuery.split("&")) {
			int eq = pair.indexOf('=');
			if (eq < 0)
				continue;
			String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
			String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);

			switch (key) {
			case "query" -> api.put("q", value);
			case "search_scope" -> api.put("scope", value);
			case "vid" -> {
				api.put("inst", value.split(":")[0]);
				api.put("vid", value);
			}
			case "facet" -> {
				String[] parts = value.split(",", 3); // e.g. [rtype, include, articles]
				if (parts.length == 3 && parts[1].equals("include")) {
					includes.add("facet_" + parts[0] + ",exact," + parts[2]);
				}
			}
			default -> {
			} // ignore anything else
			}
		}

		api.put("tab", "Everything");
		api.put("limit", "10");
		api.put("offset", "0");
		if (!includes.isEmpty()) {
			api.put("qInclude", String.join("|,|", includes));
		}

		// Re-encode as key=value&key=value
		StringJoiner joiner = new StringJoiner("&");
		for (Map.Entry<String, String> entry : api.entrySet()) {
			joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
					+ URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
		}
		return PRIMO_API_URL + "?" + joiner;
	}

	public SearchRequest create(SearchRequestDto dto) throws IOException {
		SearchRequest request = new SearchRequest(UUID.randomUUID(), dto.thesis().trim(), dto.reason().trim(),
				SearchRequest.Status.PENDING, Instant.now(), null, null, null, null);

		store.put(request.id(), request);

		log.info("Search request created id={} status={} thesisLength={} reasonLength={}", request.id(),
				request.status(), request.thesis().length(), request.reason().length());
		log.debug("Search request id={} thesis=\"{}\"", request.id(), request.thesis());

		// Hand off to the AI agent: thesis + reason -> search queries (issue #17)
		try {
			QueryPlan plan = queryPlanner.plan(request.thesis(), request.reason());
			request = request.withPlan(plan);
			log.info("Queries ready id={} model={} query={}", request.id(), plan.model(), request.query());
		} catch (AiException e) {
			request = request.failed(e.getMessage());
			log.warn("Query planning failed id={}: {}", request.id(), e.getMessage());
		}

		if (request.status() != SearchRequest.Status.FAILED) {
			try {
				String apiUrl = toPrimoApiUrl(request.gmuSearchUrl());
	            String response = sendGet(apiUrl);
	            request = request.withResponse(response);
			} catch (IOException e) {
				request = request.failed("Library search failed: " + e.getMessage());
				log.warn("Library search failed id={}: {}", request.id(), e.getMessage());
			}
		}

		store.put(request.id(), request); // save the updated version
		return request;
	}

	public Optional<SearchRequest> find(UUID id) {
		return Optional.ofNullable(store.get(id));
	}
}
