
package com.example.demo.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private Integer role;
    private String inviteCode;
    private String name;
    private String studentId;
    private String phone;
}
