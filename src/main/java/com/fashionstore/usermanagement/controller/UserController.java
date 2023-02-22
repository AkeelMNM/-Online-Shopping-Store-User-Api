package com.fashionstore.usermanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashionstore.usermanagement.model.Login;
import com.fashionstore.usermanagement.model.User;
import com.fashionstore.usermanagement.service.UserService;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController {
	
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService) {
		this.userService =  userService;
	}
	
	@PostMapping
	public ResponseEntity<Object> createUser(@RequestBody User user){
		User newUser = new User();
		newUser.setEmail(user.getEmail());
		newUser.setFullName(user.getFullName());
		newUser.setIsActive(user.getIsActive());
		newUser.setPassword(user.getPassword());
		
		User createdUser = userService.createUser(newUser);
		
		if(createdUser == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not created!.");
		}else {
			createdUser.setPassword(null);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
		}
	}
	
	@PostMapping(value="/login")
	public ResponseEntity<Object> LoginUser(@RequestBody User user){
		User fetchedUser = userService.checkLogin(user.getEmail());
		if(fetchedUser == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The user does not exist!.");
		}
		else if(!user.getPassword().equals(fetchedUser.getPassword())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User passwords don't match!");
		}else if(!fetchedUser.getIsActive()){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not active!");
		}else {
			Login login = new Login();
			login.set_id(fetchedUser.get_id());
			login.setIsActive(fetchedUser.getIsActive());
			login.setIsVerified(true);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(login);
		}
	}
	
	@GetMapping
	public ResponseEntity<User> getUser(@RequestParam String id){
		User user = userService.getUser(id);
		user.setPassword(null);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
	}
	
	@PutMapping
	public ResponseEntity<User> updateUser(@RequestParam String id, @RequestBody User user){
		User updatedUser = userService.updateUser(id, user);
		updatedUser.setPassword(null);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
	}
	
	@DeleteMapping
	public ResponseEntity<String> removeUser(@RequestParam String id) {
		userService.removeUser(id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("User removed");
	}
	
}
