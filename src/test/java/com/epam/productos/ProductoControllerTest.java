package com.epam.productos;

import com.epam.productos.dto.ProductoRequest;
import com.epam.productos.exception.InventarioInvalidoException;
import com.epam.productos.exception.PrecioInvalidoException;
import com.epam.productos.repository.ProductoRepository;
import com.epam.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiar() {
        productoRepository.deleteAll();
    }

    @Test
    void registraUnProductoDisponible() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Teclado mecanico","precio":120.00,"stock":8}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Teclado mecanico"))
                .andExpect(jsonPath("$.precio").value(120.00))
                .andExpect(jsonPath("$.stock").value(8))
                .andExpect(jsonPath("$.estadoInventario").value("DISPONIBLE"));
    }

    @Test
    void registraUnProductoAgotadoCuandoElStockEsCero() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Mouse","precio":15.50,"stock":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoInventario").value("AGOTADO"))
                .andExpect(jsonPath("$.stock").value(0));
    }

    @Test
    void rechazaPrecioQueNoEsMayorQueCeroYStockNegativo() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Cable","precio":0,"stock":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensajes", hasItem(containsString("mayor que cero"))))
                .andExpect(jsonPath("$.mensajes", hasItem(containsString("stock"))));

        assertThat(productoRepository.count()).isZero();
    }

    @Test
    void rechazaPrecioNegativo() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Cable","precio":-0.01,"stock":3}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensajes", hasItem(containsString("mayor que cero"))));
    }

    @Test
    void elServicioNoPersisteUnPrecioQueNoEsMayorQueCero() {
        assertThatThrownBy(() -> productoService.registrar(
                new ProductoRequest("Cable", new BigDecimal("0.00"), 4)))
                .isInstanceOf(PrecioInvalidoException.class)
                .hasMessage("El precio debe ser mayor que cero");
        assertThat(productoRepository.count()).isZero();
    }

    @Test
    void elServicioNoPersisteStockNegativo() {
        assertThatThrownBy(() -> productoService.registrar(
                new ProductoRequest("Cable", new BigDecimal("10.00"), -2)))
                .isInstanceOf(InventarioInvalidoException.class);
        assertThat(productoRepository.count()).isZero();
    }

    @Test
    void consultaElCatalogoCompletoYPaginado() throws Exception {
        registrar("Manzana", "30.00", 2);
        registrar("Arandano", "10.00", 1);
        registrar("Banana", "20.00", 0);

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginado").value(false))
                .andExpect(jsonPath("$.productos.length()").value(3))
                .andExpect(jsonPath("$.productos[0].nombre").value("Arandano"))
                .andExpect(jsonPath("$.page").doesNotExist());

        mockMvc.perform(get("/api/productos").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginado").value(true))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.productos.length()").value(2))
                .andExpect(jsonPath("$.productos[0].nombre").value("Arandano"));

        mockMvc.perform(get("/api/productos").param("nombre", "ban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginado").value(false))
                .andExpect(jsonPath("$.productos.length()").value(1))
                .andExpect(jsonPath("$.productos[0].nombre").value("Banana"))
                .andExpect(jsonPath("$.productos[0].estadoInventario").value("AGOTADO"));
    }

    @Test
    void rechazaUnaPaginaNegativa() throws Exception {
        mockMvc.perform(get("/api/productos").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizaSoloElPrecio() throws Exception {
        String id = registrar("Teclado mecanico", "120.00", 8);

        mockMvc.perform(patch("/api/productos/{id}/precio", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio":50.00,"nombre":"Otro","stock":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(50.00))
                .andExpect(jsonPath("$.nombre").value("Teclado mecanico"))
                .andExpect(jsonPath("$.stock").value(8))
                .andExpect(jsonPath("$.estadoInventario").value("DISPONIBLE"));

        mockMvc.perform(patch("/api/productos/{id}/precio", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensajes", hasItem(containsString("mayor que cero"))));

        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(50.00));
    }

    @Test
    void laBajaLogicaOcultaElProducto() throws Exception {
        String id = registrar("Monitor", "400.00", 2);
        registrar("Mouse", "25.00", 1);

        mockMvc.perform(delete("/api/productos/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/productos/{id}/precio", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio":10.00}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/productos/{id}", id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos.length()").value(1))
                .andExpect(jsonPath("$.productos[0].nombre").value("Mouse"));
    }

    @Test
    void publicaElContratoOpenApiConElRecursoDePrecio() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/productos/{id}/precio'].patch").exists())
                .andExpect(jsonPath("$.info.title").value("API de productos"));
    }

    private String registrar(String nombre, String precio, int stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s","precio":%s,"stock":%d}
                                """.formatted(nombre, precio, stock)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        int inicio = body.indexOf("\"id\":\"") + 6;
        int fin = body.indexOf('"', inicio);
        return body.substring(inicio, fin);
    }
}
