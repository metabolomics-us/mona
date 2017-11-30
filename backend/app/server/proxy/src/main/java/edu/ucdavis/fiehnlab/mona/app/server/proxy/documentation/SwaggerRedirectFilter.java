//package edu.ucdavis.fiehnlab.mona.app.server.proxy.documentation;
//
//import com.netflix.config.DynamicIntProperty;
//import com.netflix.config.DynamicPropertyFactory;
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.constants.ZuulConstants;
//import com.netflix.zuul.context.RequestContext;
//import org.apache.http.Header;
//import org.apache.http.HttpHost;
//import org.apache.http.HttpRequest;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.params.ClientPNames;
//import org.apache.http.conn.ClientConnectionManager;
//import org.apache.http.conn.scheme.PlainSocketFactory;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
//import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.message.BasicHttpRequest;
//import org.apache.http.params.CoreConnectionPNames;
//import org.apache.http.params.HttpParams;
//import org.apache.http.protocol.HttpContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
//import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.util.StringUtils;
//import springfox.documentation.swagger2.web.Swagger2Controller;
//
//import javax.annotation.PreDestroy;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.security.KeyStore;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * Created by wohlgemuth on 6/14/16.
// */
//public class SwaggerRedirectFilter extends ZuulFilter {
//    private static final Logger log = LoggerFactory.getLogger(SwaggerRedirectFilter.class);
//
//    public static final String CONTENT_ENCODING = "Content-Encoding";
//
//    private static final Runnable CLIENTLOADER = new Runnable() {
//        @Override
//        public void run() {
//            loadClient();
//        }
//    };
//
//    private static final DynamicIntProperty SOCKET_TIMEOUT = DynamicPropertyFactory
//            .getInstance().getIntProperty(ZuulConstants.ZUUL_HOST_SOCKET_TIMEOUT_MILLIS,
//                    10000);
//
//    private static final DynamicIntProperty CONNECTION_TIMEOUT = DynamicPropertyFactory
//            .getInstance().getIntProperty(ZuulConstants.ZUUL_HOST_CONNECT_TIMEOUT_MILLIS,
//                    2000);
//
//    private static final AtomicReference<HttpClient> CLIENT = new AtomicReference<HttpClient>(
//            newClient());
//
//    private static final Timer CONNECTION_MANAGER_TIMER = new Timer(
//            "SimpleHostRoutingFilter.CONNECTION_MANAGER_TIMER", true);
//
//    // cleans expired connections at an interval
//    static {
//        SOCKET_TIMEOUT.addCallback(CLIENTLOADER);
//        CONNECTION_TIMEOUT.addCallback(CLIENTLOADER);
//        CONNECTION_MANAGER_TIMER.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    final HttpClient hc = CLIENT.get();
//                    if (hc == null) {
//                        return;
//                    }
//                    hc.getConnectionManager().closeExpiredConnections();
//                } catch (Throwable ex) {
//                    log.error("error closing expired connections", ex);
//                }
//            }
//        }, 30000, 5000);
//    }
//
//    private ProxyRequestHelper helper;
//
//    public SwaggerRedirectFilter() {
//        this(new ProxyRequestHelper());
//    }
//
//    public SwaggerRedirectFilter(ProxyRequestHelper helper) {
//        this.helper = helper;
//    }
//
//    @PreDestroy
//    public void stop() {
//        CONNECTION_MANAGER_TIMER.cancel();
//    }
//
//    @Override
//    public String filterType() {
//        return "route";
//    }
//
//    @Override
//    public int filterOrder() {
//        return 102;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return RequestContext.getCurrentContext().getRequest().getRequestURI().endsWith(Swagger2Controller.DEFAULT_URL);
//    }
//
//    @Override
//    public Object run() {
//        RequestContext context = RequestContext.getCurrentContext();
//        HttpServletRequest request = context.getRequest();
//        MultiValueMap<String, String> headers = this.helper
//                .buildZuulRequestHeaders(request);
//        MultiValueMap<String, String> params = this.helper
//                .buildZuulRequestQueryParams(request);
//        String verb = getVerb(request);
//        InputStream requestEntity = getRequestBody(request);
//        HttpClient httpclient = CLIENT.get();
//
//        String uri = this.helper.buildZuulRequestURI(request);
//
//        try {
//            HttpResponse response = forward(httpclient, verb, uri, request, headers,
//                    params, requestEntity);
//            setResponse(response);
//        } catch (Exception ex) {
//            context.set("error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            context.set("error.exception", ex);
//        }
//        return null;
//    }
//
//    private HttpResponse forward(HttpClient httpclient, String verb, String uri,
//                                 HttpServletRequest request, MultiValueMap<String, String> headers,
//                                 MultiValueMap<String, String> params, InputStream requestEntity)
//            throws Exception {
//
//        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
//                requestEntity);
//        URL host = RequestContext.getCurrentContext().getRouteHost();
//
//        HttpHost httpHost = getHttpHost(host);
//        uri = StringUtils.cleanPath(host.getPath()/* + uri*/);
//
//        HttpRequest httpRequest = new BasicHttpRequest(verb, uri );
//
//        log.debug("verb: " + verb);
//        log.debug("uri: " + uri);
//        log.debug("host: " + host);
//        try {
//            httpRequest.setHeaders(convertHeaders(headers));
//            HttpResponse zuulResponse = forwardRequest(httpclient, httpHost, httpRequest);
//            return zuulResponse;
//        } finally {
//            // When HttpClient instance is no longer needed,
//            // shut down the connection manager to ensure
//            // immediate deallocation of all system resources
//            // httpclient.getConnectionManager().shutdown();
//        }
//    }
//
//    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
//        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
//        for (Header header : headers) {
//            String name = header.getName();
//            if (!map.containsKey(name)) {
//                map.put(name, new ArrayList<String>());
//            }
//            map.get(name).add(header.getValue());
//        }
//        return map;
//    }
//
//    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
//        List<Header> list = new ArrayList<>();
//        for (String name : headers.keySet()) {
//            for (String value : headers.get(name)) {
//                list.add(new BasicHeader(name, value));
//            }
//        }
//        return list.toArray(new BasicHeader[0]);
//    }
//
//    private HttpResponse forwardRequest(HttpClient httpclient, HttpHost httpHost,
//                                        HttpRequest httpRequest) throws IOException {
//
//
//        httpclient.execute(httpHost, httpRequest).getEntity().writeTo(System.out);
//
//        return httpclient.execute(httpHost, httpRequest);
//    }
//
//    private HttpHost getHttpHost(URL host) {
//        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
//                host.getProtocol());
//        return httpHost;
//    }
//
//    private InputStream getRequestBody(HttpServletRequest request) {
//        InputStream requestEntity = null;
//        try {
//            requestEntity = request.getInputStream();
//        } catch (IOException ex) {
//            // no requestBody is ok.
//        }
//        return requestEntity;
//    }
//
//    private String getVerb(HttpServletRequest request) {
//        String sMethod = request.getMethod();
//        return sMethod.toUpperCase();
//    }
//
//    private void setResponse(HttpResponse response) throws IOException {
//        this.helper.setResponse(response.getStatusLine().getStatusCode(),
//                response.getEntity() == null ? null : response.getEntity().getContent(),
//                revertHeaders(response.getAllHeaders()));
//
//    }
//
//    private static ClientConnectionManager newConnectionManager() throws Exception {
//        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        trustStore.load(null, null);
//        SSLSocketFactory sf = new SimpleHostRoutingFilter.MySSLSocketFactory(trustStore);
//        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//        SchemeRegistry registry = new SchemeRegistry();
//        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        registry.register(new Scheme("https", sf, 443));
//        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(registry);
//        cm.setMaxTotal(Integer.parseInt(System.getProperty("zuul.max.host.connections",
//                "200")));
//        cm.setDefaultMaxPerRoute(Integer.parseInt(System.getProperty(
//                "zuul.max.host.connections", "20")));
//        return cm;
//    }
//
//    private static void loadClient() {
//        final HttpClient oldClient = CLIENT.get();
//        CLIENT.set(newClient());
//        if (oldClient != null) {
//            CONNECTION_MANAGER_TIMER.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    try {
//                        oldClient.getConnectionManager().shutdown();
//                    } catch (Throwable ex) {
//                        log.error("error shutting down old connection manager", ex);
//                    }
//                }
//            }, 30000);
//        }
//    }
//
//    private static HttpClient newClient() {
//        // I could statically cache the connection manager but we will probably want to
//        // make some of its properties
//        // dynamic in the near future also
//        try {
//            DefaultHttpClient httpclient = new DefaultHttpClient(newConnectionManager());
//            HttpParams httpParams = httpclient.getParams();
//            httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
//                    SOCKET_TIMEOUT.get());
//            httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
//                    CONNECTION_TIMEOUT.get());
//            httpclient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0,
//                    false));
//            httpParams.setParameter(ClientPNames.COOKIE_POLICY,
//                    org.apache.http.client.params.CookiePolicy.IGNORE_COOKIES);
//            httpclient.setRedirectStrategy(new org.apache.http.client.RedirectStrategy() {
//                @Override
//                public boolean isRedirected(HttpRequest httpRequest,
//                                            HttpResponse httpResponse, HttpContext httpContext) {
//                    return false;
//                }
//
//                @Override
//                public org.apache.http.client.methods.HttpUriRequest getRedirect(
//                        HttpRequest httpRequest, HttpResponse httpResponse,
//                        HttpContext httpContext) {
//                    return null;
//                }
//            });
//            return httpclient;
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//}
