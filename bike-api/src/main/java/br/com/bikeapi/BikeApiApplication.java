package br.com.bikeapi;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BikeApiApplication {
	
//	@Autowired
//	private EmailService emailService;
	
//	@Value("${application.name}")
//	public String applicationName;
//	
//	@Bean
//	public CommandLineRunner runner() {
//		return args -> {
//			System.out.println(applicationName);
//		};
//	}
	
//	@Scheduled(cron = "0 0/1 * 1/1 * ?")
//	public void testeAgendamentoTarefa() {
//		System.out.println("AGENDAMENTO FUNCIONANDO");
//	}
	
//	@Bean
//	public CommandLineRunner runner() {
//		return args -> {
//			List<String> emails = Arrays.asList("library-api-297893@inbox.mailtrap.io");
//			emailService.sendEmail("Testando servico de e-mails.", emails);
//			System.out.println("e-mails enviados");
//		};
//	}

	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	/*
	 * Swagger-url http://localhost:8080/swagger-ui.html
	 */
	public static void main(String[] args) {
		SpringApplication.run(BikeApiApplication.class, args);
	}

}
