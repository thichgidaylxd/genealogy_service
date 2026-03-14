package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.request.address.UpdatePersonAddressRequest;
import com.nckh.genealogy.dto.request.address.UpdateTreeAddressRequest;
import com.nckh.genealogy.dto.response.address.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    // Person address
    AddressResponse addPersonAddress(UUID treeId, UUID personId, UUID requesterId, AddressRequest request);
    List<AddressResponse> getPersonAddresses(UUID treeId, UUID personId, UUID requesterId);
    AddressResponse updatePersonAddress(UUID treeId, UUID personId, UUID addressId,UUID requesterId, UpdatePersonAddressRequest request);
    void removePersonAddress(UUID treeId, UUID personId, UUID addressId, UUID requesterId);

    // Tree address
    AddressResponse addTreeAddress(UUID treeId, UUID requesterId, AddressRequest request);
    List<AddressResponse> getTreeAddresses(UUID treeId, UUID requesterId);
    AddressResponse updateTreeAddress(UUID treeId, UUID addressId, UUID requesterId, UpdateTreeAddressRequest request);
    void removeTreeAddress(UUID treeId, UUID treeAddressId, UUID requesterId);
}