package com.osu.kusold.roar.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class Login_exit_reopen extends ActivityInstrumentationTestCase2 {
  	private Solo solo;
  	
  	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "com.osu.kusold.roar.SplashScreenActivity";

    private static Class<?> launcherActivityClass;
    static{
        try {
            launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
    }
  	
  	@SuppressWarnings("unchecked")
    public Login_exit_reopen() throws ClassNotFoundException {
        super(launcherActivityClass);
    }

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}
  
   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}
  
	public void testRun() {
        //Wait for activity: 'com.osu.kusold.roar.SplashScreenActivity'
		solo.waitForActivity("SplashScreenActivity", 2000);
        //Wait for activity: 'com.osu.kusold.roar.SignUpActivity'
		assertTrue("SignUpActivity is not found!", solo.waitForActivity("SignUpActivity"));
        //Set default small timeout to 12191 milliseconds
		Timeout.setSmallTimeout(12191);
        //Click on Sign in
		solo.clickOnView(solo.getView("email_sign_in_button"));
        //Wait for activity: 'com.osu.kusold.roar.LoginActivity'
		assertTrue("LoginActivity is not found!", solo.waitForActivity("LoginActivity"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("email"));
        //Enter the text: 'i@i.com'
		solo.clearEditText((android.widget.EditText) solo.getView("email"));
		solo.enterText((android.widget.EditText) solo.getView("email"), "i@i.com");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("password_confirm"));
        //Enter the text: 'iiiiiiii'
		solo.clearEditText((android.widget.EditText) solo.getView("password_confirm"));
		solo.enterText((android.widget.EditText) solo.getView("password_confirm"), "iiiiiiii");
        //Click on Sign in
		solo.clickOnView(solo.getView("email_sign_in_button"));
        //Wait for activity: 'com.osu.kusold.roar.LoginActivity'
		assertTrue("LoginActivity is not found!", solo.waitForActivity("LoginActivity"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("password_confirm"));
        //Enter the text: 'iiiiiiii'
		solo.clearEditText((android.widget.EditText) solo.getView("password_confirm"));
		solo.enterText((android.widget.EditText) solo.getView("password_confirm"), "iiiiiiii");
        //Click on Sign in
		solo.clickOnView(solo.getView("email_sign_in_button"));
        //Wait for activity: 'com.osu.kusold.roar.EventFeedActivity'
		assertTrue("EventFeedActivity is not found!", solo.waitForActivity("EventFeedActivity"));
        //Wait for activity: 'com.osu.kusold.roar.SplashScreenActivity'
		assertTrue("SplashScreenActivity is not found!", solo.waitForActivity("SplashScreenActivity"));
        //Wait for activity: 'com.osu.kusold.roar.EventFeedActivity'
		assertTrue("EventFeedActivity is not found!", solo.waitForActivity("EventFeedActivity"));
	}
}
