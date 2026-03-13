package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressTypeRepository addressTypeRepository;
    private final PersonAddressRepository personAddressRepository;
    private final TreeAddressRepository treeAddressRepository;
    private final PersonRepository personRepository;
    private final TreeRepository treeRepository;
    private final TreeMemberRepository treeMemberRepository;

    // ==================== Person Address ====================

    @Override
    @Transactional
    public AddressResponse addPersonAddress(UUID personId, UUID requesterId, AddressRequest request) {
        Person person = personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        AddressType addressType = findAddressType(request.addressTypeId());
        Address address = saveAddress(request);

        Short isPrimary = Boolean.TRUE.equals(request.isPrimary()) ? (short) 1 : (short) 0;

        // Nếu set primary → reset các địa chỉ primary khác
        if (isPrimary == 1) {
            resetPrimaryPersonAddresses(personId);
        }

        PersonAddress personAddress = PersonAddress.builder()
                .id(new PersonAddressId(personId, address.getId()))
                .person(person)
                .address(address)
                .addressType(addressType)
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .isPrimary(isPrimary)
                .description(request.description())
                .build();

        personAddressRepository.save(personAddress);
        return buildAddressResponse(address, addressType, request.fromDate(), request.toDate(), isPrimary == 1, request.description());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getPersonAddresses(UUID personId) {
        personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
        return personAddressRepository.findByPersonId(personId)
                .stream()
                .map(pa -> buildAddressResponse(
                        pa.getAddress(),
                        pa.getAddressType(),
                        pa.getFromDate(),
                        pa.getToDate(),
                        pa.getIsPrimary() == 1,
                        pa.getDescription()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void removePersonAddress(UUID personId, UUID addressId, UUID requesterId) {
        PersonAddressId id = new PersonAddressId(personId, addressId);
        if (!personAddressRepository.existsById(id)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        personAddressRepository.deleteById(id);
    }

    // ==================== Tree Address ====================

    @Override
    @Transactional
    public AddressResponse addTreeAddress(UUID treeId, UUID requesterId, AddressRequest request) {
        requireTreeMember(requesterId, treeId);

        treeRepository.findById(treeId)
                .orElseThrow(() -> new AppException(ErrorCode.TREE_NOT_FOUND));

        AddressType addressType = findAddressType(request.addressTypeId());
        Address address = saveAddress(request);

        TreeAddress treeAddress = TreeAddress.builder()
                .tree(treeRepository.getReferenceById(treeId))
                .address(address)
                .addressType(addressType)
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .description(request.description())
                .build();

        treeAddressRepository.save(treeAddress);
        return buildAddressResponse(address, addressType, request.fromDate(), request.toDate(), false, request.description());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getTreeAddresses(UUID treeId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        return treeAddressRepository.findByTreeId(treeId)
                .stream()
                .map(ta -> buildAddressResponse(
                        ta.getAddress(),
                        ta.getAddressType(),
                        ta.getFromDate(),
                        ta.getToDate(),
                        false,
                        ta.getDescription()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void removeTreeAddress(UUID treeId, UUID treeAddressId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
        TreeAddress treeAddress = treeAddressRepository.findById(treeAddressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!treeAddress.getTree().getId().equals(treeId)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        treeAddressRepository.deleteById(treeAddressId);
    }

    // ==================== Helpers ====================

    private Address saveAddress(AddressRequest request) {
        Address address = Address.builder()
                .formattedAddress(request.formattedAddress())
                .addressLine(request.addressLine())
                .ward(request.ward())
                .district(request.district())
                .city(request.city())
                .province(request.province())
                .country(request.country())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .placeId(request.placeId())
                .build();
        return addressRepository.save(address);
    }

    private AddressType findAddressType(UUID addressTypeId) {
        return addressTypeRepository.findById(addressTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_TYPE_NOT_FOUND));
    }

    private void requireTreeMember(UUID userId, UUID treeId) {
        if (!treeMemberRepository.existsByUserIdAndTreeIdAndStatus(
                userId, treeId, TreeMemberStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TREE_ACCESS_DENIED);
        }
    }

    private void resetPrimaryPersonAddresses(UUID personId) {
        personAddressRepository.findByPersonId(personId)
                .forEach(pa -> {
                    if (pa.getIsPrimary() == 1) {
                        pa.setIsPrimary((short) 0);
                        personAddressRepository.save(pa);
                    }
                });
    }

    private AddressResponse buildAddressResponse(Address address, AddressType addressType,
                                                 java.time.LocalDateTime fromDate,
                                                 java.time.LocalDateTime toDate,
                                                 boolean isPrimary,
                                                 String description) {
        return new AddressResponse(
                address.getId(),
                address.getFormattedAddress(),
                address.getAddressLine(),
                address.getWard(),
                address.getDistrict(),
                address.getCity(),
                address.getProvince(),
                address.getCountry(),
                address.getLatitude(),
                address.getLongitude(),
                address.getPlaceId(),
                addressType.getName(),
                fromDate,
                toDate,
                isPrimary,
                description
        );
    }
}