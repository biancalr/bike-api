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
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.service.BikeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/bikes")
@RequiredArgsConstructor
@Api("Bike API")
@Slf4j
public class BikeController {

	private final BikeService service;
	private final ModelMapper modelMapper;

	/**
	 * Criar uma bicicleta
	 * 
	 * @param dto uma bicicleta valida
	 * @return a bicicleta criada
	 * @throws MethodArgumentNotValidException
	 */
	@ApiOperation("Criar uma bicicleta")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BikeDTO create(@RequestBody @Valid BikeDTO dto) {
		log.info("Criando uma bicicleta de chassi: {}", dto.getChassi());
		Bike entity = modelMapper.map(dto, Bike.class);
		entity = service.save(entity);
		return modelMapper.map(entity, BikeDTO.class);
	}

	/**
	 * Recuperar bicicleta por ID
	 * 
	 * @param id identificador da bicicleta
	 * @return a bicicleta encontrada
	 * @throws ResponseStatusException
	 */
	@ApiOperation("Recuperar bicicleta por ID")
	@GetMapping("/{id}")
	public BikeDTO get(@PathVariable Long id) {
		log.info("Recuperando uma bicicleta de ID: {}", id);
		return service.getById(id).map(bike -> modelMapper.map(bike, BikeDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	/**
	 * Remover uma bicicleta
	 * 
	 * @param id identificador da bicicleta
	 * @throws ResponseStatusException
	 */
	@ApiOperation("Remover uma bicicleta")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		log.info("Removendo uma bicicleta de ID: {}", id);
		Bike foundBike = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(foundBike);
	}

	/**
	 * Atualizar uma bicicleta
	 * 
	 * @param id      identificador da bicicleta
	 * @param bikeDTO os campos a modificar
	 * @return a bicicleta atualizada
	 * @throws ResponseStatusException
	 * @throws MethodArgumentNotValidException
	 */
	@ApiOperation("Atualizar uma bicicleta")
	@PutMapping("/{id}")
	public BikeDTO update(@PathVariable Long id, @RequestBody @Valid BikeDTO bikeDTO) {
		log.info("Atualizando a bicicleta de ID: {}", id);
		return service.getById(id).map(bike -> {
			bike.setModel(bikeDTO.getModel());
			bike.setColor(bikeDTO.getColor());
			bike = service.update(bike);
			return modelMapper.map(bike, BikeDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	/**
	 * Filtrar bicicletas
	 * 
	 * @param dto         os dados pelos quais se deseja filtrar
	 * @param pageRequest a paginacao
	 * @return a lista paginada dos resultados
	 */
	@ApiOperation("Filtrar bicicletas")
	@GetMapping
	public Page<BikeDTO> find(BikeDTO dto, Pageable pageRequest) {
		log.info("Filtrando bicicletas");
		Bike filter = modelMapper.map(dto, Bike.class);
		Page<Bike> result = service.find(filter, pageRequest);
		List<BikeDTO> list = result.getContent().stream().map(entity -> modelMapper.map(entity, BikeDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<BikeDTO>(list, pageRequest, result.getTotalElements());
	}

	/**
	 * 
	 * @param chassi
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/info/{chassi}")
	public BikeDTO findByChassi(@PathVariable String chassi) {
		return service.findByChassi(chassi).map(bike -> modelMapper.map(bike, BikeDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

}
