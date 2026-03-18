package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.entity.Person;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.PersonMapper;
import com.nckh.genealogy.repository.PersonRepository;
import com.nckh.genealogy.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Override
    @Transactional
    public PersonResponse createPerson(CreatePersonRequest request) {
        // Kiểm tra CCCD trùng nếu có
        if (StringUtils.hasText(request.citizenIdentificationNumber())
                && personRepository.existsByCitizenIdentificationNumberAndDeletedAtIsNull(
                request.citizenIdentificationNumber())) {
            throw new AppException(ErrorCode.CITIZEN_ID_ALREADY_EXISTS);
        }

        Person person = personMapper.toEntity(request);
        return personMapper.toResponse(personRepository.save(person));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonResponse getPersonById(UUID id) {
        return personMapper.toResponse(findPersonById(id));
    }

    @Override
    @Transactional
    public PersonResponse updatePerson(UUID id, UpdatePersonRequest request) {
        Person person = findPersonById(id);

        // Kiểm tra CCCD trùng nếu thay đổi
        if (StringUtils.hasText(request.citizenIdentificationNumber())
                && !request.citizenIdentificationNumber()
                .equals(person.getCitizenIdentificationNumber())
                && personRepository.existsByCitizenIdentificationNumberAndDeletedAtIsNull(
                request.citizenIdentificationNumber())) {
            throw new AppException(ErrorCode.CITIZEN_ID_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(request.firstName())) {
            person.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            person.setLastName(request.lastName());
        }
        if (request.gender() != null) {
            person.setGender(request.gender());
        }
        if (request.dateOfBirth() != null) {
            person.setDateOfBirth(request.dateOfBirth());
        }
        if (request.dateOfDeath() != null) {
            person.setDateOfDeath(request.dateOfDeath());
        }
        if (StringUtils.hasText(request.citizenIdentificationNumber())) {
            person.setCitizenIdentificationNumber(request.citizenIdentificationNumber());
        }
        if (StringUtils.hasText(request.avatarUrl())) {
            person.setAvatarUrl(request.avatarUrl());
        }

        return personMapper.toResponse(personRepository.save(person));
    }

    @Override
    @Transactional
    public void deletePerson(UUID id) {
        Person person = findPersonById(id);
        // Soft delete
        person.setDeletedAt(LocalDateTime.now());
        personRepository.save(person);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PersonResponse> searchPersons(String keyword, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        var pageResult = StringUtils.hasText(keyword)
                ? personRepository.searchByName(keyword, pageable)
                : personRepository.findAll(pageable);

        return PageResponse.of(pageResult.map(personMapper::toResponse));
    }

    // ==================== Helper ====================
    private Person findPersonById(UUID id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
    }
}