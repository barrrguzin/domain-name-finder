package ru.dnf;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import ru.dnf.controller.IpAddressScannerController;



public class Application {

    public static void main(String[] args) {
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.addStaticFiles("public", Location.CLASSPATH);
        }).start(8080);
        app.get("/", IpAddressScannerController.getScanUriPage);
        app.post("/", IpAddressScannerController.scanUri);
    }

}
