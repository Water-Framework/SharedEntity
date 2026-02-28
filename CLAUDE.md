# SharedEntity Module — Cross-User Resource Sharing

## Purpose
Enables controlled sharing of `OwnedResource` entities between users. When user A owns an entity, they can grant access to user B by creating a `WaterSharedEntity` record. The `SharedEntity` module enforces that: (1) the caller has the SHARE action permission, (2) the caller owns the entity being shared, and (3) the target entity class implements the `SharedEntity` interface.

## Sub-modules

| Sub-module | Runtime | Key Classes |
|---|---|---|
| `SharedEntity-api` | All | `SharedEntityApi`, `SharedEntitySystemApi`, `SharedEntityRestApi`, `SharedEntityRepository` |
| `SharedEntity-model` | All | `WaterSharedEntity`, `SharedEntityPK` (composite key) |
| `SharedEntity-service` | Water/OSGi | Service impl, repository, REST controller |
| `SharedEntity-service-spring` | Spring Boot | Spring MVC REST controllers |
| `SharedEntity-service-integration` | All | `SharedEntityLocalIntegrationClient` |

## WaterSharedEntity Entity

```java
@Entity
@Table(name = "water_shared_entity",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"entityResourceName", "entityId", "userId"}))
@IdClass(SharedEntityPK.class)
public class WaterSharedEntity extends AbstractJpaEntity implements ProtectedEntity {

    @Id @NotNull @NoMalitiusCode
    private String entityResourceName;    // fully-qualified class name of the shared entity

    @Id @NotNull
    private long entityId;                // ID of the specific entity instance being shared

    @Id @NotNull
    private long userId;                  // user being granted access
}
```

## SharedEntityPK (Composite Primary Key)

```java
public class SharedEntityPK implements Serializable {
    private String entityResourceName;
    private long entityId;
    private long userId;
    // equals() + hashCode() required
}
```

## Disabled CRUD Operations
Standard single-ID operations are **intentionally disabled** in `SharedEntityApi`:
- `update(WaterSharedEntity)` → throws `UnsupportedOperationException`
- `remove(long id)` → throws `UnsupportedOperationException`
- `find(long id)` → throws `UnsupportedOperationException`

Use composite-key methods instead:
```java
void remove(String entityClass, long entityId, long userId);
WaterSharedEntity find(String entityClass, long entityId, long userId);
List<WaterSharedEntity> findByEntityAndId(String entityClass, long entityId);
List<WaterSharedEntity> findByUser(long userId);
```

## Sharing Rules (enforced by SharedEntityServiceImpl)

1. **Target class must implement `SharedEntity`**: The entity being shared must implement the `SharedEntity` marker interface
2. **Caller must have SHARE permission**: `@AllowPermissions(actions = {"SHARE"})` on `save()`
3. **Caller must own the entity**: `ownerUserId == currentUserId` is verified before creating the share
4. **User resolution**: `userId`, `userEmail`, or `username` can be used to identify the target user

## Sharing Flow

```
SharedEntityApi.save(sharedEntity)
  │
  ├─ Check: entityResourceName class implements SharedEntity? → else error
  ├─ Resolve userId from userEmail/username if not provided
  ├─ Check: caller owns entityId? (via OwnedResource.ownerUserId) → else UnauthorizedException
  ├─ Check: caller has SHARE permission? → else UnauthorizedException
  │
  └─ Persist WaterSharedEntity record
```

## Permission Integration
Other modules' service implementations check for shared access:

```java
// In CompanyServiceImpl.find(long id)
@Override
public Company find(long id) {
    Company company = repository.find(id);
    long currentUser = runtime.getSecurityContext().getLoggedEntityId();

    // Access granted if: owner OR shared
    if (company.getOwnerUserId() == currentUser
        || sharedEntityClient.isSharedWith(Company.class.getName(), id, currentUser)) {
        return company;
    }
    throw new UnauthorizedException();
}
```

## SharedEntityLocalIntegrationClient
In-process client for checking sharing relationships without REST overhead. Used when the SharedEntity module runs in the same JVM:

```java
public interface SharedEntityIntegrationClient {
    boolean isSharedWith(String entityClass, long entityId, long userId);
    List<Long> getSharedUserIds(String entityClass, long entityId);
}
```

## REST Endpoints

| Method | Path | Notes |
|---|---|---|
| `POST` | `/water/sharedentities` | Create sharing — requires SHARE permission + ownership |
| `GET` | `/water/sharedentities` | Find all shared entities for current user |
| `DELETE` | `/water/sharedentities/{entityClass}/{entityId}/{userId}` | Revoke sharing |
| `GET` | `/water/sharedentities/{entityClass}/{entityId}` | Find all users with access to entity |

## Default Roles

| Role | Allowed Actions |
|---|---|
| `sharedentityManager` | SAVE, FIND, FIND_ALL, REMOVE |
| `sharedentityViewer` | FIND, FIND_ALL |
| `sharedentityEditor` | UPDATE, FIND, FIND_ALL |

## Dependencies
- `it.water.repository.jpa:JpaRepository-api` — `AbstractJpaEntity`
- `it.water.core:Core-permission` — `@AccessControl`, SHARE action
- `it.water.user:User-api` — resolve userId from email/username
- `it.water.rest:Rest-persistence` — `BaseEntityRestApi`

## Making an Entity Shareable

```java
// 1. Entity must implement both OwnedResource and SharedEntity
public class MyEntity extends AbstractJpaEntity
    implements ProtectedEntity, OwnedResource, SharedEntity {
    private long ownerUserId;
}

// 2. Add SHARE action to @AccessControl
@AccessControl(
    availableActions = {CrudActions.class, SharedEntityActions.class},
    rolesPermissions = {
        @DefaultRoleAccess(roleName = "myEntityManager",
                           actions = {CrudActions.class, SharedEntityActions.class})
    }
)

// 3. In service: check sharedEntityClient.isSharedWith() in find() + findAll()
```

## Testing
- Unit tests: `WaterTestExtension` — test full sharing lifecycle
  1. Create entity as user A → owns it
  2. Create `WaterSharedEntity` → grants access to user B
  3. Impersonate user B → `find()` on entity succeeds
  4. Delete share → user B can no longer access
- REST tests: **Karate only**

## Code Generation Rules
- `WaterSharedEntity` uses `@IdClass` composite key — never query by single `id`
- Target entities must implement BOTH `OwnedResource` AND `SharedEntity` — sharing without ownership makes no sense
- `SharedEntityLocalIntegrationClient` vs REST client: use local for same-JVM, REST client for distributed deployments
- `SharedEntityRestController` tested **exclusively via Karate**
