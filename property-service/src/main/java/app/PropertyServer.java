package app;

import io.javalin.Javalin;

public class PropertyServer {
    public static void main(String[] args) {
        Javalin.create().start(8080);
    }
}
