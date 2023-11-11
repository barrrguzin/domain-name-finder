package ru.dnf.interceptor;

import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.security.cert.Certificate;

public class ResponseCertificateScannerInterceptor implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse httpResponse, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
        ManagedHttpClientConnection routedConnection = (ManagedHttpClientConnection) httpContext.getAttribute(HttpCoreContext.SSL_SESSION);
        SSLSession sslSession = routedConnection.getSSLSession();
        if (sslSession != null) {
            Certificate[] certificates = sslSession.getPeerCertificates();
            System.out.println(certificates.length);
            // Assume that PEER_CERTIFICATES is a constant you've defined
            //httpContext.setAttribute(PEER_CERTIFICATES, certificates);
        }
    }
}
