package br.com.bikeapi.api.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BikeDTO {

	private Long id;
	
	@NotEmpty
	@Size(min = 6)
	private String chassi;
	
	@NotEmpty
	private String model;
	
	@NotEmpty
	private String color;
	
	@NotNull
	private Boolean companyProperty;
	
}
