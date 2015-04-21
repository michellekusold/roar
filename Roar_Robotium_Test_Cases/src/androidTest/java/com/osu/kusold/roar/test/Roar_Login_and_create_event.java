package com.osu.kusold.roar.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class Roar_Login_and_create_event extends ActivityInstrumentationTestCase2 {
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
    public Roar_Login_and_create_event() throws ClassNotFoundException {
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
        //Click on Sort By Nearest
		solo.clickOnView(solo.getView("geo_sort"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("action_create_event"));
        //Wait for activity: 'com.osu.kusold.roar.CreateEventActivity'
		assertTrue("CreateEventActivity is not found!", solo.waitForActivity("CreateEventActivity"));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("create_event_name"));
        //Enter the text: 'Robotium Test'
		solo.clearEditText((android.widget.EditText) solo.getView("create_event_name"));
		solo.enterText((android.widget.EditText) solo.getView("create_event_name"), "Robotium Test");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etVenue"));
        //Set default small timeout to 36396 milliseconds
		Timeout.setSmallTimeout(36396);
        //Enter the text: 'Location For Robotium '
		solo.clearEditText((android.widget.EditText) solo.getView("etVenue"));
		solo.enterText((android.widget.EditText) solo.getView("etVenue"), "Location For Robotium ");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etAddressLine1"));
        //Set default small timeout to 44944 milliseconds
		Timeout.setSmallTimeout(44944);
        //Enter the text: '2015 Neil Avenue'
		solo.clearEditText((android.widget.EditText) solo.getView("etAddressLine1"));
		solo.enterText((android.widget.EditText) solo.getView("etAddressLine1"), "2015 Neil Avenue");
        //Click on Location For Robotium
		solo.clickOnView(solo.getView("etVenue"));
        //Enter the text: 'Dreese Laboratory '
		solo.clearEditText((android.widget.EditText) solo.getView("etVenue"));
		solo.enterText((android.widget.EditText) solo.getView("etVenue"), "Dreese Laboratory ");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etCity"));
        //Enter the text: 'Columbus'
		solo.clearEditText((android.widget.EditText) solo.getView("etCity"));
		solo.enterText((android.widget.EditText) solo.getView("etCity"), "Columbus");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etZip"));
        //Enter the text: '43210'
		solo.clearEditText((android.widget.EditText) solo.getView("etZip"));
		solo.enterText((android.widget.EditText) solo.getView("etZip"), "43210");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etCost"));
        //Enter the text: '50'
		solo.clearEditText((android.widget.EditText) solo.getView("etCost"));
		solo.enterText((android.widget.EditText) solo.getView("etCost"), "50");
        //Click on Arts and Culture
		solo.clickOnView(solo.getView("categoryOptions"));
        //Click on Campus
		solo.clickOnView(solo.getView(android.R.id.text1, 3));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etMaxAttendance"));
        //Enter the text: '3'
		solo.clearEditText((android.widget.EditText) solo.getView("etMaxAttendance"));
		solo.enterText((android.widget.EditText) solo.getView("etMaxAttendance"), "3");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etEventDescription"));
        //Enter the text: 'Test case for robotium '
		solo.clearEditText((android.widget.EditText) solo.getView("etEventDescription"));
		solo.enterText((android.widget.EditText) solo.getView("etEventDescription"), "Test case for robotium ");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etPhonNumber"));
        //Enter the text: '1111111111'
		solo.clearEditText((android.widget.EditText) solo.getView("etPhonNumber"));
		solo.enterText((android.widget.EditText) solo.getView("etPhonNumber"), "1111111111");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("etEmailAddress"));
        //Enter the text: 'i@i.com'
		solo.clearEditText((android.widget.EditText) solo.getView("etEmailAddress"));
		solo.enterText((android.widget.EditText) solo.getView("etEmailAddress"), "i@i.com");
        //Click on Enter
		solo.clickOnView(solo.getView("create_event_submit"));
	}
}
