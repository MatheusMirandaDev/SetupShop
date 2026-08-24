package br.com.setupshop.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI setupShopOpenApi() {
        Info apiInfo = new Info()
            .title("SetupShop API")
            .description("API for managing products, customers and orders")
            .version("v1");

        return new OpenAPI().info(apiInfo);
    }
}
