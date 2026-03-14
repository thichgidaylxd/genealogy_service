package com.nckh.genealogy.mapper;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.request.address.UpdatePersonAddressRequest;
import com.nckh.genealogy.dto.request.address.UpdateTreeAddressRequest;
import com.nckh.genealogy.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequest request);

    @Mapping(target = "id", ignore = true)
    Address toEntity(UpdatePersonAddressRequest request);

    @Mapping(target = "id", ignore = true)
    Address toEntity(UpdateTreeAddressRequest request);
}