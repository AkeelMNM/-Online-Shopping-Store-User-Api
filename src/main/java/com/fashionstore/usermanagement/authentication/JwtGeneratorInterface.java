package com.fashionstore.usermanagement.authentication;

import com.fashionstore.usermanagement.model.User;

public interface JwtGeneratorInterface {
	String generateToken(User user);
	
	String validateToken(String token);
}
