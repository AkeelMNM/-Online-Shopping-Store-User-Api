package com.fashionstore.usermanagement.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fashionstore.usermanagement.model.User;

public interface UserRepository extends MongoRepository<User, String> {
	User findOneByEmail(String email);
}

