package com.epam.productos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de productos")
                        .description("CRUD técnico de productos. El precio debe ser mayor que cero. "
                                + "El stock no puede ser negativo y define el estado de inventario. "
                                + "La baja es lógica.")
                        .version("1.0.0"))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
}
