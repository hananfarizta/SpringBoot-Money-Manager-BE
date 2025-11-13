package dev.hananfarizta.moneymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private String status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
