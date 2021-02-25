package br.com.bikeapi.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.repository.BikeRepository;
import br.com.bikeapi.service.BikeService;

@Service
public class BikeServiceImpl implements BikeService{

	private BikeRepository repository; 
	
	public BikeServiceImpl(BikeRepository repository) {
		this.repository = repository;
	}

	@Override
	public Bike save(Bike bike) {
		if (repository.existsByChassi(bike.getChassi())) {
			throw new BusinessException("Chassi ja cadastrado.");
		}
		return repository.save(bike);
	}

	@Override
	public Optional<Bike> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public void delete(Bike bike) {
		if (bike == null || bike.getId() == null) {
			throw new IllegalArgumentException("Bike id cannot be null");
		}
		repository.delete(bike);
	}

	@Override
	public Bike update(Bike bike) {
		if (bike == null || bike.getId() == null) {
			throw new IllegalArgumentException("Bike id cannot be null");
		}
		return repository.save(bike);
	}

	@Override
	public Page<Bike> find(Bike filter, Pageable pageRequest) {
		Example<Bike> example = Example.of(filter, 
									ExampleMatcher
										.matching()
										.withIgnoreCase()
										.withIgnoreNullValues()
										.withStringMatcher(
												ExampleMatcher
													.StringMatcher
													.CONTAINING));
		
		return repository.findAll(example, pageRequest);
	}

	@Override
	public Optional<Bike> findByChassi(String chassi) {
		return repository.findByChassi(chassi);
	}

}
