package com.threlease.base;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret-key=Nwzbu8o3Rkf0iOJj0wpY2i749zjM7kr6Hnnl6x/n4e+tJoAmn5wYJt/jeFX71cawaR4kQFTw1ACeJgsHAJ/AeA==",
		"app.token.storage=rdb",
		"app.token.validate-schema=false",
		"app.redis.enabled=false"
})
class BaseApplicationTests {

	@Test
	void contextLoads() {
	}

}
