package com.scrapping;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

	@Value("${com.scrapping.fb.commaSeparated.pagenames}")
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

	@Value("${com.scrapping.stopPostingForNext.intervalInHours}")
	private int stopPostingForNextHours;

	@Autowired
	private FacebookScrapping facebookScrapping;

	@Autowired
	private InstagramPoster instagramPoster;

	@Autowired
	private GoogleDriveImageUploader googleDriveManager;

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
		try {
			List<String> fbPageNames = Arrays.asList(fbPageName.split(","));
			facebookScrapping.uploadImagesToDrive(fbUserName, fbPassword, fbPageNames, fileDirectory);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public void instaActivity(String imageFilePath) throws InterruptedException, IOException, GeneralSecurityException {
		try {
			instagramPoster.post(instaUserName, instaPassword, imageFilePath);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	Timer timer = new Timer();

	class InstagramActivity extends TimerTask {
		@Override
		public void run() {
			System.out.println("Task running at: " + System.currentTimeMillis());
			try {
				File file = googleDriveManager.downloadFirstFile(fileDirectory);
				if (file != null && file.exists()) {
					instaActivity(file.getAbsolutePath());
				}
			} catch (Exception e) {
				System.out.println("Failed posting to instagram");
			}

			Calendar cal = Calendar.getInstance(); // Create Calendar-Object
			cal.setTime(new Date()); // Set the Calendar to now
			int hour = cal.get(Calendar.HOUR_OF_DAY);

			int delay;
			if (hour >= 1 && hour < 7) {
				delay = stopPostingForNextHours * 60 * 60 * 1000;
			} else {
				int result = new Random().nextInt(instagramPostEndIntervalMin - instagramPostStartIntervalMin)
						+ instagramPostStartIntervalMin;
				delay = result * 60 * 1000;
			}
			System.out.println("Next run will be after delay of: " + delay + " milliseconds");
			timer.schedule(new InstagramActivity(), delay);
		}
	}
}