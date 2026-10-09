package com.example.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.dto.PresentationDto;
import com.example.dto.ProductDto;
import com.example.entities.Presentation;
import com.example.entities.Product;
import com.example.services.ProductService;
import com.example.spring_security_jwt.payload.request.LoginRequest;
import com.example.utilities.FileDownloadUtil;
import com.example.utilities.FileUploadUtil;
import com.example.utilities.FileUtil;

import tools.jackson.databind.ObjectMapper;

//@WebMvcTest(ProductController.class) se comenta y se agrega la siguiente anotación
@SpringBootTest 

/**
 * La anotacion anterior es la recomendada para implementar test de Integracion,
 * a la capa de controladores que conlleva la realizacion de peticiones HTTP.
 * 
 * Esta anotacion no serviria si tuviesemos implementada la seguridad con Spring
 * Security porque no carga todo el contexto de Spring. Cuando se implemente la
 * seguridad, comentaremos esta anotacion y utilizaremos @SpringBootTest
 */

/*
 * La siguiente anotacion se utiliza cuando queremos utilizar una base de datos
 * real, que no sea H2 Database, MySQL por ejemplo, y que al terminar la prueba
 * se deje la base de datos tal y como estaba
 */
@AutoConfigureTestDatabase(replace = Replace.NONE)

/**
 * Se necesita MockMvc para realizar peticiones a los end points, lo cual
 * suministra y configura la anotacion siguiente
 */
@AutoConfigureMockMvc
class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	ProductService productService;

	@MockitoBean
	FileUploadUtil fileUploadUtil;

	@MockitoBean
	FileDownloadUtil fileDownloadUtil;
	
	@MockitoBean
	FileUtil fileUtil;
	
	@Autowired
	ObjectMapper objectMapper;
	
	List<ProductDto> productsDto = new ArrayList<>();
	Presentation presentation1, presentation2;
	PresentationDto presentationDto1, presentationDto2;
	Product product1, product2;
	ProductDto productDto1, productDto2;

	String token;
	
	@BeforeEach
	void setUp() throws Exception {

		/*Necesitamos obtener un token válido para presentarlo en cada test.
		Seleccionamos la clase creada en el payload.request LoginRequest.
		Usamos un usuario ADMIN (admin1) para los tests de lectura/mutacion.*/
		this.token = obtenerToken("admin1", "Temp2026$$##");
		
		presentation1 = Presentation.builder()
				.name("decenas")
				.description("Por decenas")
				.build();
		
		presentation2 = Presentation.builder()
				.name("unidades")
				.description("Por unidades")
				.build();
			
		product1 = Product.builder()
				.name("Camara")
				.description("HP Camara")
				.price(new BigDecimal(500))
				.stock(1900)
				.productImage(null)
				.presentation(presentation1)
				.build();
		
			
		product2 = Product.builder()
				.name("Frigorifico")
				.description("General Electric")
				.price(new BigDecimal(2500))
				.stock(3900)
				.productImage(null)
				.presentation(presentation2)
				.build();
		
		// Construimos los DTOs que ahora devuelve la capa de servicio (la capa de
		// presentacion ya no trabaja con la entidad Product)
		presentationDto1 = new PresentationDto(presentation1.getId(), presentation1.getName());
		presentationDto2 = new PresentationDto(presentation2.getId(), presentation2.getName());

		productDto1 = new ProductDto(product1.getId(), product1.getName(), product1.getDescription(),
				product1.getStock(), product1.getPrice(), product1.getProductImage(), presentationDto1);

		productDto2 = new ProductDto(product2.getId(), product2.getName(), product2.getDescription(),
				product2.getStock(), product2.getPrice(), product2.getProductImage(), presentationDto2);

		productsDto.add(productDto1);
		productsDto.add(productDto2);
	}

	/**
	 * Realiza el login contra /api/auth/signin y devuelve la cabecera
	 * Authorization ("Bearer <token>") para el usuario indicado.
	 */
	private String obtenerToken(String username, String password) throws Exception {

		LoginRequest loginRequest = LoginRequest.builder()
				.username(username)
				.password(password)
				.build();

		String jsonLoginRequest = objectMapper.writeValueAsString(loginRequest);

		MvcResult mvcResult = this.mockMvc.perform(post("/api/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonLoginRequest))
				.andReturn();

		String contentAsString = mvcResult.getResponse().getContentAsString();
		JSONObject jsonObject = new JSONObject(contentAsString);

		return "Bearer " + jsonObject.getString("token");
	}

	@Test
	@DisplayName("Controller Test que recupera todos los productos")
	void testFindAll() throws Exception {

		// given
		
		given(productService.findAll(Sort.by("name")))
			.willReturn(productsDto);

		// when => Realizar la peticion (request) HTTP, mediante el metodo GET
		// al end point de products ("/products"). Aqui se utiliza MockMvc

		ResultActions response = mockMvc
				.perform(get("/products")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.token));
		// then

		response.andExpect(status().isOk()).andDo(print())
				// El CollectionModel serializa su contenido bajo "content"
				.andExpect(jsonPath("$.products.content.size()",
						is(productsDto.size())))
				// Enlace self de la coleccion (CollectionModel). Al tener @RequestParam
				// opcionales, Spring HATEOAS lo expone como plantilla /products{?page,size}
				.andExpect(jsonPath("$.products.links[0].rel", is("self")))
				.andExpect(jsonPath("$.products.links[0].href",
						is("http://localhost/products{?page,size}")))
				// La coleccion tambien anuncia la operacion de alta (POST)
				.andExpect(jsonPath("$.products.links[1].rel", is("create")))
				// Cada producto es un RepresentationModel con enlaces de mutacion:
				// self, update, delete y el enlace a la coleccion
				.andExpect(jsonPath("$.products.content[0].links[0].rel", is("self")))
				.andExpect(jsonPath("$.products.content[0].links[0].href",
						is("http://localhost/products/" + productDto1.getId())))
				.andExpect(jsonPath("$.products.content[0].links[1].rel", is("update")))
				.andExpect(jsonPath("$.products.content[0].links[2].rel", is("delete")))
				.andExpect(jsonPath("$.products.content[0].links[3].rel", is("productos")))
				// El propio ProductDto (RepresentationModel) expone sus campos + links
				.andExpect(jsonPath("$.products.content[0].name",
						is(productDto1.getName())));

	}

	@Test
	@DisplayName("Controller Test que recupera los productos paginados con PagedModel")
	void testFindAllPaginado() throws Exception {

		// given: una pagina de 1 elemento sobre un total de 2 (2 paginas)
		int page = 0;
		int size = 1;
		Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
		Page<ProductDto> productPage = new PageImpl<>(List.of(productDto1), pageable, productsDto.size());

		given(productService.findAll(any(Pageable.class))).willReturn(productPage);

		// when
		ResultActions response = mockMvc
				.perform(get("/products")
				.param("page", String.valueOf(page))
				.param("size", String.valueOf(size))
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", this.token));

		// then
		response.andExpect(status().isOk()).andDo(print())
				// Metadata de paginacion expuesta bajo "page" por el PagedModel
				.andExpect(jsonPath("$.products.page.size", is(size)))
				.andExpect(jsonPath("$.products.page.number", is(page)))
				.andExpect(jsonPath("$.products.page.totalElements", is(productsDto.size())))
				.andExpect(jsonPath("$.products.page.totalPages", is(2)))
				// Contenido de la pagina actual
				.andExpect(jsonPath("$.products.content.size()", is(1)))
				// Enlaces generados por el assembler: self (pagina actual), create (POST)
				// y navegacion first, last y next. En la primera pagina no hay prev
				.andExpect(jsonPath("$.products.links[0].rel", is("self")))
				.andExpect(jsonPath("$.products.links[0].href",
						is("http://localhost/products?page=0&size=1")))
				.andExpect(jsonPath("$.products.links[1].rel", is("create")))
				.andExpect(jsonPath("$.products.links[2].rel", is("first")))
				.andExpect(jsonPath("$.products.links[3].rel", is("last")))
				.andExpect(jsonPath("$.products.links[3].href",
						is("http://localhost/products?page=1&size=1")))
				.andExpect(jsonPath("$.products.links[4].rel", is("next")))
				.andExpect(jsonPath("$.products.links[4].href",
						is("http://localhost/products?page=1&size=1")))
				.andExpect(jsonPath("$.products.links[5]").doesNotExist());
	}

	@Test
	@DisplayName("Controller Test para Persistir un Producto")
	void testSaveProduct()  {
		
		// given
		given(productService.save(any(Product.class)))
			.willReturn(productDto1);
		
		// when
		
		/* Convertir el producto a formato JSON, es decir, una cadena (String)
		 * en formato de JSON, lo cual hace el objectMapper que hemos inyectado como 
		 * dependencia al principio de la clase bajo Test */
		
		String jsonStringProduct = objectMapper.writeValueAsString(product1);
		
		MockMultipartFile bytesArrayProduct = new MockMultipartFile(
				    "product", 
				    null, 
				    "application/json", 
				    jsonStringProduct.getBytes());
		
		try {
				mockMvc
				    .perform(multipart("/products")
					.file(bytesArrayProduct)
					.file("file", null)
					.header("Authorization", this.token))
				    	.andDo(print())
				    	.andExpect(status().isCreated())
				    	.andExpect(jsonPath("$.product.name",
		  			is(product1.getName())))
				    	// El ProductDto (RepresentationModel) lleva los enlaces del assembler
				    	.andExpect(jsonPath("$.product.links[0].rel", is("self")))
				    	.andExpect(jsonPath("$.product.links[1].rel", is("update")))
				    	.andExpect(jsonPath("$.product.links[2].rel", is("delete")))
				    	.andExpect(jsonPath("$.product.links[3].rel", is("productos")));
		  	
		  
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		// then
		
	}

	@Test
	@DisplayName("Controller Test para recuperar un producto por su ID")
	void testRecuperarProductoPorSuID() throws Exception {
		
		// given
		
		int productId = 1;
		
		given(productService.findById(productId))
			.willReturn(productDto1);
		
		// when
		mockMvc.perform(get("/products/{id}",
				productId)
				.header("Authorization", this.token))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$['producto encontrado: '].name",
						is(product1.getName())))
				// El ProductDto (RepresentationModel) lleva los enlaces del assembler
				.andExpect(jsonPath("$['producto encontrado: '].links[0].rel", is("self")))
				.andExpect(jsonPath("$['producto encontrado: '].links[1].rel", is("update")))
				.andExpect(jsonPath("$['producto encontrado: '].links[2].rel", is("delete")))
				.andExpect(jsonPath("$['producto encontrado: '].links[3].rel", is("productos")));
	}

	@Test
	@DisplayName("Controller Test Producto no encontrado")
	void testProductoNoEncontrado() throws Exception {
		
		// given
		given(productService.findById(20)).willReturn(null);
		
		// when
		
		mockMvc.perform(get("/products/{id}", 20)
				.header("Authorization", this.token))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
    @Test 
    @DisplayName("Controller Test que actualiza un producto con su imagen")
    void testUpdateProduct() throws Exception{

        //given
        int id = 1;
        given(productService.findById(id)).willReturn(productDto1);
        given(productService.save(any(Product.class)))
                .willReturn(productDto1);

        //when
        String jsonStringProduct = objectMapper.writeValueAsString(product1);

        MockMultipartFile bytesArrayProduct = new MockMultipartFile("product", 
                            null,
                            "application/json",
                            jsonStringProduct.getBytes());

        ResultActions response = this.mockMvc
        		.perform(multipart("/products/{id}", id)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .file("image", null)
                        .file(bytesArrayProduct)
						.header("Authorization", this.token));

        //then
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['producto actualizado: '].name",
            		is(product1.getName())))
            .andExpect(jsonPath("$['producto actualizado: '].description",
            		is(product1.getDescription())))
            // El ProductDto (RepresentationModel) lleva los enlaces del assembler
            .andExpect(jsonPath("$['producto actualizado: '].links[0].rel", is("self")))
            .andExpect(jsonPath("$['producto actualizado: '].links[1].rel", is("update")))
            .andExpect(jsonPath("$['producto actualizado: '].links[2].rel", is("delete")))
            .andExpect(jsonPath("$['producto actualizado: '].links[3].rel", is("productos")));
        

    }
    
    @Test 
    @DisplayName("Controller Test que elimina un producto")
    void testDeleteProduct() throws Exception{

        //given
        int ProductId = 1;

        given(productService.findById(ProductId)).willReturn(productDto1);
        doNothing().when(productService).delete(ProductId);

        //when
        mockMvc.perform(delete("/products/{id}", ProductId)
				.header("Authorization", this.token))
                .andExpect(status().isOk())
                // CollectionModel vacio: self + la operacion de alta (create)
                .andExpect(jsonPath("$.enlaces.links[0].rel", is("self")))
                .andExpect(jsonPath("$.enlaces.links[1].rel", is("create")));

    }

    @Test
    @DisplayName("Controller Test: un USER no recibe enlaces de mutacion en un producto")
    void testEnlacesItemCondicionadosPorRolUser() throws Exception {

        //given: usuario con solo ROLE_USER y un producto existente
        String userToken = obtenerToken("user1", "Temp2026$$##");
        given(productService.findById(1)).willReturn(productDto1);

        //when / then: solo self y el enlace a la coleccion, sin update ni delete
        mockMvc.perform(get("/products/{id}", 1)
                .header("Authorization", userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['producto encontrado: '].links[0].rel", is("self")))
                .andExpect(jsonPath("$['producto encontrado: '].links[1].rel", is("productos")))
                .andExpect(jsonPath("$['producto encontrado: '].links[2]").doesNotExist());
    }

    @Test
    @DisplayName("Controller Test: un USER no recibe el enlace create en la coleccion")
    void testEnlacesColeccionCondicionadosPorRolUser() throws Exception {

        //given: usuario con solo ROLE_USER
        String userToken = obtenerToken("user1", "Temp2026$$##");
        given(productService.findAll(Sort.by("name"))).willReturn(productsDto);

        //when / then: la coleccion solo expone self, sin el enlace create (POST)
        mockMvc.perform(get("/products")
                .header("Authorization", userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.links[0].rel", is("self")))
                .andExpect(jsonPath("$.products.links[1]").doesNotExist());
    }

}











