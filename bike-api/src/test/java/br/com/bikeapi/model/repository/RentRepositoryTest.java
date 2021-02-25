package br.com.bikeapi.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class RentRepositoryTest {

	@Autowired
	RentRepository repository;
	
	@Autowired
	TestEntityManager entityManager;
	
	@Test
	@DisplayName("Deve verificar se existe um aluguel nao devolvido para a bicicleta")
	void existsByBikeAndNotReturnedTest() {
		
		// cenario
		Rent rent = createAndPersistRent(LocalDateTime.now());
		Bike bike = rent.getBike();
		
		// execucao
		Boolean exists = repository.existsByBikeAndNotReturned(bike);
		
		// verificacao
		assertThat(exists).isTrue();
		
	}
	
	@Test
	@DisplayName("Deve verificar se o cliente contem algum aluguel em andamento")
	void existsByClientAndNotReturnedTest() {
		
		// cenario
		Rent rent = createAndPersistRent(LocalDateTime.now());
		Client client = rent.getClient();
		
		// execucao
		Boolean exists = repository.existsByClientAndNotReturned(client);
		
		// verificacao
		assertThat(exists).isTrue();
		
	}
	
	public Rent createAndPersistRent(LocalDateTime rentDate) {
		Bike bike = BikeRepositoryTest.createNewBike("123abc");
		entityManager.persist(bike);
		
		Client client = ClientRepositoryTest.createNewClient("389.831.240-24");
		entityManager.persist(client);
		
		Rent rent = Rent.builder()
						.bike(bike)
						.client(client)
						.rentDate(rentDate)
						.customerEmail("customer@mail.com")
						.rentHoursDuration(4*24)
						.build();

		rent.setExpectedReturnDate(rentDate.plusHours(rent.getRentHoursDuration()));
		
		entityManager.persist(rent);
		return rent;
	}
	
	@Test
	@DisplayName("Deve obter alugueis cuja data/hora de aluguel for maior ou igual a contradada e nao retornados")
	public void findByDateLessThanAndNotReturned() {
		
		// cenario
		Rent rent = createAndPersistRent(LocalDateTime.now().minusHours(24 * 5));
		
		// execucao
		List<Rent> result = repository.findByRentDateTimeLessThanAndNotReturned();
		
		// verificacao
		assertThat(result).hasSize(1).contains(rent);
		assertThat(LocalDateTime.now()).isAfter(rent.getExpectedReturnDate());
		assertThat(LocalDateTime.now()).isAfter(result.get(0).getExpectedReturnDate());
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio quando nao houver alugueis atrasados")
	public void notFoundByDateTimeLessThanNotReturned() {
		
		// cenario
		createAndPersistRent(LocalDateTime.now());
		
		// execucao
		List<Rent> result = repository.findByRentDateTimeLessThanAndNotReturned();
		
		// verificacao
		assertThat(result).isEmpty();
		
	}
	
	@Test
	@DisplayName("Deve buscar o aluguel pelo chassi da bicicleta ou pelo cpf do cliente")
	void findByBikeOrClientTest() {
		
		// cenario
		Rent rent = createAndPersistRent(LocalDateTime.now());
		
		// execucao
		Page<Rent> result = repository.findByBikeOrClient("123abc", "389.831.240-24", PageRequest.of(0, 10));
		
		// verificacao
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(rent);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
		
	}
	
}
