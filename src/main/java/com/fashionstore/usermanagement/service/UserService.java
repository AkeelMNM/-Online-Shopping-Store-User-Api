package com.fashionstore.usermanagement.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fashionstore.usermanagement.dao.UserRepository;
import com.fashionstore.usermanagement.model.User;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class UserService {
	
	@Autowired
	UserRepository userRepository;
	
	public User createUser(User user) {
		return userRepository.save(user);
	}
	
	public User checkLogin(String email) {
		User user = userRepository.findOneByEmail(email);
		return user;
	}
	
	public User getUser(String id) {
		Optional<User> user = userRepository.findById(id);
		User fechedUser = user.get();
		return fechedUser;
	}
	
	public User updateUser(String id, User user) {
		Optional<User> Acc = userRepository.findById(id);
		User existAcc = Acc.get();
		String result = null;
		
		if(!existAcc.getEmail().equals(user.getEmail()) && !user.getEmail().equals("")) {
			existAcc.setEmail(user.getEmail());
		}
		
		if(!existAcc.getFullName().equals(user.getFullName()) && !user.getFullName().equals("")) {
			existAcc.setFullName(user.getFullName());
		}
		
		if(!existAcc.getPassword().equals(existAcc.getPassword()) && !user.getPassword().equals("")) {
			existAcc.setPassword(user.getPassword());
		}
		
		userRepository.save(existAcc);
		return existAcc;
	}
	
	public void removeUser(String id) {
		userRepository.deleteById(id);
	}

}
