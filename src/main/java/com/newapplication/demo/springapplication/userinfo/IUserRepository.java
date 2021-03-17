package com.newapplication.demo.springapplication.userinfo;

import org.springframework.stereotype.Repository;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

import reactor.core.publisher.Flux;

@Repository
public interface IUserRepository extends ReactiveCosmosRepository<User, String> {
    Flux<User> findByName(String name);
}