# Screenshot Stitching example on TestObject

This script will visit a list of websites on a TestObject iOS device, and save a "stitched screenshot" of each one, saving the results
locally.

# Instructions

The test reads environment variables for configuration. The following are required:

* `TESTOBJECT_API_KEY`
* `TESTOBJECT_APP_ID`

The following environment variables are optional:

* `TESTOBJECT_DEVICE` (default is `iPhone_6S_Plus_16GB_real_2`)
* `TESTOBJECT_APPIUM_VERSION` (default is `1.4.16`)
* `WEBSITE_LIST_FILE` (default is `websites.txt`)

After setting these environment variables, run `gradle clean test`, or configure your CI to do so. The test will run through each URL
in `websites.txt` (a newline separated list), and save each screenshot to `iPhone_6S_Plus_16GB_real_2/http.www.example.com/restofurl.png`

# Notes

* To crop out the top and bottom Safari UI elements of the screenshot, hardcoded values are used on our backend for now, which are designed
to work with an iPhone 6S Plus. Using this command on other devices may provide unexpected results.
* Some websites have a header or footer which is fixed in position. We make no attempt to mitigate this, and as a result you'll likely see
the header/footer element repeated in each screenshot.