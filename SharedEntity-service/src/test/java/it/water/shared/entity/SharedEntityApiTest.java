package it.water.shared.entity;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.Role;
import it.water.core.api.permission.PermissionManager;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.role.RoleManager;
import it.water.core.api.service.Service;
import it.water.core.api.service.integration.SharedEntityIntegrationClient;
import it.water.core.api.service.integration.UserIntegrationClient;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.permission.exceptions.UnauthorizedException;
import it.water.core.testing.utils.bundle.TestRuntimeInitializer;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.repository.entity.model.exceptions.DuplicateEntityException;
import it.water.repository.entity.model.exceptions.EntityNotFound;
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
class SharedEntityApiTest implements Service {

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
    private SharedEntityIntegrationClient sharedEntityIntegrationClient;

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
    void beforeAll() {
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
        testEntityResource.setOwnerUserId(adminUser.getId());
        testEntitySystemApi.save(testEntityResource);
    }

    /**
     * Testing basic injection of basic component for sharedentity entity.
     */
    @Test
    @Order(1)
    void componentsInsantiatedCorrectly() {
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
    void saveOk() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        //creating real entity
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        //sharing it
        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(),sharedEntityEditorUser.getId());
        entity = this.sharedEntityApi.save(entity);
        Assertions.assertEquals(userId,testEntityResource.getOwnerUserId());
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertEquals(TestEntityResource.class.getName(), entity.getEntityResourceName());
        Assertions.assertEquals(sharedEntityEditorUser.getId(), entity.getUserId());
        Assertions.assertEquals(testEntityResource.getId(), entity.getEntityId());
        Assertions.assertTrue(this.sharedEntityIntegrationClient.fetchSharingUsersIds(TestEntityResource.class.getName(), sharedEntityEditorUser.getId()).contains(testEntityResource.getId()));
    }


    /**
     * Testing finding all entries with settings related to pagination.
     * Searching with 5 items per page starting from page 1.
     */
    @Test
    @Order(3)
    void findSharedEntityShouldWork() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        for (int i = 2; i < 11; i++) {
            TestEntityResource testEntityResource = new TestEntityResource();
            testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
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
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.find(0));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.find(null));
    }

    /**
     * Testing removing all entities using findAll method.
     */
    @Test
    @Order(4)
    void removeShouldWork() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
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
        //removing not existing class entity
        WaterSharedEntity entity = new WaterSharedEntity("NotExisting.class.name", (long)1, (long)1);
        long entityId = entity.getId();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.remove(entityId));
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(entity));
        //Simulating a user who tries to share a not owned entity
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(u));
        WaterSharedEntity notExistingUserSharedEntity = createSharedEntity(testEntityResource.getId(), 0);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(notExistingUserSharedEntity));
    }

    /**
     * Testing failure on duplicated entity
     */
    @Test
    @Order(5)
    void saveShouldFailOnDuplicatedEntity() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
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
    void managerCanDoEverything() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityManagerUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //creating with another user
        WaterSharedEntity savedEntity = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(savedEntity.getEntityResourceName(), savedEntity.getEntityId(), runtime.getSecurityContext().getLoggedEntityId()));
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.removeByPK(savedEntity));
    }

    @Order(7)
    @Test
    void viewerCannotSaveOrUpdateOrRemove() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //save permission is not considered since if someone has a share permission it means it can save the record
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        //viewer can search
        WaterSharedEntity found = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findAll(null, -1, -1, null).getResults().stream().findFirst()).get();
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(found.getEntityResourceName(), found.getEntityId(), found.getUserId()));
        //update operation is not supported
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.update(entity));
        //viewer cannot remove
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(found));
    }

    @Order(8)
    @Test
    void editorCannotRemove() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityEditorUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        final WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        //save permission is not considered since if someone has a share permission it means it can save the record
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.save(entity));
        //viewer can search
        WaterSharedEntity found = Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findAll(null, -1, -1, null).getResults().stream().findFirst()).get();
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByPK(found.getEntityResourceName(), found.getEntityId(), found.getUserId()));
        //update operation is not supported
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.sharedEntityApi.update(entity));
        //edittor cannot remove
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(found));
    }

    @Order(9)
    @Test
    void getSharingUsersShouldWork() {
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);

        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        this.sharedEntityApi.save(entity);

        List<Long> sharingUsers = this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), testEntityResource.getId());
        Assertions.assertNotNull(sharingUsers);
        Assertions.assertFalse(sharingUsers.isEmpty());
        Assertions.assertTrue(sharingUsers.contains(userId));
    }

    @Order(10)
    @Test
    void getEntityIdsSharedWithUserShouldWork() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);

        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), sharedEntityViewerUser.getId());
        this.sharedEntityApi.save(entity);

        List<Long> sharedEntityIds = this.sharedEntityApi.getEntityIdsSharedWithUser(TestEntityResource.class.getName(), sharedEntityViewerUser.getId());
        Assertions.assertNotNull(sharedEntityIds);
        Assertions.assertFalse(sharedEntityIds.isEmpty());
        Assertions.assertTrue(sharedEntityIds.contains(testEntityResource.getId()));
    }

    @Test
    @Order(11)
    void saveOkByEmail() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        //creating real entity
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        //sharing it
        WaterSharedEntity entity = createSharedEntityByUserEmail(testEntityResource.getId(), adminUser.getEmail());
        entity = this.sharedEntityApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertEquals(TestEntityResource.class.getName(), entity.getEntityResourceName());
        Assertions.assertEquals(userId, entity.getUserId());
        Assertions.assertEquals(testEntityResource.getId(), entity.getEntityId());
    }

    @Test
    @Order(12)
    void saveOkByUsername() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        //creating real entity
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        //sharing it
        WaterSharedEntity entity = createSharedEntityByUsername(testEntityResource.getId(), adminUser.getUsername());
        entity = this.sharedEntityApi.save(entity);
        Assertions.assertEquals(1, entity.getEntityVersion());
        Assertions.assertEquals(TestEntityResource.class.getName(), entity.getEntityResourceName());
        Assertions.assertEquals(userId, entity.getUserId());
        Assertions.assertEquals(testEntityResource.getId(), entity.getEntityId());
    }

    @Test
    @Order(13)
    void saveKoClassNotFound() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        WaterSharedEntity entity = new WaterSharedEntity("NotExisting.class.name", 1l, 1l);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.save(entity));
    }

    @Test
    @Order(14)
    void saveKoResourceIdNotExists() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        WaterSharedEntity entity = createSharedEntityByUsername(-1, adminUser.getUsername());
        Assertions.assertThrows(EntityNotFound.class, () -> this.sharedEntityApi.save(entity));
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(runtime.getSecurityContext().getLoggedEntityId()).getId());
        testEntitySystemApi.save(testEntityResource);
        WaterSharedEntity existingEntity = createSharedEntityByUsername(testEntityResource.getId(), adminUser.getUsername());
        //Simulating a user who tries to share a not owned entity
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.save(existingEntity));
    }

    @Test
    @Order(15)
    void saveKoUserDoesNotExist() {
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(runtime.getSecurityContext().getLoggedEntityId()).getId());
        testEntitySystemApi.save(testEntityResource);
        WaterSharedEntity existingEntity = createSharedEntity(testEntityResource.getId(), -1);
        Assertions.assertThrows(EntityNotFound.class, () -> this.sharedEntityApi.save(existingEntity));
    }

    // =========================================================
    // #35 — caller-scoping hardening tests
    // =========================================================

    /**
     * #35: findByUser called with own userId succeeds (non-admin caller).
     */
    @Test
    @Order(16)
    void findByUser_asSelf_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityManagerUser, runtime);
        long selfId = runtime.getSecurityContext().getLoggedEntityId();
        // The manager user may have shares; at minimum the call must not throw.
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByUser(selfId));
    }

    /**
     * #35: findByUser called with a DIFFERENT userId by a non-admin must throw UnauthorizedException.
     */
    @Test
    @Order(17)
    void findByUser_asOtherUser_throwsUnauthorized() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        // sharedEntityManagerUser has a different id; viewer must not enumerate manager's shares
        long otherUserId = sharedEntityManagerUser.getId();
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.sharedEntityApi.findByUser(otherUserId));
    }

    /**
     * #35: findByUser called by admin with any userId succeeds.
     */
    @Test
    @Order(18)
    void findByUser_asAdmin_withAnyUserId_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long anyUserId = sharedEntityViewerUser.getId();
        Assertions.assertDoesNotThrow(() -> this.sharedEntityApi.findByUser(anyUserId));
    }

    /**
     * #35: getEntityIdsSharedWithUser called with own userId succeeds (non-admin caller).
     */
    @Test
    @Order(19)
    void getEntityIdsSharedWithUser_asSelf_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        long selfId = runtime.getSecurityContext().getLoggedEntityId();
        Assertions.assertDoesNotThrow(() ->
                this.sharedEntityApi.getEntityIdsSharedWithUser(TestEntityResource.class.getName(), selfId));
    }

    /**
     * #35: getEntityIdsSharedWithUser called with a DIFFERENT userId by a non-admin must throw.
     */
    @Test
    @Order(20)
    void getEntityIdsSharedWithUser_asOtherUser_throwsUnauthorized() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        long otherUserId = sharedEntityManagerUser.getId();
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.sharedEntityApi.getEntityIdsSharedWithUser(TestEntityResource.class.getName(), otherUserId));
    }

    /**
     * #35: getSharingUsers called by the actual owner (non-admin) of the entity succeeds.
     * The manager user creates and owns an entity, then queries its sharing graph.
     */
    @Test
    @Order(21)
    void getSharingUsers_asNonAdminOwner_succeeds() {
        // Impersonate manager user (non-admin) — they will become the owner
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityManagerUser, runtime);
        long managerId = runtime.getSecurityContext().getLoggedEntityId();
        // Create entity owned by manager
        TestEntityResource resource = new TestEntityResource();
        resource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(managerId).getId());
        testEntitySystemApi.save(resource);
        // Share with viewer
        WaterSharedEntity share = createSharedEntity(resource.getId(), sharedEntityViewerUser.getId());
        this.sharedEntityApi.save(share);
        // Manager owns the entity — getSharingUsers must succeed (non-admin owner path)
        List<Long> sharingUsers = Assertions.assertDoesNotThrow(() ->
                this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), resource.getId()));
        Assertions.assertNotNull(sharingUsers);
        Assertions.assertTrue(sharingUsers.contains(sharedEntityViewerUser.getId()));
    }

    /**
     * #35: getSharingUsers called by a user who does NOT own the entity must throw UnauthorizedException.
     */
    @Test
    @Order(22)
    void getSharingUsers_asNonOwner_throwsUnauthorized() {
        // First create entity as admin and share with viewer
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long adminId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource resource = new TestEntityResource();
        resource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(adminId).getId());
        testEntitySystemApi.save(resource);
        WaterSharedEntity share = createSharedEntity(resource.getId(), sharedEntityViewerUser.getId());
        this.sharedEntityApi.save(share);

        // Now impersonate viewer — they are NOT the owner, must be rejected
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        long entityId = resource.getId();
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), entityId));
    }

    /**
     * #35: getSharingUsers called by admin on any entity succeeds (admin bypass).
     */
    @Test
    @Order(23)
    void getSharingUsers_asAdmin_bypass_succeeds() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource resource = new TestEntityResource();
        resource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(resource);
        // Admin can query getSharingUsers even on entities with no shares
        Assertions.assertDoesNotThrow(() ->
                this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), resource.getId()));
    }

    /**
     * #35: getSharingUsers with an unknown resource class name (not a SharedEntity) must throw UnauthorizedException
     * because the entity class cannot be resolved.
     */
    @Test
    @Order(24)
    void getSharingUsers_withUnknownResourceClass_throwsUnauthorized() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.sharedEntityApi.getSharingUsers("com.unknown.NonExistentClass", 1L));
    }


    // =========================================================
    // #35 hardening — additional branch coverage (null security context,
    // non-SharedEntity classes, missing referenced entities)
    // =========================================================

    /**
     * checkCallerCanQueryUser: securityContext == null -> UnauthorizedException.
     * Exercised through the public findByUser entry point.
     */
    @Test
    @Order(25)
    void findByUser_nullSecurityContext_throwsUnauthorized() {
        runtime.fillSecurityContext(null);
        try {
            Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.findByUser(1L));
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * checkCallerOwnsReferencedEntity: securityContext == null -> UnauthorizedException.
     * Exercised through the public getSharingUsers entry point.
     */
    @Test
    @Order(26)
    void getSharingUsers_nullSecurityContext_throwsUnauthorized() {
        runtime.fillSecurityContext(null);
        try {
            Assertions.assertThrows(UnauthorizedException.class,
                    () -> this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), 1L));
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * checkCallerOwnsReferencedEntity: entityClass resolved but does NOT implement SharedEntity
     * -> UnauthorizedException. Distinct from the "class not found" branch already covered by
     * getSharingUsers_withUnknownResourceClass_throwsUnauthorized.
     */
    @Test
    @Order(27)
    void getSharingUsers_withNonSharedEntityClass_throwsUnauthorized() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        Assertions.assertThrows(UnauthorizedException.class,
                () -> this.sharedEntityApi.getSharingUsers(String.class.getName(), 1L));
    }

    /**
     * checkCallerOwnsReferencedEntity: referenced entity id does not exist -> EntityNotFound
     * (NoResultException from the entity system service is translated).
     */
    @Test
    @Order(28)
    void getSharingUsers_withNonExistentEntityId_throwsEntityNotFound() {
        TestRuntimeInitializer.getInstance().impersonate(sharedEntityViewerUser, runtime);
        Assertions.assertThrows(EntityNotFound.class,
                () -> this.sharedEntityApi.getSharingUsers(TestEntityResource.class.getName(), 987654321L));
    }

    /**
     * setSharedEntityUserId: entity.getUserId() > 0 and the user exists -> the else-branch
     * resolves the user via fetchUserByUserId and the share is persisted successfully.
     * (The NoResultException -> WaterRuntimeException sub-path is not reachable with the
     * in-memory test user client, which returns null for missing ids instead of throwing.)
     */
    @Test
    @Order(29)
    void saveOkTargetUserIdResolvedViaElseBranch() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(userIntegrationClient.fetchUserByUserId(userId).getId());
        testEntitySystemApi.save(testEntityResource);
        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), userId);
        WaterSharedEntity saved = this.sharedEntityApi.save(entity);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals(userId, saved.getUserId());
    }

    /**
     * save(): entityClass IS resolved but does NOT implement SharedEntity -> UnauthorizedException.
     * Distinct from the "class not found" branch already covered by saveKoClassNotFound.
     */
    @Test
    @Order(30)
    void saveKoEntityClassNotSharedEntity() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        WaterSharedEntity entity = new WaterSharedEntity(String.class.getName(), 1L, 1L);
        entity.setUserId(1L);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.save(entity));
    }

    /**
     * save(): non-admin caller with NO role at all (hence no SHARE permission) -> UnauthorizedException.
     * Distinct from the ownership-mismatch branch already covered by saveKoResourceIdNotExists.
     */
    @Test
    @Order(31)
    void saveKoUserWithoutSharePermission() {
        it.water.core.api.model.User noRoleUser = userManager.addUser("norole", "name", "lastname", "norole@a.com", "Password1_", "salt", false);
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        TestEntityResource testEntityResource = new TestEntityResource();
        testEntityResource.setOwnerUserId(adminUser.getId());
        testEntitySystemApi.save(testEntityResource);
        TestRuntimeInitializer.getInstance().impersonate(noRoleUser, runtime);
        WaterSharedEntity entity = createSharedEntity(testEntityResource.getId(), noRoleUser.getId());
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.save(entity));
    }

    /**
     * removeByPK(): non-admin caller with NO role at all (hence no SHARE permission) -> UnauthorizedException.
     * Distinct from the ownership-mismatch branch already covered by viewerCannotSaveOrUpdateOrRemove/editorCannotRemove.
     */
    @Test
    @Order(32)
    void removeByPKKoUserWithoutSharePermission() {
        it.water.core.api.model.User noRoleUser2 = userManager.addUser("norole2", "name", "lastname", "norole2@a.com", "Password1_", "salt", false);
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        TestEntityResource resource = new TestEntityResource();
        resource.setOwnerUserId(adminUser.getId());
        testEntitySystemApi.save(resource);
        WaterSharedEntity shared = createSharedEntity(resource.getId(), adminUser.getId());
        this.sharedEntityApi.save(shared);
        TestRuntimeInitializer.getInstance().impersonate(noRoleUser2, runtime);
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(shared));
    }

    /**
     * removeByPK(): the referenced entity id does not exist -> EntityNotFound
     * (NoResultException from the entity system service is translated).
     */
    @Test
    @Order(33)
    void removeByPKKoEntityIdNotFound() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        WaterSharedEntity entity = createSharedEntity(999999997L, adminUser.getId());
        Assertions.assertThrows(EntityNotFound.class, () -> this.sharedEntityApi.removeByPK(entity));
    }

    /**
     * removeByPK(): referenced entity has a null ownerUserId (defaults to 0L) which does not match
     * the logged-in user -> UnauthorizedException. Covers the null-ternary branch.
     */
    @Test
    @Order(34)
    void removeByPKKoOwnerUserIdNull() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        TestEntityResource resource = new TestEntityResource();
        // intentionally NOT setting ownerUserId -> stays null
        testEntitySystemApi.save(resource);
        WaterSharedEntity entity = createSharedEntity(resource.getId(), adminUser.getId());
        Assertions.assertThrows(UnauthorizedException.class, () -> this.sharedEntityApi.removeByPK(entity));
    }

    /**
     * removeByPK(): caller owns the referenced entity, but no WaterSharedEntity association exists
     * for the given primary key -> EntityNotFound (the "entity == null" else-branch).
     */
    @Test
    @Order(35)
    void removeByPKKoSharedEntityNotFound() {
        TestRuntimeInitializer.getInstance().impersonate(adminUser, runtime);
        long userId = runtime.getSecurityContext().getLoggedEntityId();
        TestEntityResource resource = new TestEntityResource();
        resource.setOwnerUserId(userId);
        testEntitySystemApi.save(resource);
        // NOT calling sharedEntityApi.save(...) so no association is persisted
        WaterSharedEntity entity = createSharedEntity(resource.getId(), userId);
        Assertions.assertThrows(EntityNotFound.class, () -> this.sharedEntityApi.removeByPK(entity));
    }

    // =========================================================
    // Repository layer — direct coverage of "not found" branches
    // =========================================================

    @Test
    @Order(36)
    void repositoryFindByPK_notFound_returnsNull() {
        WaterSharedEntity result = this.sharedEntityRepository.findByPK("not.existing.Class", 123456789L, 123456789L);
        Assertions.assertNull(result);
    }

    @Test
    @Order(37)
    void repositoryFindByEntity_notFound_returnsEmptyList() {
        List<WaterSharedEntity> result = this.sharedEntityRepository.findByEntity("not.existing.Class", 123456789L);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(38)
    void repositoryFindByUser_notFound_returnsEmptyList() {
        List<WaterSharedEntity> result = this.sharedEntityRepository.findByUser(-123456789L);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @Order(39)
    void repositoryRemoveByPK_notFound_doesNothing() {
        Assertions.assertDoesNotThrow(() -> this.sharedEntityRepository.removeByPK("not.existing.Class", 123456789L, 123456789L));
    }

    private WaterSharedEntity createSharedEntity(long entityId, long userId) {
        WaterSharedEntity entity = new WaterSharedEntity(TestEntityResource.class.getName(), entityId, userId);
        entity.setUserId(userId);
        return entity;
    }

    private WaterSharedEntity createSharedEntityByUserEmail(long entityId, String email) {
        WaterSharedEntity entity = new WaterSharedEntity(TestEntityResource.class.getName(), entityId, 0l);
        entity.setUserEmail(email);
        return entity;
    }

    private WaterSharedEntity createSharedEntityByUsername(long entityId, String username) {
        WaterSharedEntity entity = new WaterSharedEntity(TestEntityResource.class.getName(), entityId, 0l);
        entity.setUsername(username);
        return entity;
    }

}
