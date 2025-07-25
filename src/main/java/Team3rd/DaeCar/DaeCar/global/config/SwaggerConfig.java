package Team3rd.DaeCar.DaeCar.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DaeCar API")
                        .description("대카 애플리케이션 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Team3rd")
                                .email("team3rd@example.com")));
    }
}