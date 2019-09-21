package com.scrapping;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections.CollectionUtils;
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
	private String fbUserName;

	@Value("${com.scrapping.fb.password}")
	private String fbPassword;

	@Value("${com.scrapping.insta.username}")
	private String instaUserName;

	@Value("${com.scrapping.insta.password}")
	private String instaPassword;

	@Value("${com.scrapping.image.dir}")
	private String fileDirectory;

	@Value("${com.scrapping.instagramPost.startIntervalInMin}")
	private int instagramPostStartIntervalMin;

	@Value("${com.scrapping.instagramPost.endIntervalInMin}")
	private int instagramPostEndIntervalMin;

	@Autowired
	private FacebookScrapping facebookScrapping;

	@Autowired
	private InstagramPoster instagramPoster;

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
		app.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Application started ");
		new InstagramActivity().run();
	}

	@Scheduled(cron = "${com.scrapping.cron.expression}", zone = "GMT+5:30")
	public void fbActivity() throws InterruptedException {
		List<String> fbPageNames = Arrays.asList(fbPageName.split(","));
		facebookScrapping.downloadImagesfromPages(fbUserName, fbPassword, fbPageNames, fileDirectory);
	}

	public void instaActivity(String imageFilePath) throws InterruptedException, IOException {
		instagramPoster.post(instaUserName, instaPassword, imageFilePath);
	}

	Timer timer = new Timer();

	class InstagramActivity extends TimerTask {
		@Override
		public void run() {
			System.out.println("Task running at: " + System.currentTimeMillis());
			String imageFilePath = "";
			File dir = new File(fileDirectory);
			List<String> children = Arrays.asList(dir.list());
			if (CollectionUtils.isEmpty(children)) {
				System.out.println("No images in file directory : " + fileDirectory);
			} else {
				imageFilePath = fileDirectory + "/" + children.get(0);
			}
			try {
				instaActivity(imageFilePath);
			} catch (InterruptedException | IOException e) {
				throw new RuntimeException("Failed posting to instagram", e);
			}
			int result = new Random().nextInt(instagramPostEndIntervalMin - instagramPostStartIntervalMin)
					+ instagramPostStartIntervalMin;
			System.out.println("random chosen: " + result);
			int delay = result * 60 * 1000;
			timer.schedule(new InstagramActivity(), delay);
		}
	}
}