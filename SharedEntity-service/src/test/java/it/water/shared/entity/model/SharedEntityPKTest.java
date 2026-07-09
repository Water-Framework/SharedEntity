/*
 * Copyright 2024 Aristide Cittadino
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.water.shared.entity.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Pure unit test for {@link SharedEntityPK}.
 * <p>
 * SharedEntity-model has no dedicated src/test/java source set, so this class lives
 * alongside the other SharedEntity-service tests (same convention used for the rest
 * of this module's coverage gaps) purely to make sure the composite primary key class
 * is exercised (constructor null-checks, equals/hashCode/toString) for coverage purposes.
 * No Water DI/runtime is needed since SharedEntityPK is a plain POJO.
 */
class SharedEntityPKTest {

    private static final String RESOURCE_NAME = "it.water.shared.entity.TestEntityResource";

    @Test
    void equals_sameInstance_returnsTrue() {
        SharedEntityPK pk = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        Assertions.assertEquals(pk, pk);
    }

    @Test
    void equals_sameValues_returnsTrueAndHashCodeMatches() {
        SharedEntityPK pk1 = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        SharedEntityPK pk2 = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        Assertions.assertEquals(pk1, pk2);
        Assertions.assertEquals(pk1.hashCode(), pk2.hashCode());
    }

    @Test
    void equals_differentEntityId_returnsFalse() {
        SharedEntityPK pk1 = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        SharedEntityPK pk2 = new SharedEntityPK(RESOURCE_NAME, 999L, 2L);
        Assertions.assertNotEquals(pk1, pk2);
    }

    @Test
    void equals_differentUserId_returnsFalse() {
        SharedEntityPK pk1 = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        SharedEntityPK pk2 = new SharedEntityPK(RESOURCE_NAME, 1L, 999L);
        Assertions.assertNotEquals(pk1, pk2);
    }

    @Test
    void equals_differentEntityResourceName_returnsFalse() {
        SharedEntityPK pk1 = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        SharedEntityPK pk2 = new SharedEntityPK("other.Class", 1L, 2L);
        Assertions.assertNotEquals(pk1, pk2);
    }

    @Test
    void equals_null_returnsFalse() {
        SharedEntityPK pk = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        Assertions.assertNotEquals(null, pk);
    }

    @Test
    void equals_differentClass_returnsFalse() {
        SharedEntityPK pk = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        Assertions.assertNotEquals("aString", pk);
    }

    @Test
    void constructor_withNullEntityResourceName_throwsNpe() {
        Assertions.assertThrows(NullPointerException.class, () -> new SharedEntityPK(null, 1L, 2L));
    }

    @Test
    void constructor_withNullEntityId_throwsNpe() {
        Assertions.assertThrows(NullPointerException.class, () -> new SharedEntityPK(RESOURCE_NAME, null, 2L));
    }

    @Test
    void constructor_withNullUserId_throwsNpe() {
        Assertions.assertThrows(NullPointerException.class, () -> new SharedEntityPK(RESOURCE_NAME, 1L, null));
    }

    @Test
    void toString_containsFieldValues() {
        SharedEntityPK pk = new SharedEntityPK(RESOURCE_NAME, 1L, 2L);
        String str = pk.toString();
        Assertions.assertNotNull(str);
        Assertions.assertTrue(str.contains(RESOURCE_NAME));
    }
}
