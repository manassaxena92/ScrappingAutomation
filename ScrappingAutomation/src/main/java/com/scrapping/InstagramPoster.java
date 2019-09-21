package com.scrapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstagramPoster {

	@Value("${com.scrapping.systemFileSelect.script}")
	private String appleScriptPath;

	private static final String INSTAGRAM_DOMAIN = "https://www.instagram.com/accounts/login/";

	public void post(String emailId, String password, String filePath) throws InterruptedException, IOException {
		ChromeDriver driver = setup();
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
			driver.findElement(By.xpath("/html/body/span/section/div[2]/div[2]/div/div/div/button[1]/span")).click();
		} catch (Exception e) {
			// Do nothing
		}

		// click next button
		driver.findElement(By.xpath("/html/body/span/section/div[1]/header/div/div[2]/button")).click();
		Thread.sleep(1000);

		// click share button
		driver.findElement(By.xpath("/html/body/span/section/div[1]/header/div/div[2]/button")).click();
		driver.close();
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