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

import br.com.bikeapi.model.entity.Client;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class ClientRepositoryTest {

	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	ClientRepository repository;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um cliente na base com o CPF informado")
	void returnTrueWhenCpfExists() throws Exception {
		
		// cenario
		String cpf = "389.831.240-24";
		Client client = createNewClient(cpf);
		entityManager.persist(client);
		
		// execucao
		boolean exists = repository.existsByCpf(cpf);
		
		// verificacao
		assertThat(exists).isTrue();
		
	}

	public static Client createNewClient(String cpf) {
		return Client.builder().cpf(cpf).nome("Fulana").build();
	}
	
	@Test
	@DisplayName("Deve retornar falso quando nao existir cliente com o cpf informado")
	void returnFalseWhenCpfDoesntExists() {
		
		// cenario
		String cpf = "389.831.240-24";

		// execucao
		boolean exists = repository.existsByCpf(cpf);

		// verificacao
		assertThat(exists).isFalse();
	}
	
	@Test
	@DisplayName("Deve obter um cliente por id")
	void findByIdTest() throws Exception {
		
		// cenario
		Client client = createNewClient("389.831.240-24");
		entityManager.persist(client);
		
		// execucao
		Optional<Client> foundClient = repository.findById(client.getId());
		
		// verificacao
		assertThat(foundClient.isPresent()).isTrue();
		
	}
	
	@Test
	@DisplayName("Deve salvar um cliente")
	void saveClientTest() throws Exception {
		
		// cenario
		Client client = createNewClient("389.831.240-24");
		
		// execucao
		Client savedClient = repository.save(client);
		
		// verificacao
		assertThat(savedClient.getId()).isNotNull();
		
	}
	
	@Test
	@DisplayName("Deve remover um cliente")
	void deleteClientTest() {
		
		// cenario
		Client client = createNewClient("389.831.240-24");
		entityManager.persist(client);
		Client foundClient = entityManager.find(Client.class, client.getId());
		
		// execucao
		repository.delete(foundClient);
		
		Client deletedClient = entityManager.find(Client.class, client.getId());
		
		// verificacao
		assertThat(deletedClient).isNull();
		
	}
	


}
