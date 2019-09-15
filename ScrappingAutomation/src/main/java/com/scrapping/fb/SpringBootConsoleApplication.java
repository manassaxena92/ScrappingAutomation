package com.scrapping.fb;

import static java.lang.System.exit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootConsoleApplication implements CommandLineRunner {

    public static void main(String[] args) throws Exception {

        SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
        app.run(args);

    }

    @Override
    public void run(String... args) throws Exception {
    	
    	String emailId = "";
    	String password = "";
    	String fileDirectory = "";

    	String exePath = "/usr/local/Caskroom/chromedriver/76.0.3809.126/chromedriver";
        System.setProperty("webdriver.chrome.driver", exePath);

        WebDriver driver = new ChromeDriver();

        driver.get("https://www.facebook.com");
        System.out.println("Successfully opened the website");
        driver.manage().window().maximize();
        driver.findElement(By.id("email")).sendKeys(emailId);
        driver.findElement(By.id("pass")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value=\"Log In\"]")).click();
        System.out.println("Successfully logged in");
        Thread.sleep(3000);
        driver.get("https://www.facebook.com/pg/SarcasticWorld/posts/");
        Thread.sleep(4000);
        List<WebElement> picElements = driver.findElements(By.xpath("//a[@rel=\"theater\"]"));
        picElements
        	.stream()
        	.filter(picElement -> StringUtils.isNotEmpty(picElement.getAttribute("data-ploi")))
        	.forEach( picElement -> {
        		try {
        			String picUrl = picElement.getAttribute("data-ploi");
        			System.out.println("picUrl:" + picUrl);
        			URL url = new URL(picUrl);
        			InputStream in;
        			in = new BufferedInputStream(url.openStream());
        			
        			OutputStream out = new BufferedOutputStream(new FileOutputStream(fileDirectory + Math.random() + ".jpg"));
	        		for ( int i; (i = in.read()) != -1; ) {
	        		    out.write(i);
	        		}
	        		in.close();
	        		out.close();
					} catch (Exception e) {
	 					e.printStackTrace();
	 				}
        	});
        exit(0);
    }
}