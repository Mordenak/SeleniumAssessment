package BuildProject.SeleniumAssessment;

import junit.framework.TestCase;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import static org.assertj.core.api.Assertions.*;

public class InitialTest 
    extends TestCase
{
    
    public void testApp()
    {
    	// Data Expectations:
    	
    	// Product List:
    	String[] productList = {
    			"Suede Kohler K-6626-6U",
    			"Cashmere Kohler K-6626-6U",
    			"Kohler K-5180-ST"
    	};
    	
    	// Numbers to verify:
    	String expectedTax = "$90.24";
    	String expectedTotal = "$1,334.86";
    	
    	
    	/*
    	 * Works against Chrome v60 and Firefox v55.0.2 - As always, IE is more erratic, slower and often triggers
    	 * the Robot CAPTCHA page, YMMV.
    	 */
    	File file = new File("C:/WebDrivers/IEDriverServer.exe");
    	System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
    	
    	// Shortcut would be to set up filtering and let Maven substitute this for us...
    	WebDriver driver = new ChromeDriver();
    	
    	
        if (driver instanceof FirefoxDriver) {
        	driver.manage().timeouts().pageLoadTimeout(60,TimeUnit.SECONDS);
        }
        
        
        driver.get("http://www.build.com");
        
        if (driver instanceof InternetExplorerDriver) {
        	// Very long start up time, have to wait......
        	try {
        		Thread.sleep(5000);
        	} catch (InterruptedException e) {
        	};
        }
        
        WebElement popup = (new WebDriverWait(driver, 20))
        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.external-close")));
        
 	    // Dismiss the email address popup
        popup.click();
        
        productLookupAndAdd(driver, productList[0], 1);
        productLookupAndAdd(driver, productList[1], 1);
        productLookupAndAdd(driver, productList[2], 2);
        
        // Verify Cart count of 4 -- Beware, when element is ready, it is not updated with correct cart count...
        
        // Cart and proceed to Checkout
        // driver.findElement(By.cssSelector("span.cart-text")).click();
        // We don't actually need to click the cart because the last product put us there anyways.
        
        // Verify on Checkout page
        WebElement checkoutButton = (new WebDriverWait(driver, 20))
        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.btn-group.right.js-continue-shopping-button > a")));
        
        // Verify cart count of 4 now that page is fully loaded.
        String cartQuantity = driver.findElement(By.cssSelector("span.header-cart-quantity-amount.js-cart-quantity")).getText();
        
        assertThat("4").isEqualTo(cartQuantity);
        
        checkoutButton.click();
        
        // Checkout as Guest
        WebElement guestCheckout = (new WebDriverWait(driver, 20))
        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[name=guestLoginSubmit]")));
        
        guestCheckout.click();
        
        fillOutCheckout(driver);
        
        // Verify Tax & Grand Total
        WebElement taxField = (new WebDriverWait(driver, 20))
        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#taxAmount")));
        
        String tax = taxField.getText();
        
        String total = driver.findElement(By.cssSelector("#grandtotalamount")).getText();
        
        assertThat(expectedTax).isEqualTo(tax);
        
        assertThat(expectedTotal).isEqualTo(total);
        
         driver.close();
 	}
    
    public void productLookupAndAdd(WebDriver driver, String productName, int quantity) {
    	// Re-assign SearchBar to avoid Stale Element
        WebElement searchBar = driver.findElement(By.cssSelector("input#search_txt"));
        
        // Enter product to search for
        searchBar.sendKeys(productName);
        
        searchBar.submit();
        
        // Slowdown for Firefox as Implicit waits struggling w/ the JS.
        if (driver instanceof FirefoxDriver) {
        	try {
        		Thread.sleep(2000);
        	} catch (InterruptedException e) {
        	};
        }
                
        // Adjust Quantity
        WebElement quantityInput = (new WebDriverWait(driver, 20))
        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#main-product-quantity > div > input")));
        
        // Firefox workarounds
        if (driver instanceof FirefoxDriver) {
        	quantityInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        	quantityInput.sendKeys(Integer.toString(quantity));
        } else {
        	quantityInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Integer.toString(quantity));
        }
        
        // Slowdown for Firefox as Implicit waits struggling w/ the JS.
        if (driver instanceof FirefoxDriver) {
        	try {
        		Thread.sleep(2000);
        	} catch (InterruptedException e) {
        	};
        }
        
        // https://github.com/mozilla/geckodriver/issues/322
        // Add item to Cart
        // Surrounding div ID varied between product pages, used Button classes instead
        driver.findElement(By.cssSelector("button.btn-standard.add-to-cart.js-add-to-cart")).click();
        
        // Verify item was added -- EXCEPT for the Kohler K-5180-ST, which seems to redirect directly to the Shopping Cart anyway...
        if (productName != "Kohler K-5180-ST") {
	        WebElement itemHeader = (new WebDriverWait(driver, 20))
	        		.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.heading-s.margin-top-none.product-added-title")));
	        
	        String headerText = itemHeader.getText();
	        assertThat("Product Added to Cart").isEqualTo(headerText);
        }
    }
    
    public void fillOutCheckout(WebDriver driver) {
    	driver.findElement(By.cssSelector("#shippingfirstname")).sendKeys("Foo");
    	driver.findElement(By.cssSelector("#shippinglastname")).sendKeys("Bar");
    	
    	driver.findElement(By.cssSelector("#shippingaddress1")).sendKeys("402 Otterson Dr");
    	driver.findElement(By.cssSelector("#shippingpostalcode")).sendKeys("95928");
    	driver.findElement(By.cssSelector("#shippingcity")).sendKeys("Chico");
    	
    	new Select(driver.findElement(By.cssSelector("#shippingstate_1"))).selectByVisibleText("California");
    	driver.findElement(By.cssSelector("#shippingphonenumber")).sendKeys("530-867-5309");
    	
    	driver.findElement(By.cssSelector("#emailAddress")).sendKeys("automation@mailinator.com");
    	
    	driver.findElement(By.cssSelector("#creditCardNumber")).sendKeys("4111111111111111");
    	new Select(driver.findElement(By.cssSelector("#creditCardMonth"))).selectByVisibleText("09");
    	new Select(driver.findElement(By.cssSelector("#creditCardYear"))).selectByVisibleText("2018");
    	driver.findElement(By.cssSelector("#creditcardname")).sendKeys("Selenium Automation");
    	driver.findElement(By.cssSelector("#creditCardCVV2")).sendKeys("867");
    	
    	driver.findElement(By.cssSelector("#creditCardCVV2")).submit();
    }
}
