package br.com.bikeapi.api.controllers;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.bikeapi.api.dto.BikeDTO;
import br.com.bikeapi.api.dto.ClientDTO;
import br.com.bikeapi.api.dto.RentDTO;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.service.ClientService;
import br.com.bikeapi.service.RentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Api("Client API")
@Slf4j
public class ClientController {

	private final ClientService service;
	private final RentService rentService;
	private final ModelMapper mapper;

	/**
	 * Criar um cliente.
	 * 
	 * @param dto um cliente valido
	 * @return o cliente criado
	 * @throws MethodArgumentNotValidException
	 */
	@ApiOperation("Criar um cliente")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClientDTO post(@RequestBody @Valid ClientDTO dto) {
		log.info("Criando um cliente de cpf: {}", dto.getCpf());
		Client entity = mapper.map(dto, Client.class);
		entity = service.save(entity);
		return mapper.map(entity, ClientDTO.class);
	}

	/**
	 * Recuperar cliente por ID.
	 * 
	 * @param id identificador do cliente a se recuperar
	 * @return o cliente encontrado
	 * @throws ResponseStatusException
	 */
	@ApiOperation("Recuperar cliente por ID")
	@GetMapping(value = "/{id}")
	public ClientDTO get(@PathVariable Long id) {
		log.info("Recuperando o cliente de ID: {}", id);
		return service.getById(id).map(cliente -> mapper.map(cliente, ClientDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	/**
	 * Remover um cliente.
	 * 
	 * @param id identificador do cliente
	 * @throws ResponseStatusException
	 */
	@ApiIgnore
	@ApiOperation("Remover um cliente")
	@DeleteMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		log.info("Removendo o cliente de ID: {}", id);
		Client cliente = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(cliente);
	}

	/**
	 * Atualizar um cliente.
	 * 
	 * @param id  identificador do cliente
	 * @param dto um cliente valido
	 * @return o cliente atualizado
	 * @throws ResponseStatusException
	 * @throws MethodArgumentNotValidException
	 */
	@ApiOperation("Atualizar um cliente")
	@PutMapping(value = "/{id}")
	public ClientDTO update(@PathVariable Long id, @RequestBody @Valid ClientDTO dto) {
		log.info("Atualizando o cliente de cpf: {}", dto.getCpf());
		return service.getById(id).map(cliente -> {
			cliente.setNome(dto.getNome());
			cliente.setCpf(dto.getCpf());
			cliente = service.update(cliente);
			return mapper.map(cliente, ClientDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	/**
	 * Filtrar clientes.
	 * 
	 * @param dto         os dados pelos quais se deseja filtrar
	 * @param pageRequest a paginacao
	 * @return a lista paginada dos resultados
	 */
	@ApiOperation("Filtrar clientes")
	@GetMapping
	public Page<ClientDTO> find(ClientDTO dto, Pageable pageRequest) {
		log.info("Filtrando clientes");
		Client filter = mapper.map(dto, Client.class);
		Page<Client> result = service.find(filter, pageRequest);
		List<ClientDTO> list = result.getContent().stream().map(entity -> mapper.map(entity, ClientDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<ClientDTO>(list, pageRequest, result.getTotalElements());
	}

	/**
	 * Recuperar aluguel por cliente.
	 * 
	 * @param id       identificador do cliente
	 * @param pageable a paginacao
	 * @return a lista paginada dos alugueis feitos pelo cliente
	 * @throws ResponseStatusException
	 */
	@ApiOperation("Recuperar aluguel por cliente")
	@GetMapping("/{id}/rents")
	Page<RentDTO> rentsByClient(@PathVariable Long id, Pageable pageable) {
		log.info("Buscando os alugueis feitos pelo cliente de ID: {}", id);

		Client client = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		Page<Rent> result = rentService.getRentsByClient(client, pageable);

		List<RentDTO> list = result.getContent().stream().map(rent -> {
			Client rentClient = rent.getClient();
			ClientDTO clientDTO = mapper.map(rentClient, ClientDTO.class);
			Bike rentBike = rent.getBike();
			BikeDTO bikeDTO = mapper.map(rentBike, BikeDTO.class);
			RentDTO rentDTO = mapper.map(rent, RentDTO.class);
			rentDTO.setCliente(clientDTO);
			rentDTO.setBike(bikeDTO);
			return rentDTO;
		}).collect(Collectors.toList());

		return new PageImpl<RentDTO>(list, pageable, result.getTotalElements());

	}

}
