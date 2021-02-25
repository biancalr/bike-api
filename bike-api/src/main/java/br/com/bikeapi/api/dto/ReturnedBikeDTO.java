package br.com.bikeapi.api.dto;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.br.CPF;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnedBikeDTO {
	
	@Column
	@Builder.Default
	private Boolean returned = false;
	
	@CPF
	@NotEmpty
	private String clientCpf;

}
