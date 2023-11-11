package ru.dnf.service.impl;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dnf.configuration.HttpClientFactory;
import ru.dnf.service.IpAddressScannerService;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

public class IpAddressScannerServiceImpl implements IpAddressScannerService {

    Logger log = LoggerFactory.getLogger(IpAddressScannerServiceImpl.class);

    private static final IpAddressScannerServiceImpl instance = new IpAddressScannerServiceImpl();

    private IpAddressScannerServiceImpl() {}

    public static IpAddressScannerServiceImpl getInstance() {
        return instance;
    }


    @Override
    public List<String> findDomainNamesFromCertificatesByAddress(URI uri) {
        List<String> uriDomainNameList = new ArrayList<>();
        HttpGet httpGet = new HttpGet(uri);
        HttpContext context= new BasicHttpContext();
        try (
                CloseableHttpClient client = HttpClientFactory.getInstance()
                        .getTrustAllSslCertificatesCloseableHttpClient();
                CloseableHttpResponse response = (CloseableHttpResponse) client.execute(httpGet, context)
        ) {

            SSLSession sslSession = (SSLSession) context.getAttribute(HttpCoreContext.SSL_SESSION);
            X509Certificate[] certificates = (X509Certificate[]) sslSession.getPeerCertificates();

            for (X509Certificate certificate : certificates) {
                List<String> domainNameList = getCertificatesDomainNameList(certificate);
                uriDomainNameList.addAll(domainNameList);
            }
            return uriDomainNameList;
        } catch (Exception e) {
            log.debug("Error while sending https request: " + e.getMessage());
            return uriDomainNameList;
        }
    }

    @Override
    public List<String> findDomainNamesFromCertificatesByAddress(String address) {
        try {
            return findDomainNamesFromCertificatesByAddress(new URI("https://"+address));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to make URI from string: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAddressesListFromNetwork(String network) {
        SubnetUtils utils = new SubnetUtils(network);
        List<String> addresses = Arrays.stream(utils.getInfo().getAllAddresses())
                .collect(Collectors.toList());
        return addresses;
    }

    private List<String> getCertificatesDomainNameList(X509Certificate certificate) {
        try {
            Collection<List<?>> alternativeNames = certificate.getSubjectAlternativeNames();
            List<String> domainNameList = new ArrayList<>();
            if (alternativeNames != null) {
                for (List<?> alternativeNameList : alternativeNames) {
                    String domainName = alternativeNameList.get(1).toString();
                    domainNameList.add(domainName);
                }
            }
            return domainNameList;
        } catch (CertificateParsingException e) {
            throw new RuntimeException("Error while getting domain names from certificate: " + e.getMessage());
        }
    }
}
