package com.nckh.genealogy.dto.request.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAlbumRequest {
    @NotNull(message = "Id album không được để trống")
    UUID id;

    @NotBlank(message = "Tên album không được để trống")
    String name;

    String description;
}
