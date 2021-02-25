package br.com.bikeapi.api.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bikeapi.api.dto.ClientDTO;
import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.service.ClientService;
import br.com.bikeapi.service.RentService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = ClientController.class)
@AutoConfigureMockMvc
public class ClientControllerTest {
	
	private static final String CLIENTE_API = "/api/clientes";
	
	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private ClientService service;
	
	@MockBean
	private RentService rentService;
	
	@Test
	@DisplayName("Deve criar um cliente com sucesso")
	void createClienteTest() throws Exception {
		
		// cenario
		long id = 1l;
		ClientDTO dto = createNewCliente();
		Client savedClient = Client.builder()
									  .id(id)
									  .cpf(createNewCliente().getCpf())
									  .nome(createNewCliente().getNome())
									  .build();
		
		BDDMockito.given(service.save(Mockito.any(Client.class)))
				.willReturn(savedClient);
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		// execucao
		MockHttpServletRequestBuilder request = 
					MockMvcRequestBuilders
							.post(CLIENTE_API)
							.content(json)
							.accept(MediaType.APPLICATION_JSON)
							.contentType(MediaType.APPLICATION_JSON);
				
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("id").isNotEmpty())
			.andExpect(jsonPath("nome").value("Fulano"))
			.andExpect(jsonPath("cpf").value("047.835.850-40"));
		
	}


	private ClientDTO createNewCliente() {
		return ClientDTO.builder()
									.nome("Fulano")
									.cpf("047.835.850-40")
									.build();
	}
	
	@Test
	@DisplayName("Deve lancar erro ao tentar cadastrar um cliente com cpf ja cadastrado")
	public void createWithDuplicatedCpf() throws Exception {
		
		// cenario
		ClientDTO dto = createNewCliente();
		String json = new ObjectMapper().writeValueAsString(dto);
		String message = "Cpf ja cadastrado";
		BDDMockito.given(service.save(Mockito.any(Client.class)))
					.willThrow(new BusinessException(message));
		
		// execucao
		MockHttpServletRequestBuilder request =
					MockMvcRequestBuilders
					.post(CLIENTE_API)
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", Matchers.hasSize(1)))
			.andExpect(jsonPath("errors[0]").value(message));
		
	}
	
	@Test
	@DisplayName("Deve lancar erro de validacao quando nao houver dados suficientes para a criacao do cliente")
	void createInvalidBikeTest() throws Exception {

		// cenario
		String json = new ObjectMapper().writeValueAsString(new Client());
		
		// execucao
		MockHttpServletRequestBuilder request =
				MockMvcRequestBuilders
				.post(CLIENTE_API)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", Matchers.hasSize(2)));
		
	}
	
	@Test
	@DisplayName("Deve obter as informacoes de um Client")
	void getClientDetailsTest() throws Exception {
		
		// cenario
		long id = 1l;
		Client cliente = Client.builder()
								.id(id)
								.nome(createNewCliente().getNome())
								.cpf(createNewCliente().getCpf())
								.build();
		BDDMockito.given(service.getById(id))
				.willReturn(Optional.of(cliente));
		
		// execucao
		MockHttpServletRequestBuilder request = 
					MockMvcRequestBuilders
					.get(CLIENTE_API.concat("/" + id))
					.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(1l))
			.andExpect(jsonPath("nome").value("Fulano"))
			.andExpect(jsonPath("cpf").value("047.835.850-40"));
		
	}

	@Test
	@DisplayName("Deve retornar ResourceNotFound quando um cliente solicitado nao for cadastrado")
	void clientNotFoundTest() throws Exception {
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
				.willReturn(Optional.empty());
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
				.get(CLIENTE_API.concat("/" + 1l))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve remover um cliente")
	void deleteClienteTest() throws Exception {
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
				.willReturn(Optional.of(Client.builder()
												.id(1l)
												.build()));	
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
					.delete(CLIENTE_API.concat("/" + 1));
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNoContent());
		
	}
	
	@Test
	@DisplayName("Deve lancar erro ao tentar remover um cliente inexistente")
	void deleteInvalidClientTest() throws Exception {
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
				.willReturn(Optional.empty());
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
					.delete(CLIENTE_API.concat("/" + 1));
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve atualizar um cliente")
	void updateClientTest() throws Exception {
		
		// cenario
		String json = new ObjectMapper().writeValueAsString(createNewCliente());
		long id = 1l;
		Client toUpdateClient = Client.builder()
										.id(id)
										.nome("Cicrano")
										.cpf("953.788.660-30")
										.build();
		BDDMockito.given(service.getById(id))
					.willReturn(Optional.of(toUpdateClient));
		
		Client updatedClient = Client.builder()
										.id(id)
										.nome("Fulano")
										.cpf("047.835.850-40")
										.build();
		BDDMockito.given(service.update(toUpdateClient))
				.willReturn(updatedClient);
		
		// execucao
		MockHttpServletRequestBuilder request = 
					MockMvcRequestBuilders
						.put(CLIENTE_API.concat("/" + 1))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(id))
			.andExpect(jsonPath("nome").value(createNewCliente().getNome()))
			.andExpect(jsonPath("cpf").value(createNewCliente().getCpf()));
		
	}
	
	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar um cliente inexistente na base")
	void updateInexistentClient() throws Exception {
		
		// cenario
		String json = new ObjectMapper().writeValueAsString(createNewCliente());
		BDDMockito.given(service.getById(Mockito.anyLong()))
					.willReturn(Optional.empty());
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
					.put(CLIENTE_API.concat("/" + 1))
					.accept(MediaType.APPLICATION_JSON)
					.contentType(MediaType.APPLICATION_JSON)
					.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve filtrar clientes")
	void findClientsTest() throws Exception {
		
		// cenario
		long id = 1l;
		Client cliente = Client.builder()
								.id(id)
								.nome(createNewCliente().getNome())
								.cpf(createNewCliente().getCpf())
								.build();
		BDDMockito.given(service.find(Mockito.any(Client.class), Mockito.any(Pageable.class)))
					.willReturn(new PageImpl<Client>(Arrays.asList(cliente), PageRequest.of(0, 20), 1));
		
		String queryString = String.format("?nome=%s", cliente.getNome());
		
		// execucao
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
				.get(CLIENTE_API.concat("/" + queryString))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("content", Matchers.hasSize(1)))
			.andExpect(jsonPath("totalElements").value(1))
			.andExpect(jsonPath("pageable.pageSize").value(20))
			.andExpect(jsonPath("pageable.pageNumber").value(0));
		
	}
	
	@Test
	@DisplayName("Deve buscar os alugueis de um determinado cliente pelo seu id")
	void rentsByClientTest() throws Exception {
		
		// cenario
		long id = 1l;
		Client client = Client.builder()
							  .id(id)
							  .build();
		Bike bike = Bike.builder().id(id).build();
		Rent rent = Rent.builder().id(1l).bike(bike).client(client).build();
		BDDMockito.given(service.getById(id))
					.willReturn(Optional.of(client));
		BDDMockito.given(rentService.getRentsByClient(Mockito.any(Client.class), Mockito.any(Pageable.class)))
					.willReturn(new PageImpl<Rent>(Arrays.asList(rent), PageRequest.of(0, 10), 1));
		
		// execucao 
		MockHttpServletRequestBuilder request = 
				MockMvcRequestBuilders
					.get(CLIENTE_API.concat("/" + id + "/rents"))
					.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("content", Matchers.hasSize(1)))
			.andExpect(jsonPath("totalElements").value(1))
			.andExpect(jsonPath("pageable.pageSize").value(20))
			.andExpect(jsonPath("pageable.pageNumber").value(0));
		
	}
	
	
}
