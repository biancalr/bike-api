package br.com.bikeapi.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bikeapi.model.entity.Bike;

public interface BikeRepository extends JpaRepository<Bike, Long>{

	/**
	 * Verifica se a {@link Bike} existe na base.
	 * 
	 * @param chassi da {@link Bike}
	 * @return <code>true</code> se a {@link Bike} existe na
	 * base, caso contrario, <code>false</code>
	 */
	boolean existsByChassi(String chassi);
	
	/**
	 * Busca a {@link Bike} pelo chassi.
	 * 
	 * @param chassi da {@link Bike}
	 * @return um {@link Optional} contendo a {@link Bike}
	 * ou nulo caso nao exista na base
	 */
	Optional<Bike> findByChassi(String chassi);
	
}
