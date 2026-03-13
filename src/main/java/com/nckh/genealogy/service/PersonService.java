package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;

import java.util.UUID;

public interface PersonService {
    PersonResponse createPerson(CreatePersonRequest request);
    PersonResponse getPersonById(UUID id);
    PersonResponse updatePerson(UUID id, UpdatePersonRequest request);
    void deletePerson(UUID id);
    PageResponse<PersonResponse> searchPersons(String keyword, int page, int size);
}