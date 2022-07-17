package dev.voidframework.web.server;

import com.google.common.collect.ImmutableList;
import dev.voidframework.web.http.Cookie;
import dev.voidframework.web.http.HttpRequest;
import dev.voidframework.web.http.HttpRequestBodyContent;
import dev.voidframework.web.routing.HttpMethod;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;

import java.io.InputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Undertow {@link HttpRequest} implementation.
 */
public final class UndertowRequest implements HttpRequest {

    private final HttpServerExchange httpServerExchange;
    private final HttpRequestBodyContent httpRequestBodyContent;

    private Map<String, List<String>> headerMapCache;

    /**
     * Build a new instance.
     *
     * @param httpServerExchange     Current Http server exchange
     * @param httpRequestBodyContent Current Http request body content
     */
    public UndertowRequest(final HttpServerExchange httpServerExchange,
                           final HttpRequestBodyContent httpRequestBodyContent) {

        this.httpServerExchange = httpServerExchange;
        this.httpRequestBodyContent = httpRequestBodyContent;
    }

    @Override
    public String getCharset() {

        return httpServerExchange.getRequestCharset();
    }

    @Override
    public Cookie getCookie(final String cookieName) {

        final io.undertow.server.handlers.Cookie c = this.httpServerExchange.getRequestCookie(cookieName);

        return c != null
            ? Cookie.of(c.getName(), c.getValue())
            : null;
    }

    @Override
    public boolean acceptContentType(final String contentType) {

        final HeaderValues acceptHeaderValues = this.httpServerExchange.getRequestHeaders().get("Accept");
        if (acceptHeaderValues == null || acceptHeaderValues.isEmpty()) {
            return false;
        }

        for (final String value : acceptHeaderValues.get(0).split(",")) {
            if (value.contains(contentType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getHeader(final String headerName) {

        return this.httpServerExchange.getRequestHeaders().getFirst(headerName);
    }

    @Override
    public Map<String, List<String>> getHeaders() {

        if (this.headerMapCache == null) {
            this.headerMapCache = new HashMap<>();
            final Iterator<HeaderValues> iterator = this.httpServerExchange.getRequestHeaders().iterator();

            HeaderValues headerValues;
            while (iterator.hasNext()) {
                headerValues = iterator.next();
                this.headerMapCache.put(headerValues.getHeaderName().toString(), headerValues.subList(0, headerValues.size()));
            }
        }

        return this.headerMapCache;
    }

    @Override
    public HttpMethod getHttpMethod() {

        return HttpMethod.valueOf(this.httpServerExchange.getRequestMethod().toString());
    }

    @Override
    public InputStream getInputSteam() {

        return httpServerExchange.getInputStream();
    }

    @Override
    public String getQueryString() {

        return this.httpServerExchange.getQueryString();
    }

    @Override
    public String getQueryStringParameter(final String parameterName) {

        if (parameterName == null) {
            return null;
        }

        final Deque<String> parameetersValueDeque = this.httpServerExchange.getQueryParameters().get(parameterName);

        return parameetersValueDeque == null ? null : parameetersValueDeque.getFirst();
    }

    @Override
    public Map<String, List<String>> getQueryStringParameters() {

        final Map<String, List<String>> parametersPerKeyMap = new HashMap<>();

        for (final Map.Entry<String, Deque<String>> entrySet : this.httpServerExchange.getQueryParameters().entrySet()) {
            parametersPerKeyMap.put(entrySet.getKey(), ImmutableList.copyOf(entrySet.getValue()));
        }

        return parametersPerKeyMap;
    }

    @Override
    public String getRemoteHostName() {

        return this.httpServerExchange.getHostName();
    }

    @Override
    public String getRequestURL() {

        return this.httpServerExchange.getRequestURL();
    }

    @Override
    public String getRequestURI() {

        return this.httpServerExchange.getRequestURI();
    }

    @Override
    public HttpRequestBodyContent getBodyContent() {

        return this.httpRequestBodyContent;
    }
}
