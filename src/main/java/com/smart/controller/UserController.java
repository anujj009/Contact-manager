package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;

import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repo.ContactRepository;
import com.smart.repo.MyOrderRepo;
import com.smart.repo.UserRepository;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private MyOrderRepo myOrderRepo;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();

		// get the user using username
		User user = userRepository.getUserByUserName(username);

		model.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		String userrName = principal.getName();
		// get user by username(email)

		User user = this.userRepository.getUserByUserName(userrName);

		model.addAttribute("user", user);
		model.addAttribute("title", "Home");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing add contact form

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// Processing and uploading file
			if (file.isEmpty()) {

				contact.setImage("contact.jpg");
				System.out.println("No image");
			} else {

				contact.setImage(file.getOriginalFilename());

				File savefile = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image uploaded");
			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("data" + contact.getName());
			System.out.println("data added");

			// success message
			session.setAttribute("message", new Message("Congrats ! Contact is added", "success"));

		} catch (Exception e) {

			System.out.println("Error " + e.getMessage());

			// error message
			session.setAttribute("message", new Message("something went wrong", "danger"));
		}

		return "normal/add_contact_form";

	}

	// show contact handler //per page=5[n]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "Show user contacts");
		// to send contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// to show specific contact detail
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		// check
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// to delete contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		System.out.println(cId);
		// check

		this.contactRepository.delete(contact);

		session.setAttribute("message", new Message("Contact deleted successfully", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// to open update form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {

		m.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cid).get();

		m.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model model, HttpSession session, Principal principal) {

		try {
			// old contact detail
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

			// image
			if (!file.isEmpty()) {

				// rewrite
				// update new pic

				File savefile = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			} else {

				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact updated..", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//user profile
	@GetMapping("/profile")
	public String userProfile(Model m) {
		
		m.addAttribute("title", "User profile");
		return "normal/profile";
		
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings(Model m) {
		
		m.addAttribute("title", "Settings");
		
		return "normal/settings";
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,
					Principal  principal, HttpSession session) {
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			// change
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Password successfully changed", "success"));

		}else {
			//error
			session.setAttribute("message", new Message("Old password is wrong", "danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/settings";
		
	}
	
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws RazorpayException {
		
		System.out.println((data));
		int amt = Integer.parseInt(data.get("amount").toString());
		
		var client = new RazorpayClient("rzp_test_BAILOBGbIhmAlb", "qSEus6fWCxu28vm1l2U3hDKc");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_123");
		
		//creating new order
		Order order = client.Orders.create(ob);
		
		System.out.println(order);
		
		//save order in database
		MyOrder myOrder = new MyOrder();
		
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepo.save(myOrder);
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		MyOrder myOrder = this.myOrderRepo.findByOrderId(data.get("order_id").toString());
		
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		this.myOrderRepo.save(myOrder);
		
		System.out.println(data);
		
		return ResponseEntity.ok(Map.of("msg", "updated"));
	}
	

}
