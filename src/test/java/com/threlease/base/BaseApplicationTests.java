package com.threlease.base;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret-key=Nwzbu8o3Rkf0iOJj0wpY2i749zjM7kr6Hnnl6x/n4e+tJoAmn5wYJt/jeFX71cawaR4kQFTw1ACeJgsHAJ/AeA==",
		"app.token.storage=rdb",
		"app.redis.enabled=false",
		"spring.flyway.enabled=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.url=jdbc:h2:mem:base-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.properties.hibernate.default_schema=PUBLIC",
		"app.database.jpa-schema=PUBLIC"
})
class BaseApplicationTests {

	@Test
	void contextLoads() {
	}

}
