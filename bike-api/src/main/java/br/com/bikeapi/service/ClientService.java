package br.com.bikeapi.service;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.bikeapi.model.entity.Client;

public interface ClientService {

	/**
	 * Salva um {@link Client}
	 * 
	 * @param cliente a ser salvo
	 * @return o cliente salvo 
	 */
	Client save(@Valid Client cliente);

	/**
	 * Busca o {@link Client} pelo seu ID
	 * 
	 * @param id do cliente
	 * @return um {@link Optional} resultante da busca
	 */
	Optional<Client> getById(@NotNull Long id);

	/**
	 * Remove um {@link Client}
	 * 
	 * @param cliente {@link Client} que se deseja remover
	 */
	void delete(@Valid Client cliente);

	/**
	 * Atualiza um {@link Client}
	 * 
	 * @param cliente a ser atualizado
	 * @return o {@link Client} atualizado
	 */
	Client update(@Valid Client cliente);

	/**
	 * Filtra {@link Client}
	 * 
	 * @param filter o filtro aplicado para procurar clientes
	 * @param pageable a paginacao
	 * @return resultado paginado da filtragem
	 */
	Page<Client> find(@Valid Client filter, Pageable pageable);

	/**
	 * Busca o {@link Client} pelo cpf fornecido
	 * 
	 * @param cpf do cliente a ser pesquisado
	 * @return o {@link Optional} resultante da busca
	 */
	Optional<Client> findByCpf(@NotBlank String cpf);

}
