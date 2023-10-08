package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.repo.UserRepository;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
	
		
		return "forgot_email_form";
	}
	
	@PostMapping("/send_otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
		
		//generating otp
		
		int otp = random.nextInt(9999);
		
		System.out.println("otp "+otp);
		
		//send otp to mail
		String subject= "OTP from SCM ";
		String text = "OTP is "+ otp+"";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, text, to);
		
		if(flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check your email id!");
			return "forgot_email_form";
			
		}
		
	}
	
	//verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");
		
		if(myOtp == otp) {
			//change password form
			User user = this.userRepository.getUserByUserName(email);
			
			if(user == null) {
				//send error message
				session.setAttribute("message", "User does not exist with this email!");
				return "forgot_email_form";
				
			}else {
				//send chnage password form
				
			}
			
			return "password_change_form";
		}else {
			session.setAttribute("message", "Wrong OTP entered !");
			return "verify_otp";
		}
		
	}
		
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully";
		
	}
	

}
