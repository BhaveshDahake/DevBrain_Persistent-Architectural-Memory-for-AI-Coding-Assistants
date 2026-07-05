package com.example.DevBrain.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;

@Component
public class StartupPortGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupPortGuard.class);
    private static final int STARTUP_TIMEOUT_SECONDS = 30;

    private final Environment environment;

    public StartupPortGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        int port = Integer.parseInt(environment.getProperty("server.port", "8080"));
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(STARTUP_TIMEOUT_SECONDS).toMillis();

        while (System.currentTimeMillis() < deadline) {
            try (Socket probe = new Socket(InetAddress.getByName("127.0.0.1"), port)) {
                log.info("Port {} is already occupied by an active listener. Reusing the running instance.", port);
                return;
            } catch (IOException ex) {
                try (ServerSocket socket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))) {
                    socket.setReuseAddress(true);
                    log.info("Port {} is available for startup.", port);
                    return;
                } catch (IOException bindEx) {
                    if (System.currentTimeMillis() >= deadline) {
                        log.warn("Startup blocked waiting for port {}. Reason: {}", port, bindEx.getMessage());
                        return;
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        log.warn("Startup could not continue within {} seconds because port {} remained blocked. Reason: check for an existing process or choose a different port.", STARTUP_TIMEOUT_SECONDS, port);
    }
}
