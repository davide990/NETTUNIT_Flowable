package nettunit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * --- ENTRY POINT OF THE NETTUNIT PLATFORM ---
 * <p>
 * This method will launch nettunit as a spring application, solve all dependencies, and run the
 * rabbitmq broker for messaging with Jixel
 */
@SpringBootApplication
public class ApplicationLauncher {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationLauncher.class, args);
    }
}