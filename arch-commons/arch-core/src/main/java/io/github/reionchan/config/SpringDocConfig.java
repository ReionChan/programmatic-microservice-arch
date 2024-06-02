package io.github.reionchan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.github.reionchan.consts.Profile.DEV;
import static io.github.reionchan.consts.Profile.TEST;

/**
 * 文档配置类
 *
 * @author Reion
 * @date 2023-06-06
 **/
@Configuration
@Profile({DEV, TEST})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@OpenAPIDefinition
@SecurityScheme(name = "security_auth", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        password = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
            refreshUrl = "${springdoc.oAuthFlow.tokenUrl}",
            extensions = { @Extension(
                    name = "passParams",
                    properties = {
                        @ExtensionProperty(name = "username", value = "wukong"),
                        @ExtensionProperty(name = "password", value = "wukong")
                    }
            )},
            scopes = { @OAuthScope(name = "WEB", description = "SpringDoc API")}
        )))
public class SpringDocConfig {

    public static final String APPLICATION_JSON_VALUE = "application/json";

    /**
     * 客户端异常响应引用名称
     */
    public static final String RESPONSE_FAIL = "failResponse";
    /**
     * 服务端异常响应引用名称
     */
    public static final String RESPONSE_ERROR = "errorResponse";

    public static final String RESPONSE_NOT_ALLOW = "notAllowResponse";

    private static final String RESPONSE_JSON_FILE = "openapi/response.json";

    public static Map<String, Object> templateMap = read();
    public static Map<String, Object> read() {
        try {
            String content = new String(SpringDocConfig.class.getClassLoader().getResourceAsStream(RESPONSE_JSON_FILE).readAllBytes());
            return new ObjectMapper().readValue(content, Map.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Swagger UI 接口测试 - 网关服务器地址
     */
    @Value("${springdoc.gateway.url:http://localhost:9000}")
    private String gatewayUrl;

    @Bean
    // @formatter:off
    public OpenAPI baseOpenAPI(@Value("${spring.application.name}") String appName, @Value("${springdoc.version}") String appVersion) throws IOException {

        // 定义通用客户端异常返回格式
        ApiResponse failResponse = new ApiResponse().content(
                new Content().addMediaType(APPLICATION_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType().addExamples("default",
                                new Example().value(templateMap.get(RESPONSE_FAIL))))
        ).description("客户端异常消息");

        // 定义通用客户端异常返回格式
        ApiResponse notAllowResponse = new ApiResponse().content(
                new Content().addMediaType(APPLICATION_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType().addExamples("default",
                                new Example().value(templateMap.get(RESPONSE_NOT_ALLOW))))
        ).description("客户端异常消息");

        // 定义通用服务端异常返回格式
        ApiResponse errorResponse = new ApiResponse().content(
                new Content().addMediaType(APPLICATION_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType().addExamples("default",
                                new Example().value(templateMap.get(RESPONSE_ERROR))))
        ).description("服务端异常消息");

        // 通用组件
        Components components = new Components();
        components.addResponses(RESPONSE_FAIL, failResponse);
        components.addResponses(RESPONSE_NOT_ALLOW, notAllowResponse);
        components.addResponses(RESPONSE_ERROR, errorResponse);

        return new OpenAPI()
            // 设置 Swagger UI 页面调用测试服务器地址
            .servers(List.of(new Server().url(gatewayUrl + "/" + appName)))
            // 设置 Swagger 文档中通用组件
            .components(components)
            .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("security_auth"))
            .info(new Info()
                .title("Arch 微服务架构")
                .description("基于『Spring Cloud 框架\uD83C\uDF43』编程式\uD83D\uDC68\u200D\uD83D\uDCBB\u200D、非云原生⛔\uFE0F\uD83D\uDCAD 的骨架\uD83C\uDF56")
                .version(appVersion)
                // 联系信息
                .contact(new Contact()
                    .name("ReionChan")
                    .email("reion78@gmail.com")
                    .url("https://reionchan.github.io/"))
                    // 许可协议
                    .license(new License()
                        .name("Apache 2.0 license")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")
                        .identifier("Apache-2.0"))
                    // 服务条款的 URL 地址
                    //.termsOfService("/server/termsOfService")
        );
    }
    // @formatter:on
}
