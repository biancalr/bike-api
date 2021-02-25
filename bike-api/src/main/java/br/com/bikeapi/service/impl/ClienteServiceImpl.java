package br.com.bikeapi.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.repository.ClientRepository;
import br.com.bikeapi.service.ClientService;

@Service
public class ClienteServiceImpl implements ClientService {

	private ClientRepository repository;
	
	public ClienteServiceImpl(ClientRepository repository) {
		this.repository = repository;
	}

	@Override
	public Client save(Client cliente) {
		if (repository.existsByCpf(cliente.getCpf())) {
			throw new BusinessException("Client ja cadastrado");
		}
		return repository.save(cliente);
	}

	@Override
	public Optional<Client> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public void delete(Client cliente) {
		if (cliente == null || cliente.getId() == null) {
			throw new IllegalArgumentException("Client nao encontrado");
		}
		repository.delete(cliente);
	}

	@Override
	public Client update(Client cliente) {
		if (cliente == null || cliente.getId() == null) {
			throw new IllegalArgumentException("Client nao encontrado");
		}
		return repository.save(cliente);
	}

	@Override
	public Page<Client> find(Client filter, Pageable pageable) {
		Example<Client> example;
		example = Example.of(filter, 
						ExampleMatcher
								.matching()
								.withIgnoreCase()
								.withIgnoreNullValues()
								.withStringMatcher(
									ExampleMatcher
										.StringMatcher
										.CONTAINING));
		return repository.findAll(example, pageable);
	}

	@Override
	public Optional<Client> findByCpf(String cpf) {
		return repository.findByCpf(cpf);
	}

}
