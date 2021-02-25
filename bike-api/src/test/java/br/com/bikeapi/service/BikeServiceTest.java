/**
 * 
 */
package br.com.bikeapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.repository.BikeRepository;
import br.com.bikeapi.service.impl.BikeServiceImpl;

/**
 * @author bianca.l.ramos
 *
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BikeServiceTest {

	private BikeService service;
	
	@MockBean
	private BikeRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BikeServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar uma bicicleta")
	public void saveBookTest() {
		
		// cenario
		Bike bike = createValidBike();
		Mockito.when(repository.existsByChassi(Mockito.anyString()))
		   .thenReturn(false);
		Mockito.when(repository.save(bike))
				.thenReturn(Bike.builder()
							.id(1l)
							.chassi("123456")
							.color("Preta")
							.model("Caloi Volcano")
							.companyProperty(true)
							.build());
		
		// execucao
		Bike savedBike = service.save(bike);
		
		// verificacao
		assertThat(savedBike.getId()).isNotNull();
		assertThat(savedBike.getChassi()).isEqualTo("123456");
		assertThat(savedBike.getColor()).isEqualTo("Preta");
		assertThat(savedBike.getModel()).isEqualTo("Caloi Volcano");
		assertThat(savedBike.getCompanyProperty()).isEqualTo(true);
		
	}

	private Bike createValidBike() {
		return Bike.builder().chassi("123456").model("Caloi Volcano").color("preta").companyProperty(true).build();
	}
	
	@Test
	@DisplayName("Deve lancar erro de negocio ao tentar salvar uma bicicleta com chassi duplicado")
	public void mustNotSaveABikeWithDuplicatedChassi() {
		
		// cenario
		Bike bike = createValidBike();
		Mockito.when( repository.existsByChassi(
								Mockito.anyString()) )
				.thenReturn(true);
		
		// execucao
		Throwable exception = 
				Assertions.catchThrowable(
							() -> service.save(bike));
		
		// verificacao
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Chassi ja cadastrado.");
		
		Mockito.verify(repository, Mockito.never())
			   .save(bike);
		
	}
	
	@Test
	@DisplayName("Deve retornar uma bicicleta pelo seu ID")
	public void getByIdTest() {
		
		// cenario
		Bike bike = createValidBike();
		long id = 1l;
		bike.setId(id);
		
		Mockito.when(repository.findById(id))
				.thenReturn(Optional.of(bike));
		
		// execucao
		Optional<Bike> foundBike = service.getById(id);
		
		// verificacao
		assertThat(foundBike.isPresent()).isTrue();
		assertThat(foundBike.get().getId()).isEqualTo(id);
		assertThat(foundBike.get().getChassi()).isEqualTo(bike.getChassi());
		assertThat(foundBike.get().getColor()).isEqualTo(bike.getColor());
		assertThat(foundBike.get().getModel()).isEqualTo(bike.getModel());
		assertThat(foundBike.get().getCompanyProperty()).isEqualTo(bike.getCompanyProperty());
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio ao obter uma bicicleta por id quando ela nao existir na base")
	public void bikeNotFoundTest() {
		
		// cenario
		long id = 1l;
		Mockito.when(repository.findById(id))
				.thenReturn(Optional.empty());
		
		// execucao
		Optional<Bike> bike = service.getById(id);
		
		// teste
		assertThat(bike.isPresent()).isFalse();
		
	}
	
	@Test
	@DisplayName("Deve apagar uma bicicleta")
	public void deleteBikeTest() {
		
		// cenario
		Bike bike = createValidBike();
		long id = 1l;
		bike.setId(id);
		
		// execucao
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(
				() -> service.delete(bike));
		
		// verificacao
		Mockito.verify(repository, Mockito.times(1)).delete(bike);
		
	}
	
	@Test
	@DisplayName("Deve lancar erro ao tentar remover uma bibicleta inexistente no banco de dados")
	public void deleteInvalidBikeTest() {
		
		// cenario
		Bike bike = new Bike();
		
		// execucao
		org.junit.jupiter.api.Assertions.assertThrows(
							IllegalArgumentException.class, 
							() -> service.delete(bike));
		
		// verificacao
		Mockito.verify(repository, Mockito.never()).delete(bike);
		
	}
	
	@Test
	@DisplayName("Deve atualizar uma bicicleta")
	public void updateBikeTest() {
		
		// cenario
		long id = 1l;
		Bike toUpdateBike = Bike.builder().id(id).build();
		
		Bike updatedBike = createValidBike();
		updatedBike.setId(id);
		
		Mockito.when(repository.save(toUpdateBike))
		       .thenReturn(updatedBike);
		
		// execucao
		Bike bike = service.update(toUpdateBike);
		
		// verificacao
		assertThat(bike.getId()).isEqualTo(updatedBike.getId());
		assertThat(bike.getChassi()).isEqualTo(updatedBike.getChassi());
		assertThat(bike.getModel()).isEqualTo(updatedBike.getModel());
		assertThat(bike.getColor()).isEqualTo(updatedBike.getColor());
		assertThat(bike.getCompanyProperty()).isEqualTo(updatedBike.getCompanyProperty());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve filtrar bicicletas pelas propriedades")
	public void findBikeTest() {
		
		// cenario
		Bike bike = createValidBike();
		List<Bike> list = new ArrayList<>();
		list.add(bike);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		Page<Bike> page = new PageImpl<Bike>(list, pageRequest, 1);
		
		Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
				.thenReturn(page);
		
		// execucao
		Page<Bike> result = service.find(bike, pageRequest);
		
		// verificacao
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		
	}
	
	@Test
	@DisplayName("Deve obter uma bicicleta pelo numero do chassi")
	public void getBikeByChassi() {
		
		// cenario
		String chassi = "123abc";
		Mockito.when(repository.findByChassi(chassi))
				.thenReturn(Optional.of(Bike.builder()
											.id(1l)
											.chassi(chassi)
											.build()));	
		
		// execucao
		Optional<Bike> bike = service.findByChassi(chassi);
		
		// verificacao
		assertThat(bike.isPresent()).isTrue();
		assertThat(bike.get().getId()).isEqualTo(1l);
		assertThat(bike.get().getChassi()).isEqualTo(chassi);
		Mockito.verify(repository, Mockito.times(1)).findByChassi(chassi);
	} 
	
}
