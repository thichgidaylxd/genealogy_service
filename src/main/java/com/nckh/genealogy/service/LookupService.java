package com.nckh.genealogy.service;


import com.nckh.genealogy.dto.response.lookup.LookupResponse;

import java.util.List;

public interface LookupService {
    List<LookupResponse> getAddressTypes();
    List<LookupResponse> getEventTypes();
    List<LookupResponse> getMediaFileTypes();
}