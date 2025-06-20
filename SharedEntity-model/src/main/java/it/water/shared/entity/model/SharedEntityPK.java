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

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Access(AccessType.FIELD)
@ToString
@EqualsAndHashCode(of = {"entityResourceName", "entityId", "userId"})
public class SharedEntityPK implements Serializable {
    @NonNull
    private String entityResourceName;
    @NonNull
    private Long entityId;
    @NonNull
    private Long userId;
}
