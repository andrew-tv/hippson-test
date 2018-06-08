package agency.july.flow;

import static agency.july.logger.Logevent.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import agency.july.config.models.Accesses;
import agency.july.config.models.Configuration;
import agency.july.webelements.Element;
import agency.july.webelements.TextInput;

public class Admin extends User {
	
	// Admin page
	
	// Moderation page	    
	private Element acceptedModeration;	
	private Element saveChangesModeration;	

	public Admin(Flow flow) {
		super(flow);
	}
	
	public Admin withUser(String user) {
		this.userEmail = Accesses.getLogins().get(user);
		this.userPasswd = Accesses.getPasswds().get(user);
		this.userFullName = Accesses.getUsernames().get(user);
		String[] fullName = this.userFullName.split(" +");
		this.userFirstName = fullName[0];
		this.userLastName = fullName[1];
		this.userTel = "+380501111111";
		return this;
	}
	
	public void confirmAction() {
		Element confirmActionBtn = new Element(this.flow, By.cssSelector(Configuration.getCsss().get("batchaction").get("confirmActionBtn")));
		confirmActionBtn.click();
	}

	public void removeUser(String userEmail) {
		
		ACTION.writeln("Remove a user : " + userEmail);		
		flow.setDriver(driver);
		
		driver.get(getBaseUrl() + "/admin/hippsonuser/user/list");
		List <WebElement> emails = driver.findElements(By.cssSelector(Configuration.getCsss().get("adminpage").get("items")));
		
		for (int i=0; i<emails.size(); i++) {
			if ( emails.get(i).getText().contains(userEmail) ) {
				driver.get( emails.get(i).getAttribute("href").replaceAll("/edit$", "/delete") ); // Delete user by direct link
				confirmAction();
				break;
			}
		}
	}

	public void confirmModeration() {
		flow.setDriver(driver);

		// Check email
		try {
			driver.get( getToken(
				Accesses.getLogins().get("noreply"),	// from
				this.userEmail,							// to
				Configuration.getCsss().get("confirmlinks").get("moderation") // confirm link in the letter
			));
			
			acceptedModeration.click();
			saveChangesModeration.click();
			
		} catch (WebDriverException e) {
			FAILED.writeln("The URL for moderation a new campaign is wrong");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
