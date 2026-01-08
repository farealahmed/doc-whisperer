package com.docwhisperer.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Doc Whisperer API.
 * <p>
 * This configuration provides comprehensive API documentation that can be accessed via Swagger UI.
 * It includes metadata about the API, contact information, and server configurations.
 * </p>
 */
@Configuration
public class OpenApiConfiguration {

    /**
     * Configures the OpenAPI specification for the application.
     * 
     * @return OpenAPI object with custom metadata and configuration
     */
    @Bean
    public OpenAPI docWhispererOpenAPI() {
        // Development server
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        // Production server (update with your actual production URL when deployed)
        Server prodServer = new Server();
        prodServer.setUrl("https://api.docwhisperer.com");
        prodServer.setDescription("Production Server");

        // Contact information
        Contact contact = new Contact();
        contact.setEmail("support@docwhisperer.com");
        contact.setName("Doc Whisperer Team");
        contact.setUrl("https://www.docwhisperer.com");

        // License information
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // API Info
        Info info = new Info()
                .title("Doc Whisperer API")
                .version("1.0.0")
                .contact(contact)
                .description("AI-powered document Q&A system using RAG (Retrieval-Augmented Generation). " +
                        "Upload PDF and DOCX documents, and interact with them through natural language questions.")
                .termsOfService("https://www.docwhisperer.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}