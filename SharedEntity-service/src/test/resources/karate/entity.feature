# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check SharedEntity Rest Api Response

  Scenario: SharedEntity CRUD Operations

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/entities/shared'
    # ---- Add entity fields here -----
    And request 
    """ { "entityResourceName": "it.water.shared.entity.TestEntityResource","entityId":#(testEntityResourceId),"userId":#(userId)} """
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":1,
        "entityCreateDate":#number,
        "entityModifyDate":#number,
        "entityResourceName": 'it.water.shared.entity.TestEntityResource',
        "entityId":#number,
        "categoryIds": #null,
        "tagIds": #null
       }
    """
    * def entityId = response.id

  # --------------- FIND By PK-----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/entities/shared/findByPK'
    And request
    """ { "entityResourceName": "it.water.shared.entity.TestEntityResource","entityId":#(testEntityResourceId),"userId":#(userId)} """
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
       { "id": #number,
        "entityVersion":1,
        "entityCreateDate":#number,
        "entityModifyDate":#number,
        "entityResourceName": 'it.water.shared.entity.TestEntityResource',
        "entityId":#number,
        "categoryIds": #null,
        "tagIds": #null
       }
    """

  # --------------- FIND By Entity-----------------------------

      Given header Content-Type = 'application/json'
      And header Accept = 'application/json'
      Given url serviceBaseUrl+'/water/entities/shared/findByEntity?entityResourceName=it.water.shared.entity.TestEntityResource&entityId='+entityId
      # ---------------------------------
      When method GET
      Then status 200
      # ---- Matching required response json ----
      And match response ==
      """
         []
      """
    
  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/entities/shared'
    When method GET
    Then status 200
    And match response.results contains
    """
    {
      "id": #number,
      "entityVersion":1,
      "entityCreateDate":#number,
      "entityModifyDate":#number,
      "entityResourceName": 'it.water.shared.entity.TestEntityResource',
      "entityId":#number,
      "categoryIds": #null,
      "tagIds": #null
    }
    """

  # --------------- DELETE BY PK-----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/entities/shared'
    And request
    """ { "entityResourceName": "it.water.shared.entity.TestEntityResource","entityId":#(testEntityResourceId),"userId":#(userId)} """
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
