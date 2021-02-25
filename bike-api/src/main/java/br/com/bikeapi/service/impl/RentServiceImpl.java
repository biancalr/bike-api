package br.com.bikeapi.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.bikeapi.api.dto.RentFilterDTO;
import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.model.repository.RentRepository;
import br.com.bikeapi.service.RentService;

@Service
public class RentServiceImpl implements RentService {

	private RentRepository repository;

	public RentServiceImpl(RentRepository repository) {
		this.repository = repository;
	}

	@Override
	public Rent save(Rent rent) {
		if (repository.existsByBikeAndNotReturned(rent.getBike())) {
			throw new BusinessException("Bike already rented");
		}
		if (repository.existsByClientAndNotReturned(rent.getClient())) {
			throw new BusinessException("Client with rental in progress");
		}
		/*
		 * 1 hour tolerance
		 */
		rent.setExpectedReturnDate(rent.getRentDate().plusHours(rent.getRentHoursDuration() + 1));
		return repository.save(rent);
	}

	@Override
	public Optional<Rent> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Rent update(Rent rent) {
		return repository.save(rent);
	}

	@Override
	public Page<Rent> find(RentFilterDTO filterDTO, Pageable pageable) {
		return repository.findByBikeOrClient(filterDTO.getChassi(), filterDTO.getCpf(), pageable);
	}

	@Override
	public Page<Rent> getRentsByClient(Client client, Pageable pageable) {
		return repository.findByClient(client, pageable);
	}

	@Override
	public List<Rent> getAllLateRents() {
		return repository.findByRentDateTimeLessThanAndNotReturned();
	}

}
