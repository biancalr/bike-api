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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bikeapi.api.dto.BikeDTO;
import br.com.bikeapi.exception.BusinessException;
import br.com.bikeapi.model.entity.Bike;
import br.com.bikeapi.service.BikeService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BikeController.class)
@AutoConfigureMockMvc
public class BikeControllerTest {

	private static String BIKE_API = "/api/bikes";

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private BikeService service;

	@Test
	@DisplayName("Deve criar uma bicicleta com sucesso")
	public void createBykeTest() throws Exception {

		// cenario
		Long id = 1l;
		BikeDTO dto = createNewBike();
		Bike savedBike = Bike.builder()
							.chassi("123456")
							.id(id)
							.model(createNewBike().getModel())
							.color(createNewBike().getColor())
							.companyProperty(createNewBike().getCompanyProperty())
							.build();

		BDDMockito.given(service.save(Mockito.any(Bike.class))).willReturn(savedBike);

		String json = new ObjectMapper().writeValueAsString(dto);
		
		// execucao

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BIKE_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);

		// verificacao
		mvc.perform(request)
		   .andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").isNotEmpty())
			.andExpect(jsonPath("chassi").value("123456"))
			.andExpect(jsonPath("model").value(dto.getModel()))
			.andExpect(jsonPath("color").value(dto.getColor()))
			.andExpect(jsonPath("companyProperty").value(true));

	}
	
	@Test
	@DisplayName("Deve lancar erro de validacao quando nao houver dados suficientes para a criacao da bicicleta")
	public void createInvalidBikeTest() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(new BikeDTO());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BIKE_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", Matchers.hasSize(4)));
		
	}
	
	@Test
	@DisplayName("Deve lancar erro ao tentar cadastrar uma bicicleta com chassi ja utilizado por outro")
	public void createBookWithDuplicatedChassi() throws Exception {
		
		// cenario
		BikeDTO dto = createNewBike();
		String json = new ObjectMapper().writeValueAsString(dto);
		String mensagemErro = "Chassi ja cadastrado.";
		BDDMockito.given(service.save(Mockito.any(Bike.class)))
				  .willThrow(new BusinessException("Chassi ja cadastrado."));
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BIKE_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", Matchers.hasSize(1)))
			.andExpect(jsonPath("errors[0]").value(mensagemErro));
		
	}

	private BikeDTO createNewBike() {
		return BikeDTO.builder()
					  .chassi("123456")
					  .color("preta")
					  .model("Caloi Vulcan")
					  .companyProperty(true)
					  .build();
	}

	@Test
	@DisplayName("Deve obter informacoes de uma bicicleta")
	public void getBikeDetailsTest() throws Exception {
		
		// cenario
		Long id = 1l;
		Bike bike = Bike.builder()
						.id(id)
						.chassi(createNewBike().getChassi())
						.color(createNewBike().getColor())
						.model(createNewBike().getModel())
						.companyProperty(createNewBike().getCompanyProperty())
						.build();
		BDDMockito.given(service.getById(id))
				.willReturn(Optional.of(bike));
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BIKE_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(1l))
			.andExpect(jsonPath("chassi").value("123456"))
			.andExpect(jsonPath("model").value("Caloi Vulcan"))
			.andExpect(jsonPath("color").value("preta"))
			.andExpect(jsonPath("companyProperty").value(true));
		
	}
	
	@Test
	@DisplayName("Deve retornar ResourceNotFound quando a bicicleta procurada nao existir")
	public void bikeNotFoundTest() throws Exception{
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
				  .willReturn(Optional.empty());
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BIKE_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve deletar uma bicicleta")
	public void deleteBikeTest() throws Exception {
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
				  .willReturn(Optional.of(Bike.builder().id(1l).build()));
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BIKE_API.concat("/" + 1));
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNoContent());
		
	}
	
	
	@Test
	@DisplayName("Deve lancar erro ao tentar remover uma bicicleta inexistente na base")
	public void deleteInvalidBikeTest() throws Exception {
		
		// cenario
		BDDMockito.given(service.getById(Mockito.anyLong()))
					.willReturn(Optional.empty());
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BIKE_API.concat("/" + 1));
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve atualizar uma bicicleta")
	public void updateBikeTest() throws Exception{
		
		// cenario
		String json = new ObjectMapper().writeValueAsString(createNewBike());
		long id = 1l;
		
		Bike toUpdateBike = Bike.builder().id(id).chassi("123456").color("color").model("model").companyProperty(true).build();
		BDDMockito.given(service.getById(id))
				.willReturn(Optional.of(toUpdateBike));
		
		Bike updatedBike = Bike.builder()
							.id(id)
							.chassi("123456")
							.color("preta")
							.model("Caloi Vulcan")
							.companyProperty(true)
							.build();
		BDDMockito.given(service.update(toUpdateBike)).willReturn(updatedBike);
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BIKE_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(id))
			.andExpect(jsonPath("chassi").value(createNewBike().getChassi()))
			.andExpect(jsonPath("model").value(createNewBike().getModel()))
			.andExpect(jsonPath("color").value(createNewBike().getColor()))
			.andExpect(jsonPath("companyProperty").value(createNewBike().getCompanyProperty()));
		
		
	}
	
	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar uma bicicleta inexistente na base")
	public void updateInexistentBikeTest() throws Exception {
		
		// cenario
		String json = new ObjectMapper().writeValueAsString(createNewBike());
		BDDMockito.given(service.getById(Mockito.anyLong()))
				  .willReturn(Optional.empty());
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BIKE_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request).andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve filtrar bicicletas")
	public void findBikesTest() throws Exception{
		
		// cenario
		long id = 1l;
		Bike bike = Bike.builder()
						.id(id)
						.chassi(createNewBike().getChassi())
						.color(createNewBike().getColor())
						.model(createNewBike().getModel())
						.companyProperty(createNewBike().getCompanyProperty())
						.build();
		
		BDDMockito.given(service.find(Mockito.any(Bike.class), Mockito.any(Pageable.class)))
				  .willReturn(new PageImpl<Bike>(Arrays.asList(bike), PageRequest.of(0, 10), 1));
		
		String queryString = String.format("?model=%s&color=%s&companyProperty=%s&page=0&size=10", bike.getModel(), bike.getColor(), bike.getCompanyProperty());
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BIKE_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("content", Matchers.hasSize(1)))
			.andExpect(jsonPath("totalElements").value(1))
			.andExpect(jsonPath("pageable.pageSize").value(10))
			.andExpect(jsonPath("pageable.pageNumber").value(0));
		
	}
	
	@Test
	@DisplayName("Deve obter uma bicicleta pelo chassi")
	public void getBikeInfoByChassi() throws Exception {
		
		// cenario
		String chassi = "123abc";
		
		BDDMockito.given(service.findByChassi(chassi))
				.willReturn(Optional.of(Bike.builder()
											.id(1l)
											.chassi(chassi)
											.build()));	
		
		// execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BIKE_API.concat("/info/" + chassi))
				.accept(MediaType.APPLICATION_JSON);
		
		// verificacao
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(1))
			.andExpect(jsonPath("chassi").value("123abc"));
		
	}
	
	
}
