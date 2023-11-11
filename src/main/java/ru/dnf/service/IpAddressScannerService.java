package ru.dnf.service;

import java.net.URI;
import java.util.List;

public interface IpAddressScannerService {

    List<String> findDomainNamesFromCertificatesByAddress(URI uri);

    List<String> findDomainNamesFromCertificatesByAddress(String address);

    List<String> getAddressesListFromNetwork(String network);
}
