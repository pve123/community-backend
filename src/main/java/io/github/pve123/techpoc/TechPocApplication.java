package io.github.pve123.techpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TechPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechPocApplication.class, args);
    }
}
