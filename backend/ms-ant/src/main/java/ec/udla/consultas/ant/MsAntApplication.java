package ec.udla.consultas.ant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "ec.udla.consultas")
@ConfigurationPropertiesScan
public class MsAntApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAntApplication.class, args);
    }
}
