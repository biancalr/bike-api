package br.com.bikeapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import br.com.bikeapi.model.entity.Rent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	/**
	 * <p>
	 * O valor da variavel significa que para cada aluguel atrasado, sera enviado um
	 * email comunicando o atraso ao cliente.
	 * </p>
	 * <p>Será enviado um e-mail a cada 10 minutos, de acordo com o cronmaker</p>
	 * <p>
	 * Lembrando que o estado dos alugueis pode
	 * mudar constantemente.
	 * </p>
	 * 
	 * @see {@link www.cronmaker.com}  foi utilizado para criar essa cron
	 *      expression.
	 */
	public static final String CRON_LATE_RENTS = "0 0/10 * 1/1 * ?";
//	public static final String CRON_LATE_RENTS = "0 0/1 * 1/1 * ?";	
	@Value("${application.mail.laterents.message}")
	private String message;
	private final RentService rentService;
	private final EmailService emailService;

	@Scheduled(cron = CRON_LATE_RENTS)
	public void sendMailToAllLateRents() {
		List<Rent> allLateRents = rentService.getAllLateRents();
		List<String> mailsList = allLateRents.stream()
				.map(rent -> rent.getCustomerEmail())
				.collect(Collectors.toList());
		if (!mailsList.isEmpty() || mailsList != null) {
			emailService.sendEmail(message, mailsList);
		}
		
	}

}
