package com.osu.kusold.roar.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class Edit_Profile extends ActivityInstrumentationTestCase2 {
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
    public Edit_Profile() throws ClassNotFoundException {
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
        //Set default small timeout to 21324 milliseconds
		Timeout.setSmallTimeout(21324);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on Profile
		solo.clickOnView(solo.getView("list_item", 2));
        //Click on edit
		solo.clickOnView(solo.getView("editProfile"));
        //Wait for activity: 'com.osu.kusold.roar.CreateProfileActivity'
		assertTrue("CreateProfileActivity is not found!", solo.waitForActivity("CreateProfileActivity"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("create_profile_name"));
        //Enter the text: 'J'
		solo.clearEditText((android.widget.EditText) solo.getView("create_profile_name"));
		solo.enterText((android.widget.EditText) solo.getView("create_profile_name"), "J");
        //Enter the text: '101'
		solo.clearEditText((android.widget.EditText) solo.getView("numberpicker_input"));
		solo.enterText((android.widget.EditText) solo.getView("numberpicker_input"), "101");
        //Click on Other
		solo.clickOnView(solo.getView("genderOptions"));
        //Click on Male
		solo.clickOnView(solo.getView(android.R.id.text1, 2));
        //Click on Enter
		solo.clickOnView(solo.getView("create_profile_submit"));
	}
}
