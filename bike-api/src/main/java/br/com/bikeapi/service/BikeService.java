package br.com.bikeapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.bikeapi.model.entity.Bike;

public interface BikeService {

	/**
	 * Salva uma bicicleta
	 * 
	 * @param bike a bicicleta a ser salva
	 * @return
	 */
	Bike save(Bike bike);

	/**
	 * Busca uma bicicleta pelo seu id
	 * 
	 * @param id da bicicleta a ser recuperada
	 * @return o {@link Optional} resultante da pesquisa 
	 */
	Optional<Bike> getById(Long id);

	/**
	 * Remove uma {@link Bike}
	 * 
	 * @param bike que se deseja remover
	 */
	void delete(Bike bike);

	/**
	 * Atualiza uma {@link Bike}
	 * 
	 * @param bike a bicicleta que se deseja atualizar
	 * @return a bicicleta atualizada
	 */
	Bike update(Bike bike);

	/**
	 * Filtra {@link Bike}
	 * 
	 * @param filter o filtro aplicado na pesquisa
	 * @param pageRequest a paginacao
	 * @return resultado paginado da filtragem
	 */
	Page<Bike> find(Bike filter, Pageable pageRequest);

	/**
	 * Busca uma {@link Bike} pelo chassi
	 * 
	 * @param chassi da bicicleta
	 * @return o {@link Optional} resultante da pesquisa
	 */
	Optional<Bike> findByChassi(String chassi);

}
