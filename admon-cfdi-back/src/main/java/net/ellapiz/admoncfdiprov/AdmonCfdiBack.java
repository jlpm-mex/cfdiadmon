package net.ellapiz.admoncfdiprov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
@EntityScan(basePackages = {"net.ellapiz.admoncfdiprov.vo", "net.ellapiz.proveedor.vo"})
public class AdmonCfdiBack {

	public static void main(String[] args) {
		SpringApplication.run(AdmonCfdiBack.class, args);
	}

}
