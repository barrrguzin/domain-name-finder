package ru.dnf.controller;

import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dnf.service.IpAddressScannerService;
import ru.dnf.service.impl.IpAddressScannerServiceImpl;
import ru.dnf.task.ForkJoinDomainNameFinderTask;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class IpAddressScannerController {

    private static Logger log = LoggerFactory.getLogger(IpAddressScannerController.class);
    private static IpAddressScannerService ipAddressScannerService = IpAddressScannerServiceImpl.getInstance();
    private static final String NETWORK_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\/(3[0-2]|[1-2]?\\d)$";


    public static final Handler getScanUriPage = context -> {
        context.redirect("/index.html");
    };

    public static final Handler scanUri = context -> {

        Integer threads = Integer.parseInt(context.formParam("threads"));
        String network = context.formParam("network");

        Boolean isNetwork = network.matches(NETWORK_REGEX);

        if (!isNetwork || threads <= 0 || network.contains("0.0.0.0")) {
            context.redirect("/index.html");
        } else {
            List<String> addresses = ipAddressScannerService.getAddressesListFromNetwork(network);
            ForkJoinDomainNameFinderTask task = new ForkJoinDomainNameFinderTask(addresses, threads, ipAddressScannerService);
            ForkJoinPool forkJoinPool = new ForkJoinPool(threads);
            List<String> domains = forkJoinPool.invoke(task);
            byte[] file = getDomainNamesByteArray(domains);

            context.header("Content-Length", String.valueOf(file.length));
            context.header("Content-Disposition", "attachment; filename=\"DN List from " + network + ".txt\"");
            context.header("Content-Type", "application/octet-stream");
            context.result(file);
        }
    };

    private static byte[] getDomainNamesByteArray(List<String> domains) {
        StringBuilder builder = new StringBuilder();
        domains.forEach(domainName -> {
            builder.append(domainName);
            builder.append("\n");
        });
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
