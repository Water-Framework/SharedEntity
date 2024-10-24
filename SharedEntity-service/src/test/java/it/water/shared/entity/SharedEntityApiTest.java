package it.water.shared.entity;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.Role;
import it.water.core.api.permission.PermissionManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.service.integration.UserIntegrationClient;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.shared.entity.api.SharedEntityApi;
import it.water.shared.entity.api.SharedEntityRepository;
import it.water.shared.entity.api.SharedEntitySystemApi;
import it.water.shared.entity.model.WaterSharedEntity;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/**
 * Generated with Water Generator.
 * Test class for SharedEntity Services.
 * <p>
 * Please use SharedEntityRestTestApi for ensuring format of the json response
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SharedEntityApiTest implements Service {

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private SharedEntityApi sharedEntityApi;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private SharedEntityRepository sharedEntityRepository;

    @Inject
    @Setter
    //default permission manager in test environment;
    private PermissionManager permissionManager;

    @Inject
    @Setter
    //default permission manager in test environment;
    private UserManager userManager;

    @Inject
    @Setter
    private UserIntegrationClient userIntegrationClient;

    @Inject
    @Setter
    //test role manager
    private RoleManager roleManager;

    @Inject
    @Setter
    private TestEntitySystemApi testEntitySystemApi;

    //admin user
    private it.water.core.api.model.User adminUser;
    private it.water.core.api.model.User sharedEntityManagerUser;
    private it.water.core.api.model.User sharedEntityViewerUser;
    private it.water.core.api.model.User sharedEntityEditorUser;

    private Role sharedEntityManagerRole;
    private Role sharedEntityViewerRole;
    private Role sharedEntityEditorRole;

    @BeforeAll
    public void beforeAll() {
        //getting user
        sharedEntityManagerRole = roleManager.getRole(WaterSharedEntity.DEFAULT_MANAGER_ROLE);
        sharedEntityViewerRole = roleManager.getRole(WaterSharedEntity.DEFAULT_VIEWER_ROLE);
        sharedEntityEditorRole = roleManager.getRole(WaterSharedEntity.DEFAULT_EDITOR_ROLE);
        Assertions.assertNotNull(sharedEntityManagerRole);
        Assertions.assertNotNull(sharedEntityViewerRole);
        Assertions.assertNotNull(sharedEntityEditorRole);
        //impersonate admin so we can test the happy path
        adminUser = userManager.findUser("admin");
        sharedEntityManagerUser = userManager.addUser("manager", "name", "lastname", "manager@a.com", "Password1_", "salt", false);
        sharedEntityViewerUser = userManager.addUser("viewer", "name", "lastname", "viewer@a.com", "Password1_", "salt", false);
        sharedEntityEditorUser = userManager.addUser("editor", "name", "lastname", "editor@a.com", "Password1_", "salt", false);
        //starting with admin permissions
        roleManager.addRole(sharedEntityManagerUser.getId(), sharedEntityManagerRole);
        roleManager.addRole(sharedEntityViewerUser.getId(), sharedEntityViewerRole);
        roleManager.addRole(sharedEntityEditorUser.getId(), sharedEntityEditorRole);
        //default security context is admin
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        //adding default resource
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(adminUser);
        testEntitySystemApi.save(testEntityResource);
    }

    /**
     * Testing basic injection of basic component for sharedentity entity.
     */
    @Test
    @Order(1)
    public void componentsInsantiatedCorrectly() {
        this.sharedEntityApi = this.componentRegistry.findComponent(SharedEntityApi.class, null);
        Assertions.assertNotNull(this.sharedEntityApi);
        Assertions.assertNotNull(this.componentRegistry.findComponent(SharedEntitySystemApi.class, null));
        this.sharedEntityRepository = this.componentRegistry.findComponent(SharedEntityRepository.class, null);
        Assertions.assertNotNull(this.sharedEntityRepository);
    }

    /**
     * Testing simple save and version increment
     */
    @Test
    @Order(2)
    public void saveOk() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        //creating real entity
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        //sharing it
        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        entity = this.sharedEntityApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertEquals(TestEntityResource.class.getName(), entity.getEntityResourceName());
        Assertions.assertEquals(userId, entity.getUserId());
        Assertions.assertEquals(testEntityResource.getId(), entity.getEntityId());
    }


    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(3)
    public void findSharedEntityShouldWork() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        for (int i = 2; i < 11; i++) {
            TestEntityResource testEntityResource = new TestEntityResource();
            testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
            testEntitySystemApi.save(testEntityResource);
            WaterSharedEntity u = createSharedEntity(testEntityResource.getId(), userId);
            this.sharedEntityApi.save(u);
        }
        //admin has all shared entities
        List<WaterSharedEntity> result = this.sharedEntityApi.findByUser(userId);
        //At least entities created in this test
        Assertions.assertTrue(result.size() >= 9);
        //each entity is shared only one time
        List<WaterSharedEntity> sharedEntitiesList = this.sharedEntityApi.findByEntity(TestEntityResource.class.getName(), 7);
        Assertions.assertEquals(1, sharedEntitiesList.size());
        WaterSharedEntity waterSharedEntity = this.sharedEntityApi.findByPK(TestEntityResource.class.getName(), 7, userId);
        Assertions.assertNotNull(waterSharedEntity);
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(4)
    public void removeShouldWork() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        WaterSharedEntity u = createSharedEntity(testEntityResource.getId(), userId);
        this.sharedEntityApi.save(u);
        List<WaterSharedEntity> result = this.sharedEntityApi.findByUser(userId);
        long count = result.size();
        WaterSharedEntity waterSharedEntity = this.sharedEntityApi.findByPK(TestEntityResource.class.getName(), testEntityResource.getId(), userId);
        Assertions.assertNotNull(waterSharedEntity);
        this.sharedEntityApi.removeByPK(waterSharedEntity);
        result = this.sharedEntityApi.findByUser(userId);
        Assertions.assertEquals(count - 1, result.size());
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(5)
    public void saveShouldFailOnDuplicatedEntity() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        this.sharedEntityApi.save(entity);
        WaterSharedEntity duplicated = this.createSharedEntity(testEntityResource.getId(), userId);
        //forcing id to be the different
        duplicated.setId(100);
        //cannot insert new entity wich breaks unique constraint
        Assertions.assertThrows(DuplicateEntityException.class, () -> this.sharedEntityApi.save(duplicated));
    }

    /**
     * Testing Crud operations on manager role
     */
    @Order(6)
    @Test
    public void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityManagerUser,runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //creating with another user
        WaterSharedEntity savedEntity = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(savedEntity.getEntityResourceName(),savedEntity.getEntityId(),runtime.getSecurityContext().getLoggedEntityId()));
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.removeByPK(savedEntity));
    }

    @Order(7)
    @Test
    public void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //save permission is not considered since if someone has a share permission it means it can save the record
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        //viewer can search
        WaterSharedEntity found = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findAll(null, -1, -1, null).getResults().stream().findFirst()).get();
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(found.getEntityResourceName(),found.getEntityId(),found.getUserId()));
        //update operation is not supported
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.update(entity));
        //viewer cannot remove
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(found));
    }

    @Order(8)
    @Test
    public void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityEditorUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setUserOwner(userIntegrationClient.fetchUserByUserId(userId));
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //save permission is not considered since if someone has a share permission it means it can save the record
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        //viewer can search
        WaterSharedEntity found = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findAll(null, -1, -1, null).getResults().stream().findFirst()).get();
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(found.getEntityResourceName(),found.getEntityId(),found.getUserId()));
        //update operation is not supported
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.update(entity));
        //edittor cannot remove
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(found));
    }

    //TODO: adding more tests to check resource sharing with specific ids or resource class name

    private WaterSharedEntity createSharedEntity(long entityId, long userId) {
        WaterSharedEntity entity = new WaterSharedEntity(TestEntityResource.class.getName(), entityId, userId);
        entity.setUserId(userId);
        return entity;
    }
}