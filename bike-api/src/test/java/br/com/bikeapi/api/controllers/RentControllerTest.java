package br.com.bikeapi.api.controllers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import br.com.bikeapi.api.dto.RentDTO;
import br.com.bikeapi.api.dto.RentFilterDTO;
import br.com.bikeapi.api.dto.ReturnedBikeDTO;
import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.model.entity.Client;
import br.com.bikeapi.model.entity.Rent;
import br.com.bikeapi.service.BikeService;
import br.com.bikeapi.service.ClientService;
import br.com.bikeapi.service.RentService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = RentController.class)
@AutoConfigureMockMvc
public class RentControllerTest {

	private static final String RENT_API = "/api/rent";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private RentService service;

	@MockBean
	private BikeService bikeService;

	@MockBean
	private ClientService clienteService;

	@Test
	@DisplayName("Deve realizar um aluguel com sucesso")
	void createRentTest() throws Exception {

		// cenario
		RentDTO dto = RentDTO.builder().customerEmail("costumer@gmail.com").rentHoursDuration(2).cpf("389.831.240-24")
				.chassi("123abc").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Client cliente = Client.builder().id(1l).cpf("389.831.240-24").nome("Fulana").build();
		BDDMockito.given(clienteService.findByCpf("389.831.240-24")).willReturn(Optional.of(cliente));

		Bike bike = Bike.builder().chassi("123abc").id(1l).color("branco").companyProperty(true).model("Caloi Volcano")
				.build();
		BDDMockito.given(bikeService.findByChassi("123abc")).willReturn(Optional.of(bike));

		Rent rent = Rent.builder().id(1l).client(cliente).bike(bike).rentHoursDuration(2).rentDate(LocalDateTime.now())
				.customerEmail("costumer@gmail.com").build();
		BDDMockito.given(service.save(Mockito.any(Rent.class))).willReturn(rent);

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(RENT_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));

	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer um aluguel de uma bicicleta inexistente na base")
	void invalidChassiTest() throws Exception {

		// cenario
		RentDTO dto = RentDTO.builder().chassi("123abc").cpf("389.831.240-24").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(bikeService.findByChassi("123abc")).willReturn(Optional.empty());

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(RENT_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Bike not found with given chassi"));

	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer um aluguel de uma bicicleta com cpf do cliente inexistente na base")
	void invalidCpfTest() throws Exception {

		// cenario
		RentDTO dto = RentDTO.builder().chassi("123abc").cpf("389.831.240-24").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(bikeService.findByChassi("123abc")).willReturn(Optional.of(Bike.builder().id(1l).build()));

		BDDMockito.given(clienteService.findByCpf("389.831.240-24")).willReturn(Optional.empty());

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(RENT_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Client not found with given cpf"));

	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer aluguel de uma bicicleta alugada")
	void rentedBikeErrorOnCreateRentTest() throws Exception {

		// cenario
		RentDTO dto = RentDTO.builder().cpf("389.831.240-24").chassi("123abc").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Bike bike = Bike.builder().id(1l).chassi("123abc").build();
		BDDMockito.given(bikeService.findByChassi("123abc")).willReturn(Optional.of(bike));

		Client cliente = Client.builder().id(1l).build();

		BDDMockito.given(clienteService.findByCpf("389.831.240-24")).willReturn(Optional.of(cliente));

		BDDMockito.given(service.save(Mockito.any(Rent.class))).willThrow(new BusinessException("Bike already rented"));

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(RENT_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Bike already rented"));

	}

	@Test
	@DisplayName("Deve pontuar o a devolucao de uma bicicleta")
	public void returnBikeTest() throws Exception {
		/*
		 * A ideia inicial era mandar o dto como LocalDateTime, mas houveram problemas
		 * na serializacao do conteudo. Essa foi a solucao: enviar um sinal booleano
		 * para indicar a devolucao, entao a hora `e capturada no momento que o sinal
		 * chega no controller
		 */
		// cenario
		String cpf = "389.831.240-24";
		ReturnedBikeDTO dto = ReturnedBikeDTO.builder().returned(true).clientCpf(cpf).build();

		Client cliente = Client.builder().id(1l).cpf(cpf).build();

		Rent rent = Rent.builder().id(1l).client(cliente).build();

		BDDMockito.given(clienteService.findByCpf(Mockito.anyString())).willReturn(Optional.of(cliente));
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(rent));

		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(RENT_API.concat("/1"))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isOk());
		Mockito.verify(service, Mockito.times(1)).update(rent);

	}

	@Test
	@DisplayName("Deve retornar 404 quando tentar devolver uma bicicleta inexistente")
	void returnInexistentBike() throws Exception {

		// cenario
		Client client = Client.builder().id(1l).cpf("389.831.240-24").build();
		ReturnedBikeDTO dto = ReturnedBikeDTO.builder().returned(true).clientCpf(client.getCpf()).build();

		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		BDDMockito.given(clienteService.findByCpf("389.831.240-24")).willReturn(Optional.of(client));

		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch(RENT_API.concat("/1"))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		// verificacao
		mvc.perform(request).andExpect(status().isNotFound());

	}

	@Test
	@DisplayName("Deve retornar erro caso haja incongruencia entre o identificador (cpf) fornecido pelo cliente e o registrado no aluguel")
	void mustNotReturnABikeAClientDidntRent() throws Exception {

		// cenario
		/*
		 * O metodo vai testar se o cliente que pretende
		 * devolver a bicicleta e o mesmo que a alugou
		 */
		Client client = Client.builder()
							  .id(1l)
							  .cpf("389.831.240-24")
							  .build();
		Rent rent = Rent.builder()
							  .id(1l)
							  .build();
		
		ReturnedBikeDTO dto = ReturnedBikeDTO
							  .builder()
							  .returned(true)
							  .build();
		
		BDDMockito.given(service.getById(Mockito.anyLong()))
				  .willReturn(Optional.of(rent));

		BDDMockito.given(clienteService.findByCpf(Mockito.anyString()))
				  .willReturn(Optional.of(client));

		String json = new ObjectMapper().writeValueAsString(dto);

		// execucao
		MockHttpServletRequestBuilder request = 
						MockMvcRequestBuilders
							.patch(RENT_API.concat("/1"))
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON)
							.content(json);
		
		// verificacao
		mvc.perform(request).andExpect(status().isBadRequest());
		/*
		 * verifica que o metodo update nunca foi
		 * chamado pelo service correspondente
		 */
		Mockito.verify(service, Mockito.never()).update(rent);

	}

	@Test
	@DisplayName("Deve filtrar alugueis")
	void findRentBikesTest() throws Exception {

		// cenario
		long id = 1l;
		Rent rent = Rent.builder().id(id).customerEmail("costumer@mail.com").rentHoursDuration(2 * 24)
				.rentDate(LocalDateTime.now()).build();

		Bike bike = Bike.builder().id(id).chassi("123abc").build();

		Client client = Client.builder().id(id).cpf("389.831.240-24").build();
		rent.setBike(bike);
		rent.setClient(client);
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		BDDMockito.given(service.find(Mockito.any(RentFilterDTO.class), Mockito.any(Pageable.class)))
					.willReturn(new PageImpl<Rent>(Arrays.asList(rent), pageRequest, 1));

		String queryString = String.format("?chassi=%s&cliente=%s&page=0&size=10", 
								bike.getChassi(), client.getCpf());
		
		// execucao
		MockHttpServletRequestBuilder request =
					MockMvcRequestBuilders
						.get(RENT_API.concat(queryString))
						.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(jsonPath("content", Matchers.hasSize(1)))
			.andExpect(jsonPath("totalElements").value(1))
			.andExpect(jsonPath("pageable.pageSize").value(10))
			.andExpect(jsonPath("pageable.pageNumber").value(0));

	}

}
