package ru.dnf.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientFactory {

    private static Logger log = LoggerFactory.getLogger(HttpClientFactory.class);

    private static HttpClientFactory instance = new HttpClientFactory();

    public static HttpClientFactory getInstance() {
        return instance;
    }

    private HttpClientFactory() {
    }

    private BasicHttpClientConnectionManager configureTrustAllConnectionManager() {
        try {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory sslsf =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory> create()
                            .register("https", sslsf)
                            .register("http", new PlainConnectionSocketFactory())
                            .build();
            return new BasicHttpClientConnectionManager(socketFactoryRegistry);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            log.error("Unable to setup connectionManager: " + e.getMessage());
            throw new RuntimeException("Unable to setup connectionManager: " + e.getMessage());
        }
    }

    public CloseableHttpClient getCloseableHttpClient() {
        return HttpClients.createDefault();
    }

    public CloseableHttpClient getTrustAllSslCertificatesCloseableHttpClient() {
        BasicHttpClientConnectionManager trustAllConnectionManager = configureTrustAllConnectionManager();
        return HttpClients.custom()
                .setConnectionManager(trustAllConnectionManager)
                .build();
    }

}
