package com.sogeti.iamservice.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

    private String username;

    private String password;

    private String role;

}
