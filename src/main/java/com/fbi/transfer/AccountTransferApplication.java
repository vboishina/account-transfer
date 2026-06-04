package com.fbi.transfer;

import liquibase.Liquibase;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AccountTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountTransferApplication.class, args);
	}

	@Bean
	public ApplicationRunner runLiquibase(Liquibase liquibase) {
		return args -> {
			liquibase.update();
			System.out.println("Liquibase executed!");
		};
	}
}