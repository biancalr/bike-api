package br.com.bikeapi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.bikeapi.service.EmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	@Value("${application.mail.default-sender}")
	private String remetente;
	
	private final JavaMailSender javaMailSender;
	
	@Override
	public void sendEmail(String message, List<String> mailsList) {

		String[] mails = mailsList.toArray(new String[mailsList.size()]);
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		
		mailMessage.setFrom(remetente);
		mailMessage.setSubject("Bicicleta com aluguel atrasado");
		mailMessage.setText(message);
		mailMessage.setTo(mails);
		
		javaMailSender.send(mailMessage);
		
	}

	
}
