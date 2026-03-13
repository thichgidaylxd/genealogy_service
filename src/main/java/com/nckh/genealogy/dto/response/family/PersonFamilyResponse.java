package com.nckh.genealogy.dto.response.family;

import com.nckh.genealogy.dto.response.person.PersonResponse;

import java.util.List;

// Toàn bộ thông tin gia đình của 1 person
public record PersonFamilyResponse(
        PersonResponse person,

        // Cha mẹ của person
        FamilyResponse parentFamily,    // nullable — chưa có cha mẹ

        // Các gia đình mà person là cha/mẹ (có thể có nhiều — tái hôn)
        List<FamilyResponse> spouseFamilies
) {}