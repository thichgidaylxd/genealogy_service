package com.nckh.genealogy.service.impl;

import com.nckh.genealogy.dto.response.lookup.LookupResponse;
import com.nckh.genealogy.repository.*;
import com.nckh.genealogy.service.LookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupServiceImpl implements LookupService {

    private final AddressTypeRepository addressTypeRepository;
    private final EventTypeRepository eventTypeRepository;
    private final RoleInEventRepository roleInEventRepository;
    private final MediaFileTypeRepository mediaFileTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LookupResponse> getAddressTypes() {
        return addressTypeRepository.findAll().stream()
                .map(a -> new LookupResponse(a.getId(), a.getName(), a.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupResponse> getEventTypes() {
        return eventTypeRepository.findAll().stream()
                .map(e -> new LookupResponse(e.getId(), e.getName(), e.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupResponse> getRoleInEvents() {
        return roleInEventRepository.findAll().stream()
                .map(r -> new LookupResponse(r.getId(), r.getName(), r.getDescription()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LookupResponse> getMediaFileTypes() {
        return mediaFileTypeRepository.findAll().stream()
                .map(m -> new LookupResponse(m.getId(), m.getName(), m.getDescription()))
                .toList();
    }
}