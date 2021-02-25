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
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.repository.ClientRepository;
import br.com.bikeapi.service.impl.ClienteServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ClientServiceTest {

	private ClientService service;
	
	@MockBean
	private ClientRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new ClienteServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um Client")
	public void saveClienteTest() {
		
		// cenario
		Client cliente = createValidClient();
		Mockito.when(repository.save(cliente))
				.thenReturn(Client.builder()
									.id(1l)
									.nome("Cicrano")
									.cpf("953.788.660-30")
									.build());	
		
		// execucao
		Client savedCliente = service.save(cliente);
		
		// verificacao
		assertThat(savedCliente.getId()).isNotNull();
		assertThat(savedCliente.getCpf()).isEqualTo("953.788.660-30");
		assertThat(savedCliente.getNome()).isEqualTo("Cicrano");
		
	}

	private Client createValidClient() {
		return Client.builder()
								.cpf("953.788.660-30")
								.nome("Cicrano")
								.build();
	}
	
	@Test
	@DisplayName("Deve lancar erro de nagocio ao tentar salvar um cliente com cpf ja existente na base")
	void mustNotSaveAClientWithDuplicatedCpf() throws Exception {
		
		// cenario
		Client cliente = createValidClient();
		Mockito.when(repository.existsByCpf(Mockito.anyString()))
				.thenReturn(true);
		
		// execucao
		Throwable exception = Assertions.catchThrowable(() -> service.save(cliente));
		
		// verificacao
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Client ja cadastrado");
		
		Mockito.verify(repository, Mockito.never())
				.save(cliente);
		
	}
	
	@Test
	@DisplayName("Deve retornar um cliente por seu Id")
	void getByIdTest() throws Exception {

		// cenario
		Client cliente = createValidClient();
		long id = 1l;
		cliente.setId(id);
		
		Mockito.when(repository.findById(id))
				.thenReturn(Optional.of(cliente));
		
		// execucao
		Optional<Client> foundClient = service.getById(id);
		
		// verificacao
		assertThat(foundClient.isPresent()).isTrue();
		assertThat(foundClient.get().getId()).isEqualTo(id);
		assertThat(foundClient.get().getNome()).isEqualTo(cliente.getNome());
		assertThat(foundClient.get().getCpf()).isEqualTo(cliente.getCpf());
		
	}
	
	@Test
	@DisplayName("Deve retornar ResourceNotFound quando o cliente procurado nao existir")
	void clientNotFoundTest() throws Exception {
		
		// cenario
		long id = 1l;
		Mockito.when(repository.findById(id))
				.thenReturn(Optional.empty());
		
		// execucao
		Optional<Client> cliente = service.getById(id);
		
		// verificacao
		assertThat(cliente.isPresent()).isFalse();
		
	}
	
	@Test
	@DisplayName("Deve remover um cliente")
	void deleteClientTest() throws Exception {
		
		// cenario
		long id = 1l;
		Client cliente = createValidClient();
		cliente.setId(id);
		
		// execucao
		org.junit.jupiter.api.Assertions
					.assertDoesNotThrow(
							() -> service.delete(cliente));
		
		// verificacao
		Mockito.verify(repository, Mockito.times(1))
				.delete(cliente);
		
	}
	
	@Test
	@DisplayName("Deve lancar erro ao tentar remover um cliente Inexistente")
	void deleteInvalidClient() throws Exception {
		
		// cenario
		Client cliente = new Client();
		
		// execucao
		org.junit.jupiter.api.Assertions
				.assertThrows(
						IllegalArgumentException.class, 
						() -> service.delete(cliente));
		
		// verificacao
		Mockito.verify(repository, Mockito.never()).delete(cliente);
		
	}
	
	@Test
	@DisplayName("Deve atualizar um cliente")
	void updateClientTest() throws Exception {
		
		// cenario
		long id = 1l;
		Client toUpdateCliente = Client.builder().id(id).build();
		
		Client updatedCliente = createValidClient();
		updatedCliente.setId(id);
		
		Mockito.when(repository.save(toUpdateCliente))
				.thenReturn(updatedCliente);
		
		// execucao
		Client cliente = service.update(toUpdateCliente);
		
		// verificacao
		assertThat(cliente.getId()).isEqualTo(updatedCliente.getId());
		assertThat(cliente.getCpf()).isEqualTo(updatedCliente.getCpf());
		assertThat(cliente.getNome()).isEqualTo(updatedCliente.getNome());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve filtrar o cliente pelo nome")
	void findClientTest() throws Exception {
		
		// cenario
		Client cliente = createValidClient();
		List<Client> list = new ArrayList<>();
		list.add(cliente);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		Page<Client> page = new PageImpl<Client>(list, pageRequest, 1);
		
		Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
				.thenReturn(page);
		
		// execucao
		Page<Client> result = service.find(cliente, pageRequest);
		
		// verificacao
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		
	}
	
	@Test
	@DisplayName("Deve obter um cliente pelo seu cpf")
	void getClienteByCpf() throws Exception {
		
		// cenario
		String cpf = createValidClient().getCpf();
		Mockito.when(repository.findByCpf(cpf))
				.thenReturn(Optional.of(Client.builder()
												.id(1l)
												.cpf(cpf)
												.build()));
		
		// execucao
		Optional<Client> cliente = service.findByCpf(cpf);
		
		// verificacao
		assertThat(cliente.isPresent()).isTrue();
		assertThat(cliente.get().getId()).isEqualTo(1l);
		assertThat(cliente.get().getCpf()).isEqualTo(cpf);
		Mockito.verify(repository, Mockito.times(1)).findByCpf(cpf);
		
	}
	
}
