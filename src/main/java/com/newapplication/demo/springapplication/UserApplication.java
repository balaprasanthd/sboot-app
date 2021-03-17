package com.newapplication.demo.springapplication;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import com.newapplication.demo.springapplication.userinfo.IUserRepository;
import com.newapplication.demo.springapplication.userinfo.User;

@SpringBootApplication
@EnableReactiveCosmosRepositories
public class UserApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserApplication.class);

    @Autowired
    private IUserRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
