package com.newapplication.demo.springapplication.helloworld;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorld {
	
	@GetMapping("/hello")
	public String helloWorld() {
		return "Hello World";
	}
}
