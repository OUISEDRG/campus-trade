
package com.example.demo.dto;

import lombok.Data;

@Data
public class UserLoginResponseDTO {
    private Long id;
    private String username;
    private String name;
    private String avatar;
    private String email;
    private String college;
    private String studentId;
    private String phone;
    private Integer role;
    private Integer status;
}
