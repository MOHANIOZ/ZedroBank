package com.bankApp.banking_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
//@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String password;
    public UserDto() {
    }
}
