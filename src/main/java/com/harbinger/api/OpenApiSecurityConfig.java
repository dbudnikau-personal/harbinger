package com.harbinger.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Registers the X-API-Key security scheme so swagger-ui offers the key field. */
@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiSecurityConfig.API_KEY_SCHEME))
@SecurityScheme(name = OpenApiSecurityConfig.API_KEY_SCHEME, type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER, paramName = ApiKeyFilter.API_KEY_HEADER)
class OpenApiSecurityConfig {

    static final String API_KEY_SCHEME = "apiKey";
}
