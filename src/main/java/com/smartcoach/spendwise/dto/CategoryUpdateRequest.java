package com.smartcoach.spendwise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryUpdateRequest {

    @NotBlank(message = "Category is required")
    private String category;

}
