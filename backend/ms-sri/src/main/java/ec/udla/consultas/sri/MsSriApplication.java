package ec.udla.consultas.sri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "ec.udla.consultas")
@ConfigurationPropertiesScan
public class MsSriApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsSriApplication.class, args);
    }
}
