package com.scrapping;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

@Component
public class FacebookScrapping {
	
	private static WebDriver driver = new ChromeDriver();
	
	public void login(String emailId, String password) throws InterruptedException {
		driver.get("https://www.facebook.com");
		System.out.println("Successfully opened the website");
		driver.manage().window().maximize();
		driver.findElement(By.id("email")).sendKeys(emailId);
		driver.findElement(By.id("pass")).sendKeys(password);
		driver.findElement(By.xpath("//input[@value=\"Log In\"]")).click();
		System.out.println("Successfully logged in");
		Thread.sleep(3000);
	}
	
	public void downloadImagesfromPages(List<String> fbPageNames, String downloadPath) {
		String fbdomain = "https://www.facebook.com/";
		String fbPostsResource = "/posts/";
		fbPageNames.stream().forEach(pageName -> {
			System.out.println("Downloading images of page:" + pageName);
			String fullPagePostsUrl = fbdomain + pageName + fbPostsResource;
			driver.get(fullPagePostsUrl);
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				throw new RuntimeException("Failed to load page with url " + fullPagePostsUrl, e);
			}
			List<WebElement> picElements = driver.findElements(By.xpath("//a[@rel=\"theater\"]"));
			picElements.stream().filter(picElement -> StringUtils.isNotEmpty(picElement.getAttribute("data-ploi")))
					.forEach(picElement -> {

						String picUrl = picElement.getAttribute("data-ploi");
						System.out.println("picUrl:" + picUrl);
						String picName = picUrl.substring(picUrl.lastIndexOf("/") + 1, picUrl.indexOf(".jpg") + 4);
						Set<String> existingImageNames = getPendingToPostImageNames(downloadPath);

						if (existingImageNames.contains(picName)) {
							System.out.println("Pic " + picUrl + "already present in image folder, skipping");
							return;
						}

						try {
							URL url = new URL(picUrl);
							BufferedImage img = ImageIO.read(url);
							File file = new File(downloadPath + picName);
							ImageIO.write(img, "jpg", file);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});

		});
	}
	
	private Set<String> getPendingToPostImageNames(String downloadPath) {
		Set<String> results = new HashSet<>();
		File[] files = new File(downloadPath).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				results.add(file.getName());
			}
		}
		return results;
	}

}
