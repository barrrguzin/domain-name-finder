package ru.dnf.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dnf.service.IpAddressScannerService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class ForkJoinDomainNameFinderTask extends RecursiveTask<List<String>> {

    Logger log = LoggerFactory.getLogger(ForkJoinDomainNameFinderTask.class);

    List<String> addresses;
    Integer size;
    Integer threadsCount;
    IpAddressScannerService ipAddressScannerService;


    public ForkJoinDomainNameFinderTask(List<String> addresses, Integer threadsCount, IpAddressScannerService ipAddressScannerService) {
        this.addresses = addresses;
        this.threadsCount = threadsCount;
        this.size = addresses.size();
        this.ipAddressScannerService = ipAddressScannerService;
    }

    @Override
    protected List<String> compute() {
        if (threadsCount > size) {
            threadsCount = size;
        }

        if (threadsCount > 1) {
            List<String> firstPart = new ArrayList<>(addresses.subList(0, (size + 1)/2));
            List<String> secondPart = new ArrayList<>(addresses.subList((size + 1)/2, size));
            ForkJoinDomainNameFinderTask firstDomainNamesPart = new ForkJoinDomainNameFinderTask(firstPart, threadsCount/2, ipAddressScannerService);
            firstDomainNamesPart.fork();
            ForkJoinDomainNameFinderTask secondDomainNamesPart = new ForkJoinDomainNameFinderTask(secondPart, threadsCount/2, ipAddressScannerService);
            List<String> secondPartOfDomainNames = secondDomainNamesPart.compute();
            List<String> firstPartOfDomainNames = firstDomainNamesPart.join();
            firstPartOfDomainNames.addAll(secondPartOfDomainNames);
            return firstPartOfDomainNames;

        } else {
            List<String> domainNamesFromCertificates = new ArrayList<>();
            for (String address : addresses) {
                List<String> domainNamesFromCertificate = ipAddressScannerService.findDomainNamesFromCertificatesByAddress(address);
                domainNamesFromCertificates.addAll(domainNamesFromCertificate);
            }
            return domainNamesFromCertificates;
        }
    }
}
