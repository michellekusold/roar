package com.osu.kusold.roar.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class Roar_Create_Account extends ActivityInstrumentationTestCase2 {
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
    public Roar_Create_Account() throws ClassNotFoundException {
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
        //Click on Empty Text View
		solo.clickOnView(solo.getView("email"));
        //Enter the text: 'qp@ty.com'
		solo.clearEditText((android.widget.EditText) solo.getView("email"));
		solo.enterText((android.widget.EditText) solo.getView("email"), "qp@ty.com");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("password"));
        //Enter the text: 'qptyqpty'
		solo.clearEditText((android.widget.EditText) solo.getView("password"));
		solo.enterText((android.widget.EditText) solo.getView("password"), "qptyqpty");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("password_confirm"));
        //Enter the text: 'qptyqpty'
		solo.clearEditText((android.widget.EditText) solo.getView("password_confirm"));
		solo.enterText((android.widget.EditText) solo.getView("password_confirm"), "qptyqpty");
        //Click on Register
		solo.clickOnView(solo.getView("email_sign_up_button"));
        //Wait for activity: 'com.osu.kusold.roar.CreateProfileActivity'
		assertTrue("CreateProfileActivity is not found!", solo.waitForActivity("CreateProfileActivity"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("create_profile_name"));
        //Enter the text: 'Qpty'
		solo.clearEditText((android.widget.EditText) solo.getView("create_profile_name"));
		solo.enterText((android.widget.EditText) solo.getView("create_profile_name"), "Qpty");
        //Click on Qpty
		solo.clickOnView(solo.getView("create_profile_name"));
        //Enter the text: '112'
		solo.clearEditText((android.widget.EditText) solo.getView("numberpicker_input"));
		solo.enterText((android.widget.EditText) solo.getView("numberpicker_input"), "112");
        //Set default small timeout to 11373 milliseconds
		Timeout.setSmallTimeout(11373);
        //Click on Other
		solo.clickOnView(solo.getView("genderOptions"));
        //Click on Female
		solo.clickOnView(solo.getView(android.R.id.text1, 1));
        //Click on Enter
		solo.clickOnView(solo.getView("create_profile_submit"));
        //Wait for activity: 'com.osu.kusold.roar.EventFeedActivity'
		assertTrue("EventFeedActivity is not found!", solo.waitForActivity("EventFeedActivity"));
	}
}
