package com.parkar.parksaathi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static com.parkar.parksaathi.constant.Constants.*;

@Configuration
public class SwaggerConfig {

    public static final String COMPONENTS_PARAMETERS = "#/components/parameters/";

    @Bean
    public OperationCustomizer addGlobalHeaders() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new Parameter().$ref(COMPONENTS_PARAMETERS + HEADER_DEVICE));
            operation.addParametersItem(new Parameter().$ref(COMPONENTS_PARAMETERS + HEADER_CORRELATION_ID));
            operation.addParametersItem(new Parameter().$ref(COMPONENTS_PARAMETERS + HEADER_VERSION));
            return operation;
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("ParkSaathi API").version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(
                        new Components()
                                .addSecuritySchemes(BEARER_AUTH,
                                        new SecurityScheme()
                                                .name(BEARER_AUTH)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme(BEARER)
                                                .bearerFormat(JWT)
                                )

                                .addParameters(X_PARKSAATHI_DEVICE,
                                        new Parameter()
                                                .in(HEADER)
                                                .name(X_PARKSAATHI_DEVICE)
                                                .description("Device type (mobile/web)")
                                                .required(true)
                                                .schema(new StringSchema()._enum(Arrays.asList("mobile", "web")))
                                )
                                .addParameters(X_PARKSAATHI_CORRELATION_ID,
                                        new Parameter()
                                                .in(HEADER)
                                                .name(X_PARKSAATHI_CORRELATION_ID)
                                                .description("Correlation ID (UUID)")
                                                .required(true)
                                                .schema(new StringSchema().format("uuid"))
                                )
                                .addParameters(X_PARKSAATHI_VERSION,
                                        new Parameter()
                                                .in(HEADER)
                                                .name(X_PARKSAATHI_VERSION)
                                                .description("API Version (1.0.0)")
                                                .required(true)
                                                .schema(new StringSchema()._default("1.0.0"))
                                )
                );
    }
}
