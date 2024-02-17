package xatal.petlove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SharedzApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharedzApplication.class, args);
	}
}
