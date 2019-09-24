package com.scrapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstagramPoster {

	@Value("${com.scrapping.systemFileSelect.script}")
	private String appleScriptPath;

	@Value("${com.scrapping.numberOfHashTags}")
	private long numberOfHashTags;

	@Autowired
	private GoogleDriveImageUploader googleDriveManager;

	private static final String HASHTAGS = "/hashtags.txt";

	private static final String INSTAGRAM_DOMAIN = "https://www.instagram.com/accounts/login/";

	public void post(String emailId, String password, String filePath)
			throws InterruptedException, IOException, GeneralSecurityException {
		ChromeDriver driver = setup();
		try {
			login(emailId, password, driver);
			try {
				// Pop up click
				driver.findElement(By.xpath("/html/body/div[3]/div/div/div[3]/button[2]")).click();
				Thread.sleep(1000);
				((JavascriptExecutor) driver).executeScript("window.scrollTo(0,100);");
				Thread.sleep(1000);
				// Notification pop up cancel click
				driver.findElement(By.xpath("/html/body/div[3]/div/div/div[3]/button[2]")).click();
				Thread.sleep(1000);
			} catch (Exception e) {
				// Do nothing
			}
			// click on add post
			driver.findElement(By.xpath("/html/body/span/section/nav[2]/div/div/div[2]/div/div/div[3]/span")).click();

			Thread.sleep(1000);
			Runtime.getRuntime().exec("osascript " + appleScriptPath + " " + filePath);
			Thread.sleep(5000);

			try {
				// Click crop button if present
				WebElement cropElement = driver
						.findElement(By.xpath("/html/body/span/section/div[2]/div[2]/div/div/div/button[1]/span"));
				if (cropElement.getText().equalsIgnoreCase("Expand")) {
					cropElement.click();
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			// click next button
			driver.findElement(By.xpath("/html/body/span/section/div[1]/header/div/div[2]/button")).click();
			Thread.sleep(1000);

			// add hash tags to text area
			String textAreaValue = getRandomHashTags();

			driver.findElement(By.xpath("/html/body/span/section/div[2]/section[1]/div[1]/textarea"))
					.sendKeys(textAreaValue);
			// click share button
			driver.findElement(By.xpath("/html/body/span/section/div[1]/header/div/div[2]/button")).click();
			Thread.sleep(3000);
		} catch (Exception e) {
			System.out.println("Failed to post to instagram");
		}
		driver.close();
		googleDriveManager.rename(new File(filePath).getName());
		File file = new File(filePath);
		file.delete();
	}

	private String getRandomHashTags() throws FileNotFoundException, IOException {
		InputStream in = InstagramPoster.class.getResourceAsStream(HASHTAGS);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + HASHTAGS);
		}

		StringWriter writer = new StringWriter();
		IOUtils.copy(in, writer, StandardCharsets.UTF_8);
		String hashTags = writer.toString();
		List<String> hashTagsList = Arrays.asList(hashTags.split(","));
		String textAreaValue = "#houseofmaymay #houseofmemes ";
		Set<Integer> processedIndex = new HashSet<>();
		for (int i = 0; i < numberOfHashTags; i++) {
			Integer randomIndex = new Random().nextInt(hashTagsList.size());
			if (!processedIndex.contains(randomIndex)) {
				textAreaValue = textAreaValue + "#" + hashTagsList.get(randomIndex) + " ";
				processedIndex.add(randomIndex);
			}
		}
		return textAreaValue;
	}

	private void login(String emailId, String password, ChromeDriver driver) throws InterruptedException {
		driver.get(INSTAGRAM_DOMAIN);
		Thread.sleep(3000);
		System.out.println("Successfully opened instagram website");
		driver.manage().window().maximize();
		driver.findElement(By.name("username")).sendKeys(emailId);
		driver.findElement(By.name("password")).sendKeys(password);
		driver.findElement(By.xpath("/html/body/span/section/main/article/div/div/div/form/div[7]/button")).click();
		Thread.sleep(3000);
		driver.findElement(By.xpath("/html/body/span/section/main/div/button")).click();
		Thread.sleep(3000);
	}

	private ChromeDriver setup() {
		Map<String, String> mobileEmulation = new HashMap<>();
		mobileEmulation.put("deviceName", "Galaxy S5");
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
		ChromeDriver driver = new ChromeDriver(chromeOptions);
		return driver;
	}
}