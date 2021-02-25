package br.com.bikeapi.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;

public interface RentRepository extends JpaRepository<Rent, Long>{

	/**
	 * Verifica se a bicicleta ja esta alugada.
	 * 
	 * @param bike a bicicleta a verificar
	 * @return <code>true</code> se a bicicleta ja esta 
	 * alugada, caso contrario, <code>false</code>.
	 */
	@Query("select case when (count(r.id) > 0) then"
			+ " true else false end from Rent r where r.bike = :bike"
			+ " and r.returnedDateTime is null")
	boolean existsByBikeAndNotReturned(@Param("bike") Bike bike);

	/**
	 * Verifica se o cliente contem um aluguel em 
	 * adamento.
	 * 
	 * @param client o cliente a verificar
	 * @return <code>true</code> se o cliente tem um aluguel em 
	 * andamento, caso contrario, <code>false</code> .
	 */
	@Query("select case when ( count(r.id) > 0 ) then true"
			+ " else false end from Rent r where r.client = :client"
			+ " and r.returnedDateTime is null")
	Boolean existsByClientAndNotReturned(@Param("client") Client client);

	/**
	 * Procura alugueis pelo chassi da bicicleta ou pelo
	 * cpf do cliente.
	 * 
	 * @param chassi da bicicleta
	 * @param cpf do cliente
	 * @param pageRequest paginacao
	 * @return a lista de alugueis correspondentes
	 */
	@Query("select r from Rent as r "
			+ "join r.bike as b "
			+ "join r.client as c "
			+ "where b.chassi = :chassi or c.cpf = :cpf")
	Page<Rent> findByBikeOrClient(
						@Param("chassi") String chassi, 
						@Param("cpf") String cpf, 
						Pageable pageRequest);

	/**
	 * Busca os alugueis nao devolvidos.
	 * 
	 * @return uma lista contendo todos os alugueis nao devolvidos
	 */
	@Query("select r from Rent r"
			+ " where CURRENT_TIMESTAMP > r.expectedReturnDate"
			+ " and r.returnedDateTime is null")
	List<Rent> findByRentDateTimeLessThanAndNotReturned();

	/**
	 * Busca a lista de alugueis feitas por determinado
	 * cliente.
	 * 
	 * @param client o locador que se deseja procurar
	 * @param pageable paginacao
	 * @return A lista de alugueis do cliente
	 */
	Page<Rent> findByClient(Client client, Pageable pageable);

}
