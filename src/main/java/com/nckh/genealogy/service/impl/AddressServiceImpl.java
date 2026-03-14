package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.request.address.UpdatePersonAddressRequest;
import com.nckh.genealogy.dto.request.address.UpdateTreeAddressRequest;
import com.nckh.genealogy.dto.response.address.AddressResponse;
import com.nckh.genealogy.entity.*;
import com.nckh.genealogy.enums.TreeMemberStatus;
import com.nckh.genealogy.exception.AppException;
import com.nckh.genealogy.exception.ErrorCode;
import com.nckh.genealogy.mapper.AddressMapper;
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
    private final AddressMapper addressMapper;
    private final PersonEventRepository personEventRepository;
    private final TreeEventRepository treeEventRepository;

    // ==================== Person Address ====================

    @Override
    @Transactional
    public AddressResponse addPersonAddress(UUID treeId, UUID personId, UUID requesterId, AddressRequest request) {
        requireTreeMember(requesterId, treeId);
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
    public List<AddressResponse> getPersonAddresses(UUID treeId, UUID personId, UUID requesterId) {
        requireTreeMember(requesterId, treeId);
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
    public AddressResponse updatePersonAddress(UUID treeId, UUID personId, UUID requesterId,
                                               UUID addressId, UpdatePersonAddressRequest request) {
        requireTreeMember(requesterId, treeId);

        personRepository.findByIdAndDeletedAtIsNull(personId)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));

        PersonAddress personAddress = personAddressRepository
                .findByPersonIdAndAddressId(personId, addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        // Lưu lại old address để cleanup sau
        UUID oldAddressId = personAddress.getAddress().getId();

        // Tạo Address mới thay vì mutate Address cũ (tránh ảnh hưởng các liên kết khác)
        Address newAddress = addressMapper.toEntity(request);
        addressRepository.save(newAddress);

        // Cập nhật liên kết PersonAddress
        AddressType addressType = findAddressType(request.addressTypeId());
        Short isPrimary = Boolean.TRUE.equals(request.isPrimary()) ? (short) 1 : (short) 0;

        if (isPrimary == 1 && personAddress.getIsPrimary() != 1) {
            resetPrimaryPersonAddresses(personId);
        }

        personAddress.setAddress(newAddress);
        personAddress.setAddressType(addressType);
        personAddress.setFromDate(request.fromDate());
        personAddress.setToDate(request.toDate());
        personAddress.setIsPrimary(isPrimary);
        personAddress.setDescription(request.description());
        personAddressRepository.save(personAddress);

        // Dọn Address cũ nếu không còn ai dùng
        cleanupOrphanAddress(oldAddressId);

        return buildAddressResponse(newAddress, addressType,
                request.fromDate(), request.toDate(),
                isPrimary == 1, request.description());
    }

    @Override
    @Transactional
    public void removePersonAddress(UUID treeId, UUID personId, UUID addressId, UUID requesterId) {
        requireTreeMember(requesterId, personId);
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
    public AddressResponse updateTreeAddress(UUID treeId, UUID treeAddressId, UUID requesterId,
                                             UpdateTreeAddressRequest request) {
        requireTreeMember(requesterId, treeId);

        TreeAddress treeAddress = treeAddressRepository.findById(treeAddressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        // Đảm bảo treeAddress thuộc đúng tree
        if (!treeAddress.getTree().getId().equals(treeId)) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // Lưu lại old address để cleanup sau
        UUID oldAddressId = treeAddress.getAddress().getId();

        // Tạo Address mới thay vì mutate Address cũ
        Address newAddress = addressMapper.toEntity(request);
        addressRepository.save(newAddress);

        // Cập nhật liên kết TreeAddress
        AddressType addressType = findAddressType(request.addressTypeId());

        treeAddress.setAddress(newAddress);
        treeAddress.setAddressType(addressType);
        treeAddress.setFromDate(request.fromDate());
        treeAddress.setToDate(request.toDate());
        treeAddress.setDescription(request.description());
        treeAddressRepository.save(treeAddress);

        // Dọn Address cũ nếu không còn ai dùng
        cleanupOrphanAddress(oldAddressId);

        return buildAddressResponse(newAddress, addressType,
                request.fromDate(), request.toDate(),
                false, request.description());
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

    private Address findById(UUID addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
    }

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
    private void cleanupOrphanAddress(UUID addressId) {
        boolean usedByPerson = personAddressRepository.existsByAddressId(addressId);
        boolean usedByTree   = treeAddressRepository.existsByAddressId(addressId);
        boolean usedByEvent  = personEventRepository.existsByAddressId(addressId)
                || treeEventRepository.existsByAddressId(addressId);

        if (!usedByPerson && !usedByTree && !usedByEvent) {
            addressRepository.deleteById(addressId);
        }
    }


}