package br.com.bikeapi.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bikeapi.model.entity.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

	/**
	 * Verifica se o {@link Client} com dado cpf existe na base
	 * 
	 * @param cpf do cliente
	 * @return <code>true</code> se o {@link Client} existe na
	 * base, caso contrario, <code>false</code>
	 */
	boolean existsByCpf(String cpf);

	/**
	 * Busca o {@link Client} dado seu cpf
	 * 
	 * @param cpf do cliente
	 * @return um {@link Optional} contendo o {@link Client}
	 * ou nulo caso ele nao exista na base
	 */
	Optional<Client> findByCpf(String cpf);

}
