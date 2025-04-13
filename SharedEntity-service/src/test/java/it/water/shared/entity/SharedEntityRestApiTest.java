
package it.water.shared.entity;

import com.intuit.karate.junit5.Karate;
import it.water.core.api.bundle.Runtime;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.service.Service;
import it.water.core.api.service.integration.UserIntegrationClient;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WaterTestExtension.class)
public class SharedEntityRestApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private UserIntegrationClient userIntegrationClient;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
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
        return Karate.run("classpath:karate")
                .systemProperty("webServerPort", TestRuntimeInitializer.getInstance().getRestServerPort())
                .systemProperty("host", "localhost")
                .systemProperty("protocol", "http")
                .systemProperty("testEntityResourceId", String.valueOf(testEntityResource.getId()))
                .systemProperty("userId", String.valueOf(runtime.getSecurityContext().getLoggedEntityId()));

    }
}
