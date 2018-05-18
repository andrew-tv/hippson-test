package agency.july.flow;

import static agency.july.logger.Logevent.*;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import agency.july.config.models.Accesses;
import agency.july.config.models.Configuration;
import agency.july.sendmail.ImapClient;
import agency.july.webelements.Element;
import agency.july.webelements.Selection;
import agency.july.webelements.TextInput;

public class User extends Test {
	
	protected String userEmail = "";
	protected String userFullName = "";
	protected String userFirstName = "";
	protected String userLastName = "";
	protected String userPasswd = "";
	protected String userTel = "";
	
	// Explore page
//	protected Element profileBtn;
	
	// Login page
	private TextInput emailIn;
	private TextInput passwordIn;
	private Element submitBtn;

	// Register page
	private TextInput yourEmailIn;
	private TextInput firstNameIn;
	private TextInput lastNameIn;
	private TextInput passwordNewIn;
	private TextInput passwordConfirmIn;
	private TextInput telephoneIn;
	private Selection placeSel;
	private Element termsAccepted;
	private Element registerSubmit;
	private Element gotoStartPage;
	
	public User(Flow flow) {
		super(flow);
		
		// Login page
		emailIn = new TextInput(this.flow, By.cssSelector (Configuration.getCsss().get("loginpage").get("email")));
		passwordIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("loginpage").get("password")));
		submitBtn = new Element(this.flow, By.cssSelector(Configuration.getCsss().get("loginpage").get("submit")));
		// Register page
		firstNameIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("firstNameIn")));
		yourEmailIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("yourEmailIn")));
		lastNameIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("lastNameIn")));
		passwordNewIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("passwordNewIn")));
		passwordConfirmIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("passwordConfirmIn")));
		telephoneIn = new TextInput(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("telephoneIn")));
		placeSel = new Selection(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("placeSel")));
		termsAccepted = new Element(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("termsAccepted")));
		registerSubmit = new Element(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("registerSubmit")));
		gotoStartPage = new Element(this.flow, By.cssSelector(Configuration.getCsss().get("registerpage").get("gotoStartPage")));
	}
	
	public User withUser(String user) {
		this.userEmail = Accesses.getLogins().get(user);
		this.userPasswd = Accesses.getPasswds().get(user);
		this.userFullName = Accesses.getUsernames().get(user);
		String[] fullName = this.userFullName.split(" +");
		this.userFirstName = fullName[0];
		this.userLastName = fullName[1];
		this.userTel = "+380501111111";
		return this;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void login () {
		
		ACTION.writeln("Login user : " + this.userEmail);		
		flow.setDriver(driver);
		
		try {
			driver.get(getBaseUrl() + "/login");
			
			emailIn.set(getUserEmail());
			passwordIn.set(userPasswd);		
			submitBtn.click();
	    
		} catch (TestFailedException e) {
			throw new TestFailedException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new TestFailedException();
		}
	}

	public void register () {
		
		ACTION.writeln("Register an user : " + this.userEmail);
		flow.setDriver(driver);
		driver.get(Accesses.getUrls().get("dev") + "/register/");
		WebDriverWait wait = new WebDriverWait(driver, 10);

		try {
				
			firstNameIn.set(userFirstName);
			lastNameIn.set(userLastName);
			yourEmailIn.set(userEmail);
			passwordNewIn.set(userPasswd);
			passwordConfirmIn.set(userPasswd);
			telephoneIn.set(userTel);
			placeSel.set("w");
			
			driver.findElement(termsAccepted.getBy()).click();
//			termsAccepted.click();
			registerSubmit.click();
			
			wait.until( ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.page__description > strong")) );
			wait.until( ExpectedConditions.textToBe(By.cssSelector("div.page__description>strong"), userEmail) );			
			
			// Check email
			driver.get( getToken(
				Accesses.getLogins().get("noreply"),	// from
				this.userEmail,								// to
				Configuration.getCsss().get("confirmlinks").get("registration") // confirm link in the letter
			));
			
			try {
				wait.until( ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.main__thin-center > a")) );
				PASSED.writeln("Thankyou page of registration has been reached");
			} catch (TimeoutException e1) {
				FAILED.writeln("Thankyou page  of registration has not been reached");
//				throw new TestFailedException();
			}
			
			gotoStartPage.click();
			
		} catch (TestFailedException e) {
			throw new TestFailedException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new TestFailedException();
		}
	}

	protected String getToken (String sender, String recipient, String selector) throws InterruptedException {
		String tokenURL = "";
		ACTION.writeln("Waiting for email in \"" + Accesses.getEmail().getFolder() + "\" folder >> ");
		for (int i = 0; i < 5; i++) { // Waiting for email
			Thread.sleep(5000);
		    ImapClient imapClient = new ImapClient (Accesses.getEmail());
		    
			tokenURL = imapClient.getHref( sender, recipient, selector );
			
	    	imapClient.close();
			if ( !tokenURL.isEmpty() ) break;
		}
		if ( tokenURL.isEmpty() ) { // The email has not been received
			FAILED.writeln("The token has not been received in the email or the time was out." 
					+ "\n\tSender: " + sender
					+ "\n\tRecipient: " + recipient
					+ "\n\tCss selector of confirm link: " + selector);
//			throw new TestFailedException();
		} else {
			PASSED.writeln("The token has been received: " + tokenURL);
		}
		return tokenURL;
	}
		
    public void teardown () {
    	driver.get(getBaseUrl() + "/logout");
    	driver.quit();
    }

}
