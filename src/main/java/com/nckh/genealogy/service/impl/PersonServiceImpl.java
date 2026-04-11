package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.request.person.UpdatePersonRequest;
import com.nckh.genealogy.dto.response.PageResponse;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.PersonMapper;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.CloudinaryService;
import com.nckh.genealogy.service.MediaFileService;
import com.nckh.genealogy.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final TreeRepository treeRepository;
    private final FamilyRepository familyRepository;
    private final TreePersonRepository treePersonRepository;
    private final FamilyChildRepository familyChildRepository;
    private final CloudinaryService  cloudinaryService;
    private final MediaFileService  mediaFileService;

    private final EventRepository eventRepository;
    private final EventTypeRepository eventTypeRepository;
    private final PersonEventRepository personEventRepository;

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

        Person saved = personRepository.save(person);
        syncLifeEvents(saved);
        return personMapper.toResponse(saved);
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

        Person saved = personRepository.save(person);
        syncLifeEvents(saved);
        return personMapper.toResponse(saved);
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

    @Override
    public PersonResponse uploadAvatar(UUID personId, MultipartFile file) {
        Person person = personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        String[] uploaded = cloudinaryService.upload(file, "genealogy/persons/" + personId);

        person.setAvatarUrl(uploaded[0]);
        return personMapper.toResponse(personRepository.save(person));
    }

    // ==================== Helper ====================
    private Person findPersonById(UUID id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
    }



    private void syncLifeEvents(Person person) {
        EventType birthType = eventTypeRepository.findByName("BIRTH").orElse(null);
        EventType deathType = eventTypeRepository.findByName("DEATH").orElse(null);

        // BIRTH
        if (person.getDateOfBirth() != null && birthType != null) {
            boolean exists = personEventRepository.existsByPersonIdAndEventTypeId(
                    person.getId(), birthType.getId());
            if (!exists) {
                Event event = Event.builder()
                        .createdBy(null) // system-generated, hoặc truyền vào nếu cần
                        .name("Ngày sinh của " + person.getLastName() + " " + person.getFirstName())
                        .description("Sự kiện sinh tự động")
                        .startedAt(person.getDateOfBirth())
                        .endedAt(person.getDateOfBirth())
                        .status((short) 1)
                        .build();
                eventRepository.save(event);

                PersonEvent pe = PersonEvent.builder()
                        .person(person)
                        .event(event)
                        .eventType(birthType)
                        .name("Sinh")
                        .build();
                personEventRepository.save(pe);
            }
        }

        // DEATH
        if (person.getDateOfDeath() != null && deathType != null) {
            boolean exists = personEventRepository.existsByPersonIdAndEventTypeId(
                    person.getId(), deathType.getId());
            if (!exists) {
                Event event = Event.builder()
                        .createdBy(null)
                        .name("Ngày mất của " + person.getLastName() + " " + person.getFirstName())
                        .description("Sự kiện mất tự động")
                        .startedAt(person.getDateOfDeath())
                        .endedAt(person.getDateOfDeath())
                        .status((short) 1)
                        .build();
                eventRepository.save(event);

                PersonEvent pe = PersonEvent.builder()
                        .person(person)
                        .event(event)
                        .eventType(deathType)
                        .name("Mất")
                        .build();
                personEventRepository.save(pe);
            }
        }
    }
}