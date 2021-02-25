package br.com.bikeapi.api.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientDTO {
	
	private Long id;
	
	@NotEmpty
	@Size(max = 100, min = 3)
	private String nome;
	
	@NotEmpty
	@CPF
	private String cpf;

}
