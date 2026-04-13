package org.java.assesment.quiz_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quizServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Service API")
                        .description("REST API for managing quiz categories, exams, questions and answers")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("JavaPrep")
                                .email("admin@javaprep.dev")));
    }
}
