package com.sogeti.iamservice.config;

import com.sogeti.iamservice.model.Account;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("basicauth")
@Data
public final class AccountListProperties {
    private List<Account> config;
}
