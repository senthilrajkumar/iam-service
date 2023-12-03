package com.sogeti.iamservice;

import com.sogeti.iamservice.controller.AuthenticationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class IamServiceApplicationTests {

	@Autowired
	private AuthenticationController controller;

	@Test
	void contextLoads() {
		assertThat(controller).isNotNull();
	}

}
