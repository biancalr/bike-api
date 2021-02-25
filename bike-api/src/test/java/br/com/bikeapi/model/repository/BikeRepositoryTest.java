package br.com.bikeapi.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.bikeapi.model.entity.Bike;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BikeRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private BikeRepository repository;
	
	@Test
	@DisplayName("Deve retornar verdadeiro caso o Chassi exista na base")
	public void returnTrueWhenChassiExists() {
		
		// cenario
		String chassi = "123456";
		Bike bike = createNewBike(chassi);
		entityManager.persist(bike);
		
		// execucao
		boolean exists = repository.existsByChassi(chassi);
		
		// verificacao
		assertThat(exists).isTrue();
		
	}
	
	@Test
	@DisplayName("Deve retornar falso caso o Chassi nao exista na base")
	public void returnFalseWhenChassiDoesNotExists() {
		
		// cenario
		String chassi = "123456";
		
		// execucao
		boolean exists = repository.existsByChassi(chassi);
		
		// verificacao
		assertThat(exists).isFalse();
		
	}

	public static Bike createNewBike(String chassi) {
		return Bike.builder()
						.chassi(chassi)
						.color("preta")
						.model("Caloi Volcano")
						.companyProperty(true)
						.build();
	}
	
	@Test
	@DisplayName("Deve salvar uma bicicleta")
	public void saveBookTest() {
		
		// cenario
		Bike bike = createNewBike("123456");
		
		// execucao
		Bike savedBike = repository.save(bike);
		
		// verificacao
		assertThat(savedBike.getId()).isNotNull();
		
	}
	
	@Test
	@DisplayName("Deve obter uma bicicleta pelo id")
	public void findByIdTest() {
		
		// cenario
		Bike bike = createNewBike("654321");
		entityManager.persist(bike);
		
		// execucao
		Optional<Bike> foundBike = repository.findById(bike.getId());
		
		// verificacao
		assertThat(foundBike.isPresent()).isTrue();
		
	}
	
	@Test
	@DisplayName("Deve remover uma bicicleta")
	public void deleteBikeTest() {
		
		// cenario
		Bike bike = createNewBike("654321");
		entityManager.persist(bike);
		Bike foundBike = entityManager.find(Bike.class, bike.getId());
		
		// execucao
		repository.delete(foundBike);
		
		Bike deletedBike = entityManager.find(Bike.class, bike.getId());
		
		// verificacao
		assertThat(deletedBike).isNull();
		
	}
		
}
