package com.evidence_engine.backend.ai;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** AI search terms. Terms in a group are OR-ed; groups are AND-ed. */
public record QueryPlan(List<List<String>> conceptGroups, List<String> exclude,
                        boolean peerReviewedOnly, String model) {

    /** e.g. ("urban parks" OR "green space") AND (exercise OR "physical activity") */
    public String booleanQuery() {
        List<String> parts = new ArrayList<>();
        for (List<String> group : conceptGroups) {
            parts.add("(" + group.stream().map(QueryPlan::quote).collect(Collectors.joining(" OR ")) + ")");
        }
        StringBuilder query = new StringBuilder(String.join(" AND ", parts));
        for (String term : exclude) {
            query.append(" NOT ").append(quote(term));
        }
        return query.toString();
    }

    /** Same search on the GMU library site. */
    public String gmuSearchUrl() {
        StringBuilder url = new StringBuilder("https://wrlc-gm.primo.exlibrisgroup.com/discovery/search")
                .append("?query=").append(encode("any,contains," + booleanQuery()))
                .append("&search_scope=MyInst_and_CI")
                .append("&vid=").append(encode("01WRLC_GML:01WRLC_GML"))
                .append("&facet=").append(encode("rtype,include,articles"));
        if (peerReviewedOnly) {
            url.append("&facet=").append(encode("tlevel,include,peer_reviewed"));
        }
        return url.toString();
    }

    // Quote phrases.
    private static String quote(String term) {
        return term.contains(" ") ? "\"" + term + "\"" : term;
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
