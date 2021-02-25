package br.com.bikeapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.bikeapi.api.dto.RentFilterDTO;
import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.model.repository.RentRepository;
import br.com.bikeapi.service.impl.RentServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RentServiceTest {
	
	private RentService service;
	
	@MockBean
	private RentRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new RentServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um aluguel")
	void saveRentTest() throws Exception {
		
		// cenario
		long id = 1l;
		Bike bike = Bike.builder().id(id).build();
		Client client = Client.builder().id(id).build(); 
		final LocalDateTime now = LocalDateTime.now();
		Rent savingRent = Rent.builder()
							  .bike(bike)
							  .client(client)
							  .rentDate(now)
							  .rentHoursDuration(2*24)
							  .build();
		
		Rent savedRent = Rent.builder()
							 .id(id)
							 .rentDate(now)
							 .bike(bike)
							 .client(client)
							 .rentHoursDuration(2*24)
							 .expectedReturnDate(LocalDateTime.now().plusHours(2*24))
							 .build();
		
		Mockito.when(repository.existsByBikeAndNotReturned(bike))
				.thenReturn(false);
		Mockito.when(repository.existsByClientAndNotReturned(client))
				.thenReturn(false);
		Mockito.when(repository.save(savingRent))
				.thenReturn(savedRent);
		
		// execucao
		Rent rent = service.save(savingRent);
		
		// verificacao
		assertThat(rent.getId()).isEqualTo(savedRent.getId());
		assertThat(rent.getRentDate()).isEqualTo(savedRent.getRentDate());
		assertThat(rent.getRentHoursDuration()).isEqualTo(savedRent.getRentHoursDuration());
		assertThat(rent.getClient()).isEqualTo(savedRent.getClient());
		assertThat(rent.getBike()).isEqualTo(savedRent.getBike());
		assertThat(rent.getExpectedReturnDate()).isEqualTo(savedRent.getExpectedReturnDate());
		
	}
	
	@Test
	@DisplayName("Deve lancar erro de negocio ao salvar um aluguel com uma bicicleta ja alugada")
	void rentedBikeSaveTest() throws Exception {
		
		// cenario
		Rent savingRent = createRental();
		Mockito.when(repository.existsByBikeAndNotReturned(createRental().getBike()))
				.thenReturn(true);
		
		// execucao
		Throwable exception = 
				catchThrowable(() -> service.save(savingRent));
		
		// verificacao
		assertThat(exception).isInstanceOf(BusinessException.class)
							 .hasMessage("Bike already rented");
		Mockito.verify(repository, never()).save(savingRent);
		
	}
	
	@Test
	@DisplayName("Deve lancar erro de negocio ao salvar um aluguel com um cliente que possua um aluguel anterior em andamento")
	void clientRentalInProgressTest() throws Exception {
		
		// cenario
		Rent savingRent = createRental();
		Mockito.when(repository.existsByClientAndNotReturned(createRental().getClient()))
				.thenReturn(true);
		
		// execucao 
		Throwable exception = 
				catchThrowable(() -> service.save(savingRent));
		
		// teste
		assertThat(exception).isInstanceOf(BusinessException.class)
							 .hasMessage("Client with rental in progress");
		Mockito.verify(repository, never()).save(savingRent);
		
	}

	public static Rent createRental() {
		long id = 1l;
		Bike bike = Bike.builder().id(id).build();
		Client client = Client.builder().id(id).build(); 
		final LocalDateTime now = LocalDateTime.now();
		Rent rent = Rent.builder()
							  .bike(bike)
							  .client(client)
							  .rentDate(now)
							  .rentHoursDuration(2*24)
							  .build();
		return rent;
	}
	
	@Test
	@DisplayName("Deve obter as informacoes de um aluguel")
	void getRentDetailsTest() throws Exception {
		
		// cenario
		Rent rent = createRental();
		long id = 1l;
		rent.setId(id);
		
		Mockito.when(repository.findById(id))
				.thenReturn(Optional.of(rent));
		
		// execucao
		Optional<Rent> result = service.getById(id);
		
		// verificacao
		assertThat(result.isPresent()).isTrue();
		assertThat(result.get().getId()).isEqualTo(id);
		assertThat(result.get().getClient()).isEqualTo(rent.getClient());
		assertThat(result.get().getBike()).isEqualTo(rent.getBike());
		assertThat(result.get().getRentDate()).isEqualTo(rent.getRentDate());
		assertThat(result.get().getRentHoursDuration()).isEqualTo(rent.getRentHoursDuration());
		
		Mockito.verify(repository).findById(id);
	}
	
	@Test
	@DisplayName("Deve atualizar um aluguel")
	void updateRentTest() throws Exception {
		
		// cenario
		Rent rent = createRental();
		rent.setId(1l);
		rent.setReturnedDateTime(LocalDateTime.now());
		
		Mockito.when(repository.save(rent))
			   .thenReturn(rent);
		
		// execucao
		Rent updatedRent = service.update(rent);
		
		// verificacao
		assertThat(updatedRent.getReturnedDateTime()).isNotNull();
		Mockito.verify(repository).save(rent);
		
	}
	
	@Test
	@DisplayName("Deve filtrar as bicicletas pelas propriedades")
	void findRentTest() throws Exception {
		
		// cenario
		RentFilterDTO rentFilterDTO = RentFilterDTO
											.builder()
											.chassi("123abc")
											.cpf("609.397.640-83")
											.build();
		
		Rent rent = createRental();
		long id = 1l;
		rent.setId(id);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		List<Rent> list = Arrays.asList(rent);
		
		Page<Rent> page = new PageImpl<Rent>(list, pageRequest, list.size());
		
		Mockito.when(repository.findByBikeOrClient(Mockito.anyString(), Mockito.anyString(),Mockito.any(PageRequest.class)))
				.thenReturn(page);
		
		// execucao
		Page<Rent> result = service.find(rentFilterDTO, pageRequest);
		
		// verificacao
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		
	}
	
	@Test
	@DisplayName("Deve retornar todos os alugueis atrasados")
	void getAllLateRents() throws Exception{
		
		// cenario
		Rent rent = createRental();
		
		Mockito.when(repository.findByRentDateTimeLessThanAndNotReturned())
				.thenReturn(Arrays.asList(rent));
		
		// execucao
		List<Rent> result = service.getAllLateRents();
		
		// verificacao
		assertThat(result.size()).isEqualTo(1);
		assertThat(result).contains(rent);
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio caso nao haja alugueis em atraso")
	void allRentsInTimeTest() {
		
		// cenario
		createRental();
		Mockito.when(repository.findByRentDateTimeLessThanAndNotReturned())
				.thenReturn(new ArrayList<Rent>());
		
		// execucao
		List<Rent> result = service.getAllLateRents();
		
		// verificacao
		assertThat(result).isEmpty();
		
	}
	
	
}
