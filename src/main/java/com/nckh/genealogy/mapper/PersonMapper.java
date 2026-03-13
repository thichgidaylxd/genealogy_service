package com.nckh.genealogy.mapper;

import com.nckh.genealogy.dto.request.person.CreatePersonRequest;
import com.nckh.genealogy.dto.response.person.PersonResponse;
import com.nckh.genealogy.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "fullName", expression = "java(person.getLastName() + \" \" + person.getFirstName())")
    PersonResponse toResponse(Person person);

    @Mapping(target = "deletedAt", ignore = true)
    Person toEntity(CreatePersonRequest request);
}