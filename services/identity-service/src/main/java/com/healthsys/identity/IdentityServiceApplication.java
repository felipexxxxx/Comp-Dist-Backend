package com.healthsys.identity;

import com.healthsys.identity.config.BootstrapAdminProperties;
import com.healthsys.identity.config.JwtProperties;
import com.healthsys.identity.config.MessagingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    JwtProperties.class,
    BootstrapAdminProperties.class,
    MessagingProperties.class
})
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
