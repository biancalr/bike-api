package br.com.bikeapi.api.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.bikeapi.api.dto.BikeDTO;
import br.com.bikeapi.api.dto.ClientDTO;
import br.com.bikeapi.api.dto.RentDTO;
import br.com.bikeapi.api.dto.RentFilterDTO;
import br.com.bikeapi.api.dto.ReturnedBikeDTO;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.service.BikeService;
import br.com.bikeapi.service.ClientService;
import br.com.bikeapi.service.RentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/rent")
@RequiredArgsConstructor
@Api("Rent API")
@Slf4j
public class RentController {

	private final RentService service;
	private final BikeService bikeService;
	private final ClientService clientService;
	private final ModelMapper mapper;

	/**
	 * Cria o aluguel
	 * 
	 * @param dto os dados do aluguel
	 * @return o aluguel criado
	 * @throws MethodArgumentNotValidException
	 * @throws ResponseStatusException
	 */
	@ApiOperation("Criar aluguel")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody RentDTO dto) {

		log.info("Criando aluguel para o cliente: {}, utilizando a bicicleta: {}", dto.getCpf(), dto.getChassi());
		
		Bike bike = bikeService.findByChassi(dto.getChassi()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bike not found with given chassi"));

		Client client = clientService.findByCpf(dto.getCpf()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client not found with given cpf"));

		Rent rent = Rent.builder().bike(bike).client(client).rentDate(LocalDateTime.now())
				.rentHoursDuration(dto.getRentHoursDuration()).customerEmail(dto.getCustomerEmail())
				.expectedReturnDate(LocalDateTime.now().plusHours(dto.getRentHoursDuration())).build();

		rent = service.save(rent);
		return rent.getId();
	}

	/**
	 * Atualiza o aluguel para marcar a data e a hora que a bicicleta foi devolvida
	 * 
	 * @param id  identificador do aluguel
	 * @param dto contem o sinal indicando que a bicicleta foi devolvida
	 * @throws ResponseStatusException
	 * @throws MethodArgumentNotValidException
	 */
	@ApiOperation("Atualizar aluguel")
	@PatchMapping("/{id}")
	public void returnedBike(@PathVariable Long id, @RequestBody @Valid ReturnedBikeDTO dto) {

		log.info("Atualizando o estado aluguel de ID: {}", id);
		
		Rent rent = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		Client client = clientService.findByCpf(dto.getClientCpf())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

		/*
		 * Testar se o cliente que pretende devolver a bicicleta `e o mesmo que a alugou
		 */
		if (!rent.getClient().getCpf().equals(client.getCpf())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This client didn't rent this bike");
		}

		if (dto != null && dto.getReturned()) {
			rent.setReturnedDateTime(LocalDateTime.now());
			service.update(rent);
		}

	}

	/**
	 * Filtra os alugueis
	 * 
	 * @param dto      os dados do aluguel
	 * @param pageable a paginacao
	 * @return A lista paginada dos resultados
	 */
	@ApiOperation("Filtrar alugueis")
	@GetMapping
	public Page<RentDTO> find(RentFilterDTO dto, Pageable pageable) {

		log.info("Filtrando alugueis");
		
		Page<Rent> result = service.find(dto, pageable);
		List<RentDTO> rents = result.getContent().stream().map(entity -> {
			Bike bike = entity.getBike();
			Client client = entity.getClient();
			BikeDTO bikeDTO = mapper.map(bike, BikeDTO.class);
			ClientDTO clientDTO = mapper.map(client, ClientDTO.class);
			RentDTO rentDTO = mapper.map(entity, RentDTO.class);
			rentDTO.setBike(bikeDTO);
			rentDTO.setCliente(clientDTO);
			return rentDTO;
		}).collect(Collectors.toList());

		return new PageImpl<RentDTO>(rents, pageable, result.getTotalElements());

	}

}
