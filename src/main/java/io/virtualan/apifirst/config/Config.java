package io.virtualan.apifirst.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
@ComponentScan(basePackages = "io.virtualan.apifirst")
public class Config {

    @Bean
    public Docket api() {
        final String BASE_PACKAGE = "io.virtualan.apifirst";
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Api First tutorial")
                        .description("This is the awesome TaskList management Api!")
                        .contact(new Contact("Virtualan Sofware", "https://www.virtualan.io", "elan.thangamani@virtualan.io"))
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE))
                // exclude HomeController
                .apis(Predicates.not(RequestHandlerSelectors.basePackage(BASE_PACKAGE + ".swagger")))
                .build();
    }
}
