package executors;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import constants.CurrentConstants;
import managers.ReportManager;

import org.apache.commons.io.FileUtils;


import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;


//the test results will not show failure on the console or on the testNG Interface anymore, since the run is clotured in try-catch blocks,
//that will report the failures to the extentreporter, and ignore displaying it on the console output
//so dont flip out if you dont get a test failure on the console or testNG-output. Consider only extentreports
public class SauceDemoTest {
	
	// P:positive, N:negative, F:functionality (is differenciated to tech and logic), NF:non-functional, T:technical, L:logic
	//about saucedemo:
//			U1: standard_user: correct functionality until the end
//			U2: locked_out_user: cannot login, because locked out
//			U3: problem_user: all products with same wrong picture, only backpack light and Onsie can be added to cart, but not removed
//			U4: performance_glitch_user: correct functionality, but slow response
//			U5: error_user: only backpack light and Onsie can be added to cart, but not removed
//			U6: visual_user: first pic item wrong, prices wrong
//			UX: non-existing user
	
	//constructor of constants, URL to be configured in constants class
	public static CurrentConstants current = new CurrentConstants("chrome");
	public static ReportManager reporterM;
	public ReportManager testM;
	
	
	@BeforeMethod
	public static void setUp() {
		
		ReportManager.reportGenerator(); //a report has been generated / initialized
	
		
		current.cd.manage().window().maximize();
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		current.cd.get(current.cURL);
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
	
	}
	@Test
	public  void loginTest1() {
		//P+T login
		ExtentTest test1 = ReportManager.testGenerator("positive login test", "User1").info("User credentials: standard_user/secret_sauce");
		
		try {
			current.cd.findElement(By.id("user-name")).sendKeys("standard_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
			
		}
		catch (Exception e) {
			test1.fail("test execution failed because " + e.getMessage()); //catch the test code execution failure
			
		}
//		if(current.cd.findElement(By.xpath("//a[@data-test=\"shopping-cart-link\"]")).isDisplayed()) {
//			test1.pass("test successful");
//		}
//		
//		else if(!current.cd.findElement(By.xpath("//a[@data-test=\"shopping-cart-lnk\"]")).isDisplayed()) {
//			test1.fail("test failed");
//		}
			
		
			
	}
	@Test
	public  void loginTest2() {
		
		//N,T+L, login, wrong credentials
		ExtentTest test2 = ReportManager.testGenerator("negative login test", "User 2").info("ser credentials: default_user/secret_sauce");
		
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		try {
			current.cd.findElement(By.id("user-name")).sendKeys("sauce_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
			
		}
		catch(Exception e) {
			test2.fail("test execution failed because " + e.getMessage()); 
		}
	}
	
	@Test
	public  void loginTest3() {
		
		//N, L, login, different error message
		ExtentTest test3 = ReportManager.testGenerator("negative login test", "User3: locked_out_user").info("more details");
		
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		try {
		
			current.cd.findElement(By.id("user-name")).sendKeys("locked_out_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
		} catch(Exception e) {
			test3.fail("test execution failed because " + e.getMessage());
		}
	}
	@Test
	public  void loginTest4() {
		
		//P,L, verify cart contains the correct items(1) backpack
		ExtentTest test4 = ReportManager.testGenerator("positive test", "functional test").info("test goal: verify cart conttains the one correct item: backpack");
		
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		try {
			current.cd.findElement(By.id("user-name")).sendKeys("standard_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
			//WebElement clickedItem = current.cd.findElement(By.id("add-to-cart-sauce-labs-backpack"));
			WebElement clickedItem = current.cd.findElement(By.xpath("//button[@id=\"add-to-cart-sauce-labs-bike-light\"]"));
			String clickedTxt = clickedItem.getDomAttribute("name") ;//save for later comparison with keyword
			
			//System.out.println("clicked: " + clickedTxt);//for visualization temporary only
			test4.info("clicked item  :" + clickedTxt);
			
			clickedItem.click();
		
			current.cd.findElement(By.xpath("//a[@data-test=\"shopping-cart-link\"]")).click(); // go to the cart
			WebElement currentItem = current.cd.findElement(By.xpath("//div[@data-test=\"inventory-item-name\"]"));//check the added item name
			
			//System.out.println("current: " + currentItem.getText());
			test4.info("current item in shopping cart :" + currentItem.getText());
			
			if (currentItem.getText().contains("Light")) {
				//Assert.assertTrue(true);
				test4.pass("correct item in the cart, test successful");
			}
			else if (!currentItem.getText().contains("Light")){
				//Assert.assertTrue(true);
				test4.fail("wrong item in the cart, test failed");
			}

			current.cd.findElement(By.id("react-burger-menu-btn")).click();
			current.cd.findElement(By.id("logout_sidebar_link")).click();
			
		} catch(Exception e) {
			test4.fail("test execution failed because " + e.getMessage());
		}
		
		
		//even after logging out, the item was not removed, which means data remains safe after the logout
		
		
	}
	
	@Test
	public  void loginTest5() {
		//P, T, verify a purchase process till the end with 2 items
		//U5, form data{Mike Tyson 299117}
		ExtentTest test5 = ReportManager.testGenerator("functional test 2", "full purchase test").info("test goal: verify a succesful purchase with 2 items");
		
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		try {
			current.cd.findElement(By.id("user-name")).sendKeys("standard_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
			current.cd.findElement(By.xpath("//button[@id=\"remove-sauce-labs-bike-light\"]")).click();
			WebElement clickedOne = current.cd.findElement(By.id("add-to-cart-sauce-labs-bike-light"));
			clickedOne.click();
			WebElement clickedTwo = current.cd.findElement(By.id("add-to-cart-sauce-labs-backpack"));
			clickedTwo.click();
			
			current.cd.findElement(By.xpath("//a[@data-test=\"shopping-cart-link\"]")).click();
			//the checkout function to be simplified!!
			current.cd.findElement(By.id("checkout")).click();		
			current.cd.findElement(By.id("first-name")).sendKeys("Mike");	
			current.cd.findElement(By.id("last-name")).sendKeys("Tyson");	
			current.cd.findElement(By.id("postal-code")).sendKeys("299117");	
			current.cd.findElement(By.id("continue")).click();
			current.cd.findElement(By.id("finish")).click();
			
		} catch(Exception e) {
			test5.fail("test execution failed because " + e.getMessage());
			test5.generateLog(Status.FAIL,"log for test5");
		}
		
		
		
		
	}
	
	@Test
	public  void loginTest6() {
		//P, L verify sum
		//U1 {lisa Simpson, 1289}
		ExtentTest test6 = ReportManager.testGenerator("Sum and price changes verification test", "test goal: verify the prices dont change throughout the purchase pages, and the final sum of three items");
		
		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
		try {
			current.cd.findElement(By.id("user-name")).sendKeys("standard_user");
			current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
			current.cd.findElement(By.id("login-button")).click();
			
			WebElement clicked1 = current.cd.findElement(By.id("add-to-cart-sauce-labs-backpack"));
			WebElement cprice1 = current.cd.findElement(By.xpath("//*[@id=\"inventory_container\"]/div/div[1]/div[2]/div[2]/div"));
			double d1 = Double.parseDouble(cprice1.getText().substring(1, 6));//extract the price
			System.out.println("price of backpack: "+ d1);
			test6.info("price of backpak: " + d1);
			
			WebElement clicked2 = current.cd.findElement(By.id("add-to-cart-sauce-labs-fleece-jacket"));
			WebElement cprice2 = current.cd.findElement(By.xpath("//*[@id=\"inventory_container\"]/div/div[4]/div[2]/div[2]/div"));
			double d2 = Double.parseDouble(cprice2.getText().substring(1, 6));//extract the price
			System.out.println("price of fleece jacket: "+ d2);
			test6.info("price of fllece jacket: " + d2);
			
			WebElement clicked3 = current.cd.findElement(By.id("add-to-cart-sauce-labs-onesie"));
			WebElement cprice3 = current.cd.findElement(By.xpath("//*[@id=\"inventory_container\"]/div/div[5]/div[2]/div[2]/div"));
			double d3 = Double.parseDouble(cprice3.getText().substring(1, 5));//extract the price
			System.out.println("price of onesie: "+ d3);
			test6.info("price of onesie: " + d3);
			
			clicked1.click();
			clicked2.click();
			clicked3.click();
			
			current.cd.findElement(By.xpath("//a[@data-test=\"shopping-cart-link\"]")).click();
			
			WebElement cprice11 = current.cd.findElement(By.xpath("//*[@id=\"cart_contents_container\"]/div/div[1]/div[3]/div[2]/div[2]/div"));
			double d11 = Double.parseDouble(cprice11.getText().substring(1, 6));//extract the price from cart page
			System.out.println("price in the cart of backpack: "+ d11);
			test6.info("price in the cart of th backpack: " + d11);
			
			WebElement cprice22 = current.cd.findElement(By.xpath("//*[@id=\"cart_contents_container\"]/div/div[1]/div[4]/div[2]/div[2]/div"));
			double d22 = Double.parseDouble(cprice22.getText().substring(1, 6));//extract the price from cart page
			System.out.println("price in the cart of fleecejackt: "+ d22);
			test6.info("price in the cart of fleecejackt: "+ d22);
			
			WebElement cprice33 = current.cd.findElement(By.xpath("//*[@id=\"cart_contents_container\"]/div/div[1]/div[5]/div[2]/div[2]/div"));
			double d33 = Double.parseDouble(cprice33.getText().substring(1, 5));//extract the price from cart page
			System.out.println("price in the cart of onsie: "+ d33);
			test6.info("price in the cart of onsie: "+ d33);
			
			//the checkout function to be simplified!!
			current.cd.findElement(By.id("checkout")).click();
			current.cd.findElement(By.id("first-name")).sendKeys("Lisa");	
			current.cd.findElement(By.id("last-name")).sendKeys("Simpson");	
			current.cd.findElement(By.id("postal-code")).sendKeys("1289");	
			current.cd.findElement(By.id("continue")).click();
			
			WebElement cprice111 = current.cd.findElement(By.xpath("//*[@id=\"checkout_summary_container\"]/div/div[1]/div[3]/div[2]/div[2]/div"));
			double d111 = Double.parseDouble(cprice111.getText().substring(1, 6));//extract the price from overview page
			System.out.println("price in overview of backpack: "+ d111);
			test6.info("price in overview of backpack: "+ d111);
			
			WebElement cprice222 = current.cd.findElement(By.xpath("//*[@id=\"checkout_summary_container\"]/div/div[1]/div[4]/div[2]/div[2]/div"));
			double d222 = Double.parseDouble(cprice222.getText().substring(1, 6));//extract the price from overview page
			System.out.println("price in overview of fleece jacket: "+ d222);
			test6.info("price in overview of fleece jacket: "+ d222);
			
			WebElement cprice333 = current.cd.findElement(By.xpath("//*[@id=\"checkout_summary_container\"]/div/div[1]/div[5]/div[2]/div[2]/div"));
			double d333 = Double.parseDouble(cprice333.getText().substring(1, 5));//extract the price from overview page
			System.out.println("price in overview of onesie: "+ d333);
			test6.info("price in overview of onesie: "+ d333);
			
			WebElement priceTotal = current.cd.findElement(By.xpath("//*[@id=\"checkout_summary_container\"]/div/div[2]/div[6]"));
			double dTotal = Double.parseDouble(priceTotal.getText().substring(13, (priceTotal.getText()).length()));//extract the price from overview page
			System.out.println("total price displayed : "+ dTotal);
			double sum = d1+d2+d3;
			System.out.println("total price calculated :"+ sum);
			test6.info("total price displayed : "+ dTotal);
			test6.info("total price calculated :"+ sum);


			
			//checking changing prices
			boolean c3 = true;
			boolean  c2 = true;
			boolean c1 = true;
			boolean coherence = true;
			if ((d1 != d11) || (d1 != d111) ) {
				System.out.println("first price changed");
				test6.info("first price changed");
				c1 = false;
			}
			if ((d2 != d22) && (d2 != d222)) {
				System.out.println("second price changed");
				test6.info("second price changed");
				c2 = false;
			}
			if ((d3 != d33) && (d3 != d333)) {
				System.out.println("third price changed");
				test6.info("third price changed");
				c3 = false;
			}
			if (!c1 || !c2 || !c3) 
				coherence = false;
			if (coherence)
			System.out.println("prices remained unchanged");
			test6.info("prices remained unchanged");
			
		} catch(Exception e) {
			test6.fail("test6 failed because : " + e.getMessage());
		}
		
		
	
	
		
			
		
		
		
		
		
		
		
		
		
		
	}
//	@Test
//	public  void loginTest7() {
// 		ReportManager.testGenerator("testNameHeretoo", "test decription here").info("more details");
//		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//		
//		current.cd.findElement(By.id("user-name")).sendKeys("some_user");
//		current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
//		current.cd.findElement(By.id("login-button")).click();
//	}
//	
//	@Test
//	public  void loginTest8() {
	//ReportManager.testGenerator("testNameHeretoo", "test decription here").info("more details");
//		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//		
//		current.cd.findElement(By.id("user-name")).sendKeys("some_user");
//		current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
//		current.cd.findElement(By.id("login-button")).click();
//	}
//	
//	@Test
//	public  void loginTest9() {
//		ReportManager.testGenerator("testNameHeretoo", "test decription here").info("more details");
//		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//		
//		current.cd.findElement(By.id("user-name")).sendKeys("some_user");
//		current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
//		current.cd.findElement(By.id("login-button")).click();
//	}
//	
//	@Test
//	public  void loginTest10() {
//		ReportManager.testGenerator("testNameHeretoo", "test decription here").info("more details");
//		current.cd.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
//		
//		current.cd.findElement(By.id("user-name")).sendKeys("some_user");
//		current.cd.findElement(By.id("password")).sendKeys("secret_sauce");
//		current.cd.findElement(By.id("login-button")).click();
//	}
//	
	
	
	@AfterMethod(alwaysRun = true)
	public static void tearDown(ITestResult testResult) throws IOException {
		
		 
		String filename = testResult.getMethod().getMethodName() + ".png";
		String dir = System.getProperty("user.dir")	+ "//SCREENSHOTS//";
		System.out.println(dir);
		File sourcefile = ((TakesScreenshot)current.cd).getScreenshotAs(OutputType.FILE);
		FileUtils.copyFile(sourcefile, new File(dir + filename));
		
		ReportManager.reportFlush();
		
		//important not to close the driver, otherwise invalid id session
		
		
		
//		//IMPORTANT: ADD report flush
		//IMPORTANT: comments to the report for each test
	 }
		
		
}

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		
//		setUp();
//		loginTest1();
//		
//
//	}


