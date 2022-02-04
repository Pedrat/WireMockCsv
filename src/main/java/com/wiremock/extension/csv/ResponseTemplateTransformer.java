//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wiremock.extension.csv;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.ProxyResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.TextFile;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.extension.StubLifecycleListener;
import com.github.tomakehurst.wiremock.extension.responsetemplating.HandlebarsOptimizedTemplate;
import com.github.tomakehurst.wiremock.extension.responsetemplating.HttpTemplateCacheKey;
import com.github.tomakehurst.wiremock.extension.responsetemplating.RequestTemplateModel;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wiremock.com.fasterxml.jackson.databind.JsonNode;
import wiremock.com.github.jknack.handlebars.Handlebars;
import wiremock.com.github.jknack.handlebars.Helper;
import wiremock.com.google.common.base.MoreObjects;
import wiremock.com.google.common.collect.ImmutableList;
import wiremock.com.google.common.collect.ImmutableMap;
import wiremock.com.google.common.collect.ImmutableSet;
import wiremock.com.google.common.collect.Iterables;

public class ResponseTemplateTransformer extends ResponseDefinitionTransformer implements StubLifecycleListener {
    public static final String NAME = "response-template";
    private final boolean global;
    private final TemplateEngine templateEngine;

    public static ResponseTemplateTransformer.Builder builder() {
        return new ResponseTemplateTransformer.Builder();
    }

    public ResponseTemplateTransformer(boolean global) {
        this(global, Collections.emptyMap());
    }

    public ResponseTemplateTransformer(boolean global, String helperName, Helper<?> helper) {
        this(global, ImmutableMap.of(helperName, helper));
    }

    public ResponseTemplateTransformer(boolean global, Map<String, Helper<?>> helpers) {
        this(global, new Handlebars(), helpers, (Long) null, (Set) null);
    }

    public ResponseTemplateTransformer(boolean global, Handlebars handlebars, Map<String, Helper<?>> helpers, Long maxCacheEntries, Set<String> permittedSystemKeys) {
        this.global = global;
        this.templateEngine = new TemplateEngine(handlebars, helpers, maxCacheEntries, permittedSystemKeys);
    }

    public boolean applyGlobally() {
        return this.global;
    }

    public String getName() {
        return "response-template";
    }

    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        ResponseDefinitionBuilder newResponseDefBuilder = ResponseDefinitionBuilder.like(responseDefinition);
        ImmutableMap<Object, Object> model = ImmutableMap.builder().put("parameters", MoreObjects.firstNonNull(parameters, Collections.emptyMap())).put("request", RequestTemplateModel.from(request)).putAll(this.addExtraModelElements(request, responseDefinition, files, parameters)).build();
        HandlebarsOptimizedTemplate proxyBaseUrlTemplate;
        String newProxyBaseUrl;
        if (responseDefinition.specifiesTextBodyContent()) {
            boolean isJsonBody = responseDefinition.getJsonBody() != null;
            HandlebarsOptimizedTemplate bodyTemplate = this.templateEngine.getTemplate(HttpTemplateCacheKey.forInlineBody(responseDefinition), responseDefinition.getTextBody());
            this.applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate, isJsonBody);
        } else if (responseDefinition.specifiesBodyFile()) {
            proxyBaseUrlTemplate = this.templateEngine.getUncachedTemplate(responseDefinition.getBodyFileName());
            newProxyBaseUrl = this.uncheckedApplyTemplate(proxyBaseUrlTemplate, model);
            boolean disableBodyFileTemplating = parameters.getBoolean("disableBodyFileTemplating", false);
            if (disableBodyFileTemplating) {
                newResponseDefBuilder.withBodyFile(newProxyBaseUrl);
            } else {
                TextFile file = files.getTextFileNamed(newProxyBaseUrl);
                HandlebarsOptimizedTemplate bodyTemplate = this.templateEngine.getTemplate(HttpTemplateCacheKey.forFileBody(responseDefinition, newProxyBaseUrl), file.readContentsAsString());
                this.applyTemplatedResponseBody(newResponseDefBuilder, model, bodyTemplate, false);
            }
        }

        if (responseDefinition.getHeaders() != null) {
            Iterable<HttpHeader> newResponseHeaders = Iterables.transform(responseDefinition.getHeaders().all(), (header) -> {
                wiremock.com.google.common.collect.ImmutableList.Builder<String> valueListBuilder = ImmutableList.builder();
                int index = 0;
                Iterator var6 = header.values().iterator();

                while (var6.hasNext()) {
                    String headerValue = (String) var6.next();
                    HandlebarsOptimizedTemplate template = this.templateEngine.getTemplate(HttpTemplateCacheKey.forHeader(responseDefinition, header.key(), index++), headerValue);
                    valueListBuilder.add(this.uncheckedApplyTemplate(template, model));
                }

                return new HttpHeader(header.key(), valueListBuilder.build());
            });
            newResponseDefBuilder.withHeaders(new HttpHeaders(newResponseHeaders));
        }

        if (responseDefinition.getProxyBaseUrl() == null) {
            return newResponseDefBuilder.build();
        } else {
            proxyBaseUrlTemplate = this.templateEngine.getTemplate(HttpTemplateCacheKey.forProxyUrl(responseDefinition), responseDefinition.getProxyBaseUrl());
            newProxyBaseUrl = this.uncheckedApplyTemplate(proxyBaseUrlTemplate, model);
            ProxyResponseDefinitionBuilder newProxyResponseDefBuilder = newResponseDefBuilder.proxiedFrom(newProxyBaseUrl);
            if (responseDefinition.getAdditionalProxyRequestHeaders() != null) {
                Iterable<HttpHeader> newResponseHeaders = Iterables.transform(responseDefinition.getAdditionalProxyRequestHeaders().all(), (header) -> {
                    wiremock.com.google.common.collect.ImmutableList.Builder<String> valueListBuilder = ImmutableList.builder();
                    int index = 0;
                    Iterator var6 = header.values().iterator();

                    while (var6.hasNext()) {
                        String headerValue = (String) var6.next();
                        HandlebarsOptimizedTemplate template = this.templateEngine.getTemplate(HttpTemplateCacheKey.forHeader(responseDefinition, header.key(), index++), headerValue);
                        valueListBuilder.add(this.uncheckedApplyTemplate(template, model));
                    }

                    return new HttpHeader(header.key(), valueListBuilder.build());
                });
                HttpHeaders proxyHttpHeaders = new HttpHeaders(newResponseHeaders);
                Iterator var12 = proxyHttpHeaders.keys().iterator();

                while (var12.hasNext()) {
                    String key = (String) var12.next();
                    newProxyResponseDefBuilder.withAdditionalRequestHeader(key, proxyHttpHeaders.getHeader(key).firstValue());
                }
            }

            return newProxyResponseDefBuilder.build();
        }
    }

    protected Map<String, Object> addExtraModelElements(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
        return Collections.emptyMap();
    }

    private void applyTemplatedResponseBody(ResponseDefinitionBuilder newResponseDefBuilder, ImmutableMap<Object, Object> model, HandlebarsOptimizedTemplate bodyTemplate, boolean isJsonBody) {
        String newBody = this.uncheckedApplyTemplate(bodyTemplate, model);
        if (isJsonBody) {
            newResponseDefBuilder.withJsonBody((JsonNode) Json.read(newBody, JsonNode.class));
        } else {
            newResponseDefBuilder.withBody(newBody);
        }

    }

    private String uncheckedApplyTemplate(HandlebarsOptimizedTemplate template, Object context) {
        return template.apply(context);
    }

    public void beforeStubCreated(StubMapping stub) {
    }

    public void afterStubCreated(StubMapping stub) {
    }

    public void beforeStubEdited(StubMapping oldStub, StubMapping newStub) {
    }

    public void afterStubEdited(StubMapping oldStub, StubMapping newStub) {
    }

    public void beforeStubRemoved(StubMapping stub) {
    }

    public void afterStubRemoved(StubMapping stub) {
        this.templateEngine.invalidateCache();
    }

    public void beforeStubsReset() {
    }

    public void afterStubsReset() {
        this.templateEngine.invalidateCache();
    }

    public long getCacheSize() {
        return this.templateEngine.getCacheSize();
    }

    public Long getMaxCacheEntries() {
        return this.templateEngine.getMaxCacheEntries();
    }

    public static class Builder {
        private boolean global = true;
        private Handlebars handlebars = new Handlebars();
        private Map<String, Helper<?>> helpers = new HashMap();
        private Long maxCacheEntries = null;
        private Set<String> permittedSystemKeys = null;

        public Builder() {
        }

        public ResponseTemplateTransformer.Builder global(boolean global) {
            this.global = global;
            return this;
        }

        public ResponseTemplateTransformer.Builder handlebars(Handlebars handlebars) {
            this.handlebars = handlebars;
            return this;
        }

        public ResponseTemplateTransformer.Builder helpers(Map<String, Helper<?>> helpers) {
            this.helpers = helpers;
            return this;
        }

        public ResponseTemplateTransformer.Builder helper(String name, Helper<?> helper) {
            this.helpers.put(name, helper);
            return this;
        }

        public ResponseTemplateTransformer.Builder maxCacheEntries(Long maxCacheEntries) {
            this.maxCacheEntries = maxCacheEntries;
            return this;
        }

        public ResponseTemplateTransformer.Builder permittedSystemKeys(Set<String> keys) {
            this.permittedSystemKeys = keys;
            return this;
        }

        public ResponseTemplateTransformer.Builder permittedSystemKeys(String... keys) {
            this.permittedSystemKeys = ImmutableSet.copyOf(keys);
            return this;
        }

        public ResponseTemplateTransformer build() {
            return new ResponseTemplateTransformer(this.global, this.handlebars, this.helpers, this.maxCacheEntries, this.permittedSystemKeys);
        }
    }
}
