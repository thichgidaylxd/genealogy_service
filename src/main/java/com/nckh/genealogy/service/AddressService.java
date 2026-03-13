package com.nckh.genealogy.service;

import com.nckh.genealogy.dto.request.address.AddressRequest;
import com.nckh.genealogy.dto.response.address.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    // Person address
    AddressResponse addPersonAddress(UUID personId, UUID requesterId, AddressRequest request);
    List<AddressResponse> getPersonAddresses(UUID personId);
    void removePersonAddress(UUID personId, UUID addressId, UUID requesterId);

    // Tree address
    AddressResponse addTreeAddress(UUID treeId, UUID requesterId, AddressRequest request);
    List<AddressResponse> getTreeAddresses(UUID treeId, UUID requesterId);
    void removeTreeAddress(UUID treeId, UUID treeAddressId, UUID requesterId);
}