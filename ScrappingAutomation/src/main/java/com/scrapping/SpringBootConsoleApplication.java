package com.scrapping;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@Configuration
@EnableScheduling
public class SpringBootConsoleApplication implements CommandLineRunner {

	@Value("${com.scrapping.fb.pagename}")
	private String fbPageName;

	@Value("${com.scrapping.fb.username}")
	private String userName;

	@Value("${com.scrapping.fb.password}")
	private String password;

	@Value("${com.scrapping.image.dir}")
	private String fileDirectory;

	@Autowired
	private FacebookScrapping facebookScrapping;

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Application started ");
	}

	@Scheduled(cron = "${com.scrapping.cron.expression}", zone = "GMT+5:30")
	public void fbActivity() throws InterruptedException {
		List<String> fbPageNames = Arrays.asList(fbPageName.split(","));
		facebookScrapping.login(userName, password);
		facebookScrapping.downloadImagesfromPages(fbPageNames, fileDirectory);
	}
}