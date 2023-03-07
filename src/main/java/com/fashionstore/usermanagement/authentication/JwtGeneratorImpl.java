package com.fashionstore.usermanagement.authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.fashionstore.usermanagement.model.User;

@Service
public class JwtGeneratorImpl implements JwtGeneratorInterface{
	
	@Value("${jwt.secret}")
	private String secret;
	
	@Value("${jwt.jwtExpirationMs}")
	private int jwtExpirationMs;

	@Override
	public String generateToken(User user) {
		String jwtToken="";
		jwtToken = Jwts.builder().setSubject(user.getEmail()).setIssuedAt(new Date()).setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS256, secret).compact();
		
		return jwtToken;
	}
	
	@Override
	public String validateToken(String token) {
		try {
			String isValid= Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
		
			return isValid;
		} catch (Exception e) {
			return e.getMessage();
		}
	}	
	  
}