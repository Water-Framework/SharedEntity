package it.water.shared.entity;

import com.intuit.karate.junit5.Karate;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.integration.UserIntegrationClient;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = SharedEntityApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {
        "water.rest.security.jwt.validate=false",
        "water.testMode=true"
})
public class SharedEntityRestSpringApiTest {

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private UserIntegrationClient userIntegrationClient;

    @Autowired
    private Runtime runtime;

    @BeforeEach
    void impersonateAdmin() {
        //jwt token service is disabled, we just inject admin user for bypassing permission system
        //just remove this line if you want test with permission system working
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
    }

    @Karate.Test
    Karate restInterfaceTest() {
        return Karate.run("../SharedEntity-service/src/test/resources/karate")
                .systemProperty("testEntityResourceId", String.valueOf(1))
                .systemProperty("userId", String.valueOf(runtime.getSecurityContext().getLoggedEntityId()));
    }

}
