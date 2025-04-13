package it.water.shared.entity;

import com.intuit.karate.junit5.Karate;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.integration.UserIntegrationClient;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = SharedEntityApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "water.rest.security.jwt.validate=false",
        "water.testMode=true"
})
public class SharedEntityRestSpringApiTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private UserIntegrationClient userIntegrationClient;

    @Autowired
    private Runtime runtime;

    @Autowired
    private TestEntitySystemApi testEntitySystemApi;

    private TestEntityResource testEntityResource;

    @BeforeEach
    void impersonateAdmin() {
        //jwt token service is disabled, we just inject admin user for bypassing permission system
        //just remove this line if you want test with permission system working
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        //Adding a specific test resource in order to make the sharing system work
        testEntityResource = new TestEntityResource();
        testEntityResource.setEntityVersion(1);
        testEntityResource.setId(1);
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(runtime.getSecurityContext().getLoggedEntityId()).getId());
        testEntitySystemApi.save(testEntityResource);
    }

    @Karate.Test
    Karate restInterfaceTest() {
        return Karate.run("../SharedEntity-service/src/test/resources/karate")
                .systemProperty("webServerPort", String.valueOf(serverPort))
                .systemProperty("host", "localhost")
                .systemProperty("protocol", "http")
                .systemProperty("testEntityResourceId", String.valueOf(testEntityResource.getId()))
                .systemProperty("userId", String.valueOf(runtime.getSecurityContext().getLoggedEntityId()));
    }

}
