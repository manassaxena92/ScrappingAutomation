package com.scrapping;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacebookScrapping {

	private static final String FACEBOOK_DOMAIN = "https://www.facebook.com/";
	private static final String FB_POSTS_RESOURCE = "/posts/";

	@Autowired
	private GoogleDriveImageUploader googleDriveManager;

	public void uploadImagesToDrive(String emailId, String password, List<String> fbPageNames, String downloadPath)
			throws InterruptedException {
		WebDriver driver = new ChromeDriver();
		login(emailId, password, driver);
		fbPageNames.stream().forEach(pageName -> {
			System.out.println("Downloading images of page:" + pageName);
			String fullPagePostsUrl = FACEBOOK_DOMAIN + pageName + FB_POSTS_RESOURCE;
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
						String imageType = "jpg";
						if(picUrl.indexOf("." + imageType)==-1) {
							imageType = "png";
						}
						String picName = picUrl.substring(picUrl.lastIndexOf("/") + 1, picUrl.indexOf("." + imageType) + 4);
						

						if (googleDriveManager.fileExist(picName)) {
							System.out.println("Pic " + picUrl + " already present in image folder, skipping");
							return;
						}

						try {
							URL url = new URL(picUrl);
							BufferedImage img = ImageIO.read(url);
							File file = new File(downloadPath + picName);
							ImageIO.write(img, imageType, file);
							googleDriveManager.uploadImage(downloadPath + picName, picName);
							file.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					});

		});
		driver.close();
	}

	private void login(String emailId, String password, WebDriver driver) throws InterruptedException {
		driver.get(FACEBOOK_DOMAIN);
		System.out.println("Successfully opened the website");
		driver.manage().window().maximize();
		driver.findElement(By.id("email")).sendKeys(emailId);
		driver.findElement(By.id("pass")).sendKeys(password);
		driver.findElement(By.xpath("//input[@value=\"Log In\"]")).click();
		System.out.println("Successfully logged in");
		Thread.sleep(3000);
	}

}
