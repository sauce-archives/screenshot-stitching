# Screenshot Stitching example on TestObject

This script will visit a list of websites on a TestObject iOS device, and save a "stitched screenshot" of each one, saving the results
locally.

# Instructions

The test reads environment variables for configuration:

* `TESTOBJECT_API_KEY` (**required**)
* `TESTOBJECT_APP_ID` (default is `1`, match this up to the ID in the App Versions section of your app on TestObject)
* `TESTOBJECT_DEVICE` (default is `iPhone_6S_Plus_16GB_real_ABP_off_private`)
* `TESTOBJECT_APPIUM_VERSION` (default is `1.4.16`)
* `WEBSITE_LIST_FILE` (default is `websites.txt`)

## Testing with ABP off

The defaults are set to test with ABP off. You are still free to modify settings via environment variables if you wish.

## Testing with ABP on

* Set `TESTOBJECT_APP_ID` to comma separated list of values, the first element of which is the ID of your web test, and the second of which is the ID of the
Adblock Plus app, which will be installed as a dependency. For example, `TESTOBJECT_APP_ID=1,2`
* Set `TESTOBJECT_DEVICE` to `iPhone_6S_Plus_16GB_real_ABP_on_private`

## Running test

After setting these environment variables, run `gradle clean test`, or configure your CI to do so. The test will run through each URL
in `websites.txt` (a newline separated list), and save each screenshot to `iPhone_6S_Plus_16GB_real_ABP_off_private/http.www.example.com/restofurl.png`

# Notes

* To crop out the top and bottom Safari UI elements of the screenshot, hardcoded values are used on our backend for now, which are designed
to work with an iPhone 6S Plus. Using this command on other devices may provide unexpected results.
* Some websites have a header or footer which is fixed in position. We make no attempt to mitigate this, and as a result you'll likely see
the header/footer element repeated in each screenshot.