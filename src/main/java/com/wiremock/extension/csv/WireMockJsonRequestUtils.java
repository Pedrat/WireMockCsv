package com.wiremock.extension.csv;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.jayway.jsonpath.JsonPath;
import com.wiremock.extension.csv.ConfigHandler.RequestConfigHandler;
import wiremock.com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public final class WireMockJsonRequestUtils {

    private WireMockJsonRequestUtils() {
    }

    /**
     * Remplace les variables dans la requête SQL.
     *
     * @param querySQL
     * @param requestConfig
     * @return la nouvelle requete SQL
     */
    public static String replaceQueryVariables(final String querySQL, final RequestConfigHandler requestConfig) {
        String newQuerySQL = querySQL;
        ResponseTemplateTransformer rt = new ResponseTemplateTransformer(true);
        final HashSet<String> done = new HashSet<>();
        // Standard replacement
        Matcher m = Pattern.compile("\\$\\{\\s*(\\$[^\\s^\\}]*)\\s*\\}").matcher(newQuerySQL);
        Matcher m2 = Pattern.compile("\\{\\{\\s*([^\\s^\\}]*)\\s*\\}\\}").matcher(newQuerySQL);
        while (m2.find()) {
            final String paramName = m2.group(1);
            if (!done.contains(paramName)) {
                Request request = requestConfig.getRequest();
                ResponseDefinition responseDefinition = rt.transform(request, aResponse().withBody("{{" + paramName + "}}").build(), new FileSourceImpl(), Parameters.empty());
                System.out.println(responseDefinition.getBody());
                newQuerySQL = newQuerySQL.replaceAll("\\{\\{\\s*" + Pattern.quote(m2.group(1)) + "\\s*\\}\\}", responseDefinition.getBody());
                System.out.println("in newQuerySQL " + newQuerySQL);
                done.add(paramName);
            }
        }
        while (m.find()) {
            final String paramName = m.group(1);
            if (!done.contains(paramName)) {
                Request request = requestConfig.getRequest();
                Body body = new Body(request.getBody());
                JsonNode jsonNode = body.asJson();
                ArrayList<String> paramObject = JsonPath.read(jsonNode.toString(), paramName);
                String paramValue = paramObject.size() > 0 ? paramObject.get(0) : "";
                newQuerySQL = newQuerySQL.replaceAll("\\$\\{\\s*" + Pattern.quote(m.group(1)) + "\\s*\\}", paramValue.toString());
                System.out.println("in newQuerySQL " + newQuerySQL);
                done.add(paramName);
            }
        }

        done.clear();
        newQuerySQL = WireMockCsvUtils.replaceQueryVariables(newQuerySQL, requestConfig);

        return newQuerySQL;
    }

    /**
     * Computes files root to use between runner and csv-root-dir system property. Don't use WireMockCsvServerRunner.filesRoot() directly.
     */
    public static String getFilesRoot() {
        return System.getProperty("csv-root-dir",
                WireMockCsvServerRunner.filesRoot() == null ? "." : WireMockCsvServerRunner.filesRoot());
    }
}
