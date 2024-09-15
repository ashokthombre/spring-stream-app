package com.stream.app;

import com.stream.app.services.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringStreamBackendApplicationTests {

	@Autowired
	private VideoService videoService;

	@Test
	void contextLoads() {
		videoService.processVideo("fb77af0d-e094-4fc3-a189-73e14207b4ed");

	}
//
//	@Test
//	void testMethod()
//	{
//
//	}

}
