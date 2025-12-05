package com.rest_mag_sys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.rest_mag_sys.mapper")
@EnableTransactionManagement
public class RestMagSysApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestMagSysApplication.class, args);
    }

}
