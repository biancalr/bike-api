package br.com.bikeapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.bikeapi.api.dto.RentFilterDTO;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;

/**
 * Interface que comtempla os servicos oferecidos pelo
 * servico de aluguel.
 * 
 * @author bianca.l.ramos
 *
 */
public interface RentService {

	/**
	 * Salva um aluguel
	 * 
	 * @param rent o aluguel a se salvar
	 * @return o aluguel salvo
	 */
	Rent save(Rent rent);

	/**
	 * Recupera um aluguel
	 * @param id identificador do aluguel a recuperar
	 * @return o {@link Optional} resultante da pesquisa
	 */
	Optional<Rent> getById(Long id);

	/**
	 * Atualiza um aluguel
	 * @param rent o aluauel a se recuperar
	 * @return o aluguel atualizado
	 */
	Rent update(Rent rent);

	/**
	 * Filtra os alugueis
	 * @param filterDTO os campos de {@link RentFilterDTO}
	 * que se deseja filtrar
	 * @param pageable a paginacao da filtragem
	 * @return o resultado paginado da busca
	 */
	Page<Rent> find(RentFilterDTO filterDTO, Pageable pageable);

	/**
	 * Busca todos os alugueis atrasados
	 * 
	 * @return Todos os alugueis atrasados
	 */
	List<Rent> getAllLateRents();
	
	/**
	 * Recupera o historico de alugueis de determinado cliente
	 * 
	 * @param client o cliente que se quer verificar
	 * @param pageable a paginacao
	 * @return todos os alugueis de determinado {@link Client}
	 */
	Page<Rent> getRentsByClient(Client client, Pageable pageable);

}
