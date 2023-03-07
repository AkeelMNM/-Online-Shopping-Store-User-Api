package com.fashionstore.usermanagement.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.fashionstore.usermanagement.authentication.JwtGeneratorInterface;
import com.fashionstore.usermanagement.model.Login;
import com.fashionstore.usermanagement.model.User;
import com.fashionstore.usermanagement.service.UserService;


@CrossOrigin(origins = {"https://localhost:3000","https://localhost:5000"},allowCredentials = "true",allowedHeaders = "*")
@RestController
@RequestMapping("/user")
public class UserController {
	
	private final UserService userService;
	private JwtGeneratorInterface jwtGenerator;
	
	@Autowired
	public UserController(UserService userService, JwtGeneratorInterface jwtGenerator) {
		this.userService =  userService;
		this.jwtGenerator = jwtGenerator;
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
	public ResponseEntity<Object> loginUser(@RequestBody User user,HttpServletResponse response){
		User fetchedUser = userService.checkLogin(user.getEmail());
		Login login = new Login();
		
		if(fetchedUser == null) {
			login.setIsVerified(false);
			login.setMessage("The user does not exist!");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(login);
		}
		else if(!user.getPassword().equals(fetchedUser.getPassword())) {
			login.setIsVerified(false);
			login.setMessage("User passwords don't match!");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(login);
		}else if(!fetchedUser.getIsActive()){
			login.setIsVerified(false);
			login.setMessage("User is not active!");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(login);
		}else {
			/**
			 * Creating JWT Token
			 */
			String token = jwtGenerator.generateToken(fetchedUser);
			
			/**
			 * Creating HttpOnly cookie and adding the JWT token in the cookie send it in the response
			 */
			Cookie cookie = new Cookie("jwtToken", token);
			cookie.setMaxAge(7 * 24 * 60 * 60);
			cookie.setSecure(true);
			cookie.setHttpOnly(true);
			cookie.setPath("/");
			cookie.setDomain("localhost");
			response.addCookie(cookie);
			
			/**
			 * Sending user id and the other information as success of the login
			 */
			login.set_id(fetchedUser.get_id());
			login.setIsActive(fetchedUser.getIsActive());
			login.setIsVerified(true);
			
			
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(login);
		}
	}
	
	@PostMapping(value="/authenticate")
	public ResponseEntity<Object> authenticateUser(HttpServletRequest request){
		
		/**
		 * Retrieving the httpOnly cookie send in the request
		 */
		 Cookie[] cookies = request.getCookies();
		 String token = "";
		 if (cookies != null) {
			 token =  Arrays.stream(cookies).map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining(", "));
		    }
		
		 /**
		  * Validating the JWT token
		  */
		String username = jwtGenerator.validateToken(token);
		User user = userService.checkLogin(username);
		if(user != null) {
			if(user.getEmail().equals(username)) {
				Login login = new Login();
				login.setIsActive(user.getIsActive());
				login.setIsVerified(true);
				
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(login);
			}	
		}
		
		HashMap<String, Object> errResponse = new HashMap<>();
		errResponse.put("message", "Invalid token");
		errResponse.put("IsVerified", false);
	    
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errResponse);
	}
	
	@PostMapping(value="/validate")
	public ResponseEntity<Object> CheckUserPassword(HttpServletRequest request, @RequestBody Map<String, String> payload){
		
		/**
		 * Retrieving the httpOnly cookie send in the request
		 */
		 Cookie[] cookies = request.getCookies();
		 String token = "";
		 if (cookies != null) {
			 token =  Arrays.stream(cookies).filter(c -> c.getName().equals("jwtToken")).findFirst().map(Cookie::getValue).orElse(null);
		    }
		
		 /**
		  * Validating the JWT token
		  */
		String username = jwtGenerator.validateToken(token);
		HashMap<String, Object> response = new HashMap<>();
		if(username == null) {
			response.put("message", "Invalid token");
			response.put("IsVerified", false);
			
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		}
		
		String oldPassword = payload.get("oldPassword");
		String newPassword = payload.get("newPassword");
		
		/**
		  * Get user by userName
		  */
		User user = userService.checkLogin(username);
		
		 /**
		  * Validating the old password entered by user with stored password 
		  */
		if (!user.getPassword().equals(oldPassword)) {
			response.put("message", "Your old password is not matched");
			response.put("IsPasswordNotSame", false);
			
			return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
		}
		
		/**
		  * Validating the old password entered by user with new password 
		  */
		if (user.getPassword().equals(newPassword)) {
			response.put("message", "Your old password and new passowrd is same");
			response.put("IsPasswordNotSame", false);
		} else {
			response.put("message", "Password is not matched");
			response.put("IsPasswordNotSame", true);
		}
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
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
	
	@GetMapping(value="/logout")
	public ResponseEntity<Object> logout(HttpServletResponse response){
		/**
		 * Creating HttpOnly cookie and adding the JWT token in the cookie send it in the response
		 */
		Cookie cookie = new Cookie("jwtToken", null);
		cookie.setMaxAge(0);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setDomain("localhost");
		response.addCookie(cookie);
		
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("User logout successful");
	}
}
