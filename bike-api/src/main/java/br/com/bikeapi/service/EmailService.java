package br.com.bikeapi.service;

import java.util.List;

public interface EmailService {

	/**
	 * Envia o email.
	 * 
	 * @param message conteudo da mensagem
	 * @param mailsList lista de destinatarios
	 */
	void sendEmail(String message, List<String> mailsList);
	
}
