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

function fn() {
    let testEntityResourceId = karate.properties['testEntityResourceId'];
    let userId = karate.properties['userId'];
    let webServerPort = karate.properties['webServerPort'];
    let host = karate.properties['host'];
    let protocol = karate.properties['protocol'];
    let serviceBaseUrl = protocol+"://"+host+":"+webServerPort;
    return {
        "testEntityResourceId": testEntityResourceId,
        "userId": userId,
        "serviceBaseUrl": serviceBaseUrl
    }
}