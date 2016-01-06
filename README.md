# adblock-screenshot-stitching
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