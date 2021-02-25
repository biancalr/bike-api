package br.com.bikeapi.api.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentDTO {
	
	private Long id;
	
	@NotEmpty
	@Email
	private String customerEmail;
		
	@NotNull
	@Builder.Default
	private Integer rentHoursDuration = 0;
	
	@NotEmpty
	@Size(min = 6)
	private String chassi;
	
	@NotEmpty
	@CPF
	private String cpf;
	
	private ClientDTO cliente;
	
	private BikeDTO bike;
}
