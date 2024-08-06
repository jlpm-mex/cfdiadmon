package net.ellapiz.admoncfdiprov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
@EntityScan(basePackages={"net.ellapiz.admoncfdiprov*","net.ellapiz.proveedor.*"})
public class AdmonCfdiBack {

	public static void main(String[] args) {
		SpringApplication.run(AdmonCfdiBack.class, args);
	}

}
