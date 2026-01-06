package com.simplifyqa.codeeditor;

import com.simplifyqa.pluginbase.codeeditor.annotations.AutoInjectCurrentObject;
import com.simplifyqa.pluginbase.common.models.Attribute;
import com.simplifyqa.pluginbase.common.models.SqaObject;
import com.simplifyqa.pluginbase.argument.IArgument;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.json.JSONArray;
import com.simplifyqa.pluginbase.codeeditor.annotations.AutoInjectExecutionLogger;
import com.simplifyqa.pluginbase.plugin.execution.IExecutionLogReporter;
import com.simplifyqa.web.base.search.FindBy;
import com.simplifyqa.pluginbase.codeeditor.annotations.AutoInjectWebDriver;
import com.akiban.sql.parser.ParseException;
import com.github.javafaker.Faker;
import com.simplifyqa.abstraction.driver.IQAWebDriver;
import com.simplifyqa.abstraction.element.IQAWebElement;
import com.simplifyqa.pluginbase.common.enums.TechnologyType;
import com.simplifyqa.pluginbase.plugin.annotations.ObjectTemplate;
import com.simplifyqa.pluginbase.codeeditor.annotations.SyncAction;

/**
 * Hello there!!, please keep the following things in mind while creating custom
 * class.
 * Your class should have a public default constructor.
 * 
 * @SyncAction methods should be public and return a boolean value.
 *             uniqueId field in @SyncAction annotation should be unique
 *             throughout the project.
 */

public class SampleClass {
    @AutoInjectCurrentObject
    private SqaObject currentObject;
    @AutoInjectWebDriver
    private IQAWebDriver driver;
    private static final Logger log = Logger.getLogger(SampleClass.class.getName());
    @AutoInjectCurrentObject
    private SqaObject currentStepObject;
    @AutoInjectExecutionLogger
    private IExecutionLogReporter logReporter;

    public SampleClass() {

    }

    public String getObjectProperty(String name) {
        try {
            List<Attribute> attributes = this.currentObject.attributes();
            for (Attribute attribute : attributes) {
                if (attribute.name().equals(name))
                    return attribute.value();
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error(e.toString());
        }
        return null;
    }

    public String generateRandomString(String length) {
        String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
        int LENGTH = Integer.parseInt(length);
        StringBuilder sb = new StringBuilder(LENGTH);
        Random random = new Random();
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    @SyncAction(uniqueId = "custom-web-getUniqueLastName", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Generates a unique last name by combining a given prefix, to ensure it's not duplicated"))
    public boolean getUniqueLastName(String Prefix, IArgument Name) {
        boolean bStatus = false;
        try {
            driver.getExecutionLogReporter().info("Generating unique last name...");
            Faker faker = new Faker();
            String name = faker.name().lastName().toLowerCase().trim().replace("'", "");
            name = Prefix + name + generateRandomString("4");
            name = name.toLowerCase();
            driver.getExecutionLogReporter().info("Generated candidate last name: " + name);
            // sqaobject.getwebobjects().entertext(name);
            driver.findElement(FindBy.xpath("//input[@id='search-input']")).enterText(name);
            driver.getExecutionLogReporter().info("Entered text: " + name);
            driver.findElement(FindBy.xpath("//button[text()='Search']")).click();
            Thread.sleep(1000);
            int count = driver
                    .findElements(FindBy.xpath("//a[contains(@data-bind,'click: $parent.view') and text()='View']"))
                    .size();
            Thread.sleep(500);
            while (!(count == 0)) {
                String newName = faker.name().firstName().toLowerCase().trim();
                newName = Prefix + newName + generateRandomString("4");
                driver.getExecutionLogReporter().info("Name already exists. Trying new candidate: " + newName);
                driver.findElement(FindBy.xpath("//input[@id='search-input']")).enterText(name);
                driver.getExecutionLogReporter().info("Entered text: " + name);
                driver.findElement(FindBy.xpath("//button[text()='Search']")).click();
                Thread.sleep(1000);
                count = driver
                        .findElements(FindBy.xpath("//a[contains(@data-bind,'click: $parent.view') and text()='View']"))
                        .size();
                Thread.sleep(500);
                name = newName;
            }
            Name.updateValue(name);
            driver.getExecutionLogReporter().info("Unique last name stored with key: " + Name + " -> " + name);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in getUniqueLastName: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-getUniqueName", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Generates a unique and realistic-looking name using the Faker library, prefixed with a given value"))
    public boolean getUniqueName(String Prefix, IArgument Name) {
        if (Prefix == null) {
            driver.getExecutionLogReporter().error("Prefix is null. Cannot generate unique name.");
            return false;
        }
        if (Name == null) {
            driver.getExecutionLogReporter().error("Runtime variable to store name is null.");
            return false;
        }
        try {
            String name;
            Faker faker = new Faker();
            name = faker.name().firstName().toLowerCase();
            name = name.replace("'", "");
            String aa = generateRandomString("4");
            name = Prefix + name + aa;
            name = name.toLowerCase();
            Name.updateValue(name);
            driver.getExecutionLogReporter().info("Generated unique name: " + name);
            return true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in getUniqueName: " + e.toString());
            return false;
        }
    }

    @SyncAction(uniqueId = "Xfit-003", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.GENERIC, description = "Generates a string of random characters of a specified length"), objectRequired = false)
    public boolean generateRandomString(String prefix, String length, IArgument store) {
        boolean status = false;
        try {
            String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
            int LENGTH = Integer.parseInt(length);
            StringBuilder sb = new StringBuilder(LENGTH);
            Random random = new Random();

            for (int i = 0; i < LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                char randomChar = CHARACTERS.charAt(index);
                sb.append(randomChar);
            }
            // Combine prefix + random string
            String finalString = prefix + sb.toString();
            // Store in runtime
            store.updateValue(finalString);
            driver.getExecutionLogReporter().info("Generated random string '" + finalString + "' with prefix '" + prefix
                    + "' and stored into runtime variable: " + store);
            status = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Error in generateRandomStringWithPrefix: " + e.toString());
            status = false;
        }
        return status;
    }

    @SyncAction(uniqueId = "custom-web-generateRandomNumber", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Generates random number of specified length"))
    public boolean generateRandomNumber(String count, IArgument Name) {
        boolean bstatus = false;
        try {
            int min = (int) Math.pow(10.0D, (Integer.parseInt(count) - 1));
            int max = (int) Math.pow(10.0D, Integer.parseInt(count));
            Random random = new Random();
            String randomNumber = Integer.toString(random.nextInt(max - min) + min);
            Name.updateValue(randomNumber);
            driver.getExecutionLogReporter()
                    .info("Generated random number: " + randomNumber + "' and stored into runtime variable: " + Name);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Error in generateRandomNumber: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-clickWithCondition", groupName = "Click", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Clicks the element only if the condition is true"))
    public boolean clickWithCondition(String condition) {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            if (driver.findElements(FindBy.xpath(xpath1)).size() == 1) {
                condition = condition.toLowerCase();/// YES,Yes,yes
                if (condition.equals("yes")) {
                    driver.findElement(FindBy.xpath(xpath1)).click();
                    bstatus = true;
                    driver.getExecutionLogReporter().info("Condition is Yes, Checkbox clicked");
                } else if (condition.equals("no")) {
                    driver.getExecutionLogReporter().info("Condition is NO, skipping click");
                    bstatus = true;
                } else {
                    // Invalid input
                    driver.getExecutionLogReporter().info("Invalid condition passed: " + condition);
                    bstatus = false;
                }
            } else {
                bstatus = false;
                driver.getExecutionLogReporter().info("Element not Exist");
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("exception");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-generic-phoneNumberConcate", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Combines three parts of a phone number into a single formatted number"))
    public boolean phoneNumberConcate(String phone1, String phone2, String phone3, IArgument phoneNumber) {   
        if (phone1 == null || phone1.isEmpty() || phone2 == null || phone2.isEmpty() || phone3 == null || phone3.isEmpty()) {
            driver.getExecutionLogReporter().error("One or more phone number parts are null or empty.");
            return false;
        }
        if (phoneNumber == null) {
            driver.getExecutionLogReporter().error("Runtime variable to store phone number is null.");
            return false;
        }
        boolean bstatus = false;
        try {
            String value = "(" + phone1 + ") " + phone2 + "-" + phone3;
            phoneNumber.updateValue(value);
            driver.getExecutionLogReporter().info("Successfully concatenated phone number: " + value);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while concatenating phone number: ");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyDescendingSort", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether the values listed below are arranged in descending order"))
    public boolean verifyDescendingSort() {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            int SearchLength = driver.findElements(FindBy.xpath(xpath1)).size();
            if (SearchLength != 0) {
                List<String> strings = new ArrayList<String>();
                for (int i = 1; i <= SearchLength; i++) {
                    String s1 = "(" + xpath1 + ")[" + i + "]"; // (//div[@class='worklist-name-values']/span)[1]
                    String text = driver.findElement(FindBy.xpath(s1)).getText().toLowerCase().trim();
                    strings.add(text);
                }
                List<String> str1 = new ArrayList<String>(strings);
                Collections.sort(str1);
                Collections.reverse(str1);
                System.out.println(str1);
                boolean b1 = strings.equals(str1);
                if (b1 == true) {
                    bstatus = true;
                    driver.getExecutionLogReporter().info("length count - " + strings.size());
                    driver.getExecutionLogReporter().info("Sort Functionlity Passed");
                } else {
                    bstatus = false;
                    driver.getExecutionLogReporter().info("Sort Functionlity Failed");
                }
            } else {
                driver.getExecutionLogReporter().info("No elements found for given XPath");
                bstatus = false;
            }
        } catch (Exception e) {
            bstatus = false;
            driver.getExecutionLogReporter().info("Sort Functionality Failed");
            driver.getExecutionLogReporter().error(e.toString());
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyAscendingSort", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether the values listed below are arranged in ascending order"))
    public boolean verifyAscendingSort() {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            int SearchLength = driver.findElements(FindBy.xpath(xpath1)).size();
            if (SearchLength != 0) {
                List<String> strings = new ArrayList<String>();
                for (int i = 1; i <= SearchLength; i++) {
                    String s1 = "(" + xpath1 + ")[" + i + "]"; // (//div[@class='worklist-name-values']/span)[1]
                    String text = driver.findElement(FindBy.xpath(s1)).getText().toLowerCase().trim();
                    strings.add(text);
                }
                List<String> str1 = new ArrayList<String>(strings);
                Collections.sort(str1);
                Collections.reverse(str1);
                System.out.println(str1);
                boolean b1 = strings.equals(str1);
                if (b1 == true) {
                    bstatus = true;
                    driver.getExecutionLogReporter().info("length count -" + strings.size());
                    driver.getExecutionLogReporter().info("Sort Functionlity Passed");
                } else {
                    bstatus = false;
                    driver.getExecutionLogReporter().info("Sort Functionlity Failed");
                }
            } else {
                driver.getExecutionLogReporter().info("No elements found for given XPath.");
                bstatus = false;
            }
        } catch (Exception e) {
            bstatus = false;
            driver.getExecutionLogReporter().info("Sort Functionality Failed");
            driver.getExecutionLogReporter().error(e.toString());
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyDateAscendingSort", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether the Date values listed below are arranged in ascending order"))
    public boolean verifyDateAscendingSort() throws ParseException {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            int SearchLength = driver.findElements(FindBy.xpath(xpath1)).size();

            if (SearchLength != 0) {
                List<String> strings = new ArrayList<String>();
                for (int i = 1; i <= SearchLength; i++) {
                    String s1 = "(" + xpath1 + ")[" + i + "]";
                    String text = driver.findElement(FindBy.xpath(s1)).getText().toLowerCase().trim();
                    strings.add(text);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                List<Date> dateList = new ArrayList<Date>();
                for (int i = 0; i < strings.size(); i++) {
                    dateList.add(dateFormat.parse(strings.get(i)));
                }

                List<Date> dateList1 = new ArrayList<Date>(dateList);
                Collections.sort(dateList1);

                boolean b1 = dateList.equals(dateList1);
                if (b1 == true) {
                    bstatus = true;
                    driver.getExecutionLogReporter().info("Dates are sorted in ascending order.");
                } else {
                    driver.getExecutionLogReporter().info("Dates are NOT sorted in ascending order.");
                }
            } else {
                driver.getExecutionLogReporter().info("No date elements found for the given xpath.");
            }
        } catch (Exception e) {
            bstatus = false;
            driver.getExecutionLogReporter().info("Sort Functionality Failed");
            driver.getExecutionLogReporter().error(e.toString());
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyDateDescendingSort", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether the Date values listed below are arranged in descending order"))
    public boolean verifyDateDescendingSort() throws ParseException {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            int SearchLength = driver.findElements(FindBy.xpath(xpath1)).size();

            if (SearchLength != 0) {
                List<String> strings = new ArrayList<String>();
                for (int i = 1; i <= SearchLength; i++) {
                    String s1 = "(" + xpath1 + ")[" + i + "]";
                    String text = driver.findElement(FindBy.xpath(s1)).getText().toLowerCase().trim();
                    strings.add(text);
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                List<Date> dateList = new ArrayList<Date>();
                for (int i = 0; i < strings.size(); i++) {
                    dateList.add(dateFormat.parse(strings.get(i)));
                }
                List<Date> dateList1 = new ArrayList<Date>(dateList);
                Collections.sort(dateList1);
                Collections.reverse(dateList1);
                boolean b1 = dateList.equals(dateList1);
                if (b1 == true) {
                    bstatus = true;
                    driver.getExecutionLogReporter().info("Dates are sorted in descending order.");
                } else {
                    driver.getExecutionLogReporter().info("Dates are NOT sorted in descending order.");
                }
            } else {
                driver.getExecutionLogReporter().info("No date elements found for the given xpath.");
            }
        } catch (Exception e) {
            bstatus = false;
            driver.getExecutionLogReporter().info("Sort Functionality Failed");
            driver.getExecutionLogReporter().error(e.toString());
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-concatefullname", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Combine the first name and last name in the single fullname string"))
    public boolean concatefullname(String lastname, String firstname, IArgument fullname) {
        boolean bstatus = false;
        try {
            String FullName = lastname + ", " + firstname;
            fullname.updateValue(FullName);
            driver.getExecutionLogReporter().info("Fullname concatenated successfully; Fullname : " + FullName);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while concatenating full name.");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-generic-nhgetdate", groupName = "get and store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Gets formatted date with offset"))
    public boolean nhgetdate(String number, String format, IArgument store) {
        boolean bStatus = false;
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);

            // Add days to current date
            cal.add(Calendar.DATE, Integer.parseInt(number));
            String generatedDate = dateFormat.format(cal.getTime());

            // Store and log
            store.updateValue(generatedDate);
            driver.getExecutionLogReporter().info("Generated Date: " + generatedDate);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Date generation failed");
            driver.getExecutionLogReporter().error(e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-dynamicClick", groupName = "Click", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "clicks the target element from within the static element"))
    public boolean dynamicClick(String replaceValue1, String replaceValue2) {
        boolean bstatus = false;
        try {
            if (replaceValue1 != null && replaceValue2 != null) {
                String locatorvalue = getObjectProperty("xpath");
                if (locatorvalue.contains("#replace1") && locatorvalue.contains("#replace2")) {
                    locatorvalue = locatorvalue.replaceAll("#replace1", replaceValue1);
                    locatorvalue = locatorvalue.replaceAll("#replace2", replaceValue2);
                    driver.findElement(FindBy.xpath(locatorvalue)).click();
                    driver.getExecutionLogReporter()
                            .info("Clicked element with dynamic values: " + replaceValue1 + ", " + replaceValue2);
                    // return webdriver.click("xpath", locatorvalue);
                    bstatus = true;
                }
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while performing dynamic click.");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-validateTask", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies the specific task"))
    public boolean validateTask(String description) {
        boolean bStatus = false;
        try {
            String xpathvalue = getObjectProperty("xpath");
            if (!xpathvalue.contains("#replace")) {
                driver.getExecutionLogReporter().info("Locator does not contain #replace placeholder.");
                return bStatus;
            }
            // Replace #replace with actual description value
            xpathvalue = xpathvalue.replaceAll("#replace", description);
            driver.getExecutionLogReporter().info("Validating task with XPath: " + xpathvalue);
            boolean hasNextPage = true;
            boolean taskFound = false;
            while (hasNextPage) {
                int lenCount = driver.findElements(FindBy.xpath(xpathvalue)).size();
                // int lenCount = driver.findElements("xpath", xpathvalue).length();
                if (lenCount == 1) {
                    taskFound = true;
                } else if (lenCount == 0) {
                    taskFound = false;
                } else {
                    driver.getExecutionLogReporter()
                            .error("Multiple elements found for task description: " + description);
                    bStatus = false;
                }
                if (taskFound) {
                    driver.getExecutionLogReporter().info("Task found successfully for description: " + description);
                    bStatus = true;
                    break;
                } else {
                    int nextPageExists = driver.findElements(FindBy.xpath("(//a[text()='Next Page >'])[1]")).size();
                    if (nextPageExists == 1) {
                        driver.getExecutionLogReporter()
                                .info("Task not found on current page. Navigating to next page...");
                        driver.findElement(FindBy.xpath("(//a[text()='Next Page >'])[1]")).click();
                        Thread.sleep(3000);
                    } else {
                        driver.getExecutionLogReporter().info("Task not found and no more pages available.");
                        bStatus = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception occurred in validateTask: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-getPartialText", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Get a portion of the text"))
    public boolean getPartialText(String name, IArgument store) {
        boolean bstatus = false;
        try {
            if (name == null || name.isEmpty()) {
                driver.getExecutionLogReporter().info("Input 'name' is null or empty. Cannot extract partial text.");
                return bstatus;
            }
            int size = name.length() / 2; // Extract first half of the string
            String pname = name.substring(0, size);
            store.updateValue(pname);
            driver.getExecutionLogReporter()
                    .info("Stored partial text '" + pname + "' into runtime variable: " + store);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception occurred in getPartialText: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-016", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verify a partial match of the Medical Record Number"))
    public boolean verifyMRNPartialText(String name) {
        boolean bstatus = false;
        try {
            if (name == null || name.isEmpty()) {
                driver.getExecutionLogReporter().info("Input 'name' is null or empty. Cannot verify MRN partial text.");
                return bstatus;
            }
            int searchLength = driver.findElements(FindBy.xpath("//ul[@class='search-matches']/li")).size();
            if (searchLength == 0) {
                driver.getExecutionLogReporter().info("No search match elements found for MRN verification.");
                return bstatus;
            }
            boolean allMatch = true;
            for (int i = 1; i <= searchLength; i++) {
                String s = driver
                        .findElement(FindBy.xpath(
                                "(//ul[@class='search-matches']/li/div/div[2]/div[2]/div[3]/span/span)[" + i + "]"))
                        .getText();
                driver.getExecutionLogReporter().info("Verifying MRN text for element " + i + ": " + s);
                if (!s.contains(name)) {
                    driver.getExecutionLogReporter()
                            .error("Element " + i + " does not contain expected MRN text: " + name);
                    bstatus = false;
                }
            }
            if (allMatch) {
                driver.getExecutionLogReporter().info("All MRNs contain the expected partial text: " + name);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().info("Some MRNs do not contain the expected partial text: " + name);
                bstatus = false;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception occurred in verifyMRNPartialText: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getValue", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Get the current value"))
    public boolean getValue(IArgument store) {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");

            if (driver.findElements(FindBy.xpath(xpath1)).size() == 1) {
                String sh = this.driver.executeScript(
                        "function getElementByXpath(path) {" +
                                "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                                +
                                "}" +
                                "var a = getElementByXpath(\"" + xpath1 + "\");" +
                                "return a ? a.value : '';",
                        new Object[0]).toString();
                if (sh != null && !sh.isEmpty()) {
                    store.updateValue(sh);
                    driver.getExecutionLogReporter().info("Fetched value: " + sh);
                    bstatus = true;
                } else {
                    driver.getExecutionLogReporter().info("Value is empty for xpath: " + xpath1);
                    bstatus = false;
                }

            } else {
                bstatus = false;
                driver.getExecutionLogReporter().info("Element not Exist for xpath: " + xpath1);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Error in getvalue");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getTherapistfullname", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Combine the last name and firstname in the single fullname string seperated by comma"))
    public boolean getTherapistfullname(String firstname, String lastname, IArgument fullname) {
        boolean bstatus = false;
        try {
            String FullName = firstname + " " + lastname;
            fullname.updateValue(FullName);
            driver.getExecutionLogReporter().info(
                    "Generated therapist full name '" + FullName + "' and stored into runtime variable: " + fullname);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in getTherapistFullName: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-removeChar", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Remove a specific character"))
    public boolean removeChar(String character, String value, IArgument Store) {
        boolean bstatus = false;
        try {
            if (value != null && character != null) {
                String a = value.replace(character, "");
                Store.updateValue(a);
                driver.getExecutionLogReporter()
                        .info("Stored runtime value after removing '" + character + "' from input: " + a);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().error("Input value or character to remove is null.");
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred when element is not checked");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-020", groupName = "Dropdown", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Clicks on a dropdown option that matches the given value"))
    public boolean dropdownClick(String replaceValue) {
        boolean bstatus = false;
        try {
            if (replaceValue.isEmpty()) {
                driver.getExecutionLogReporter().info("Replace value is empty, dropdown click skipped");
                bstatus = false;
                return bstatus;
            }
            String xpathvalue = getObjectProperty("xpath");
            String LastElement = "//div[contains(@class,'menuable__content__active')]/div/div/div/div";

            if (!xpathvalue.contains("#replace")) {
                driver.getExecutionLogReporter().info("XPath does not contain #replace placeholder");
                return false;
            }
            xpathvalue = xpathvalue.replaceAll("#replace", replaceValue);
            boolean flag = true;

            int counter = 1;
            while (true) {
                int lenCount = driver.findElements(FindBy.xpath(LastElement)).size();
                int elementCount = driver.findElements(FindBy.xpath(xpathvalue)).size();
                if (elementCount == 1) {
                    scrollIntoViewIfNeeded("xpath", xpathvalue);
                    break;
                } else {
                    scrollIntoViewIfNeeded("xpath", "(" + LastElement + ")[" + lenCount + "]");
                    Thread.sleep(300);
                    int len = driver.findElements(FindBy.xpath(LastElement)).size();
                    if (lenCount == len) {
                        if (!(elementCount == 1)) {
                            bstatus = false;
                            driver.getExecutionLogReporter()
                                    .info("Dropdown element not found with value: " + replaceValue);
                            break;
                        }
                        break;
                    }
                    counter++;
                }
            }
            driver.findElement(FindBy.xpath(xpathvalue)).click();
            driver.getExecutionLogReporter().info("Dropdown option clicked with value: " + replaceValue);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in dropdownClick: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-021", groupName = "Scroll", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Scrolls in the page till the element comes into view"))
    public boolean scrollIntoViewIfNeeded(String identifiername, String value) {
        boolean bstatus = false;
        try {
            if (identifiername == null || value == null || value.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: locator type or value is null/empty");
                return false;
            }
            if (identifiername.equalsIgnoreCase("xpath")){
                String sh = this.driver.executeScript(
                        "function getElementByXpath(path) {" +
                                "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                                +
                                "}" +
                                "var a = getElementByXpath(\"" + value + "\");" +
                                "if(a){ a.scrollIntoViewIfNeeded(); return 'true'; } else { return 'false'; }",
                        new Object[0]).toString();
                driver.getExecutionLogReporter().info("ScrollIntoViewIfNeeded executed. Result: " + sh);
            }
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in scrollIntoViewIfNeeded: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-022", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Create a number that isn’t repeated"))
    public boolean generateUniqueNumber(IArgument Store) {
        boolean bstatus = false;
        try {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf = new SimpleDateFormat("ddMMhhmmss");
            String Number = sdf.format(date); // MMDDHHMMSS
            // webdriver.datadisplay("Generated Number: ", Number);
            Store.updateValue(Number);
            driver.getExecutionLogReporter().info("Generated Unique Number: " + Number);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred when generateUniqueNumber");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyvalidationnotexists", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks that the element is not present on the webpage"))
    public boolean verifyValidationNotExists(String replaceValue1) {
        boolean bstatus = false;
        try {
            if (replaceValue1 == null || replaceValue1.isEmpty()) {
                driver.getExecutionLogReporter().info("Provided replace value is null or empty");
                return false;
            }

            String locatorValue = getObjectProperty("xpath");
            locatorValue = locatorValue.replaceAll("#replace", replaceValue1);

            int elementCount = driver.findElements(FindBy.xpath(locatorValue)).size();
            if (elementCount == 0) {
                driver.getExecutionLogReporter().info("Validation element not found for value: " + replaceValue1);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().info("Validation element exists for value: " + replaceValue1);
                bstatus = false;
            }

        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in verifyValidationNotExists: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-024", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies that the element is checkable"))
    public boolean verifyElementisChecked() {
        boolean bstatus = false;
        try {
            String locatorValue = getObjectProperty("xpath");
            // 2. Run custom JavaScript to fetch the element and its "ariaChecked" property
            String checkedValue = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorValue + "\");" +
                            "return a ? a.ariaChecked : 'false';",
                    new Object[0]).toString();
            driver.getExecutionLogReporter().info("ariaChecked value: " + checkedValue);
            if ("true".equalsIgnoreCase(checkedValue)) {
                bstatus = true;
                driver.getExecutionLogReporter().info("Element is checked.");
            } else {
                driver.getExecutionLogReporter().info("Element is NOT checked.");
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in verifyElementisChecked");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-025", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies that the element is not checkable"))
    public boolean verifyElementisNotChecked(String replaceValue) {
        boolean bstatus = false;
        try {
            String locatorValue = getObjectProperty("xpath");
            locatorValue = locatorValue.replaceAll("#replace", replaceValue);

            // 1. Run custom JavaScript to fetch the element and its "checked" property
            String checkedValue = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorValue + "\");" +
                            "return a ? a.checked : 'false';",
                    new Object[0]).toString();
            driver.getExecutionLogReporter().info("Checked value: " + checkedValue);

            // 4. Mark true if element is NOT checked
            if ("false".equalsIgnoreCase(checkedValue)) {
                bstatus = true;
                driver.getExecutionLogReporter().info("Element is NOT checked.");
            } else {
                driver.getExecutionLogReporter().info("Element is checked.");
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in verifyElementisNotChecked");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-026", groupName = "Click", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Clicks the target element from within the static element by using JS"))
    public boolean dynamicClickUsingJS(String replaceValue) {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            locatorvalue = locatorvalue.replaceAll("#replace", replaceValue);
            driver.clickUsingJs(FindBy.xpath(locatorvalue));
            if (bstatus) {
                driver.getExecutionLogReporter()
                        .info("Successfully clicked element using JS with value: " + replaceValue);
            } else {
                driver.getExecutionLogReporter().info("Failed to click element using JS with value: " + replaceValue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in dynamicClickUsingJS");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getContentText", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Returns the visible text content from an element or document"))
    public boolean getContentText(IArgument store) {
        boolean bstatus = false;
        try {
            String xpath = getObjectProperty("xpath");
            String contentText = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + xpath + "\");" +
                            "return a ? a.textContent : '';",
                    new Object[0]).toString();
            driver.getExecutionLogReporter().info("ContentText: " + contentText);
            store.updateValue(contentText);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in getContentText");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-validatePartialParameters", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Check if some parameters are valid"))
    public boolean validatePartialParameters(String fullText, String partialText) {
        boolean bstatus = false;
        try {
            if (fullText != null && partialText != null && fullText.contains(partialText)) {
                bstatus = true;
                driver.getExecutionLogReporter().info(
                        "Partial text '" + partialText + "' found in full text: " + fullText);
            } else {
                driver.getExecutionLogReporter().info(
                        "Partial text '" + partialText + "' not found in full text: " + fullText);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in validatePartialParameters");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-clearTextbox", groupName = "Clear", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Resets the UI input field to blank"))
    public boolean clearTextbox() {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            String result = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "if (a) { a.value = ''; return 'true'; } else { return 'false'; }",
                    new Object[0]).toString();
            Thread.sleep(500);
            driver.getExecutionLogReporter().info("Textbox cleared successfully.");
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception in clearTextbox");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-checkifElementisDisabled", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether an element is disabled or not"))
    public boolean checkifElementisDisabled(String replaceValue) {
        boolean bStatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            locatorvalue = locatorvalue.replaceAll("#replace", replaceValue);
            locatorvalue = locatorvalue.replaceAll("\"", "\\\\\"");
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "return a ? (a.disabled || a.getAttribute('disabled') === 'disabled' || a.getAttribute('aria-disabled') === 'true') : false;",
                    new Object[0]).toString().toLowerCase();
            if (sh.equals("true")) {
                bStatus = true;
                driver.getExecutionLogReporter().info("Element is disabled for value: " + replaceValue);
            } else {
                driver.getExecutionLogReporter().info("Element is enabled for value: " + replaceValue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while checking element disabled state: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyClearContent", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Check that the content has been cleared"))
    public boolean verifyClearContent() {
        boolean bstatus = false;
        try {
            String xpathvalue = getObjectProperty("xpath");
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + xpathvalue + "\");" +
                            "return a ? a.value : '';",
                    new Object[0]).toString();
            if (sh.equals("")) {
                bstatus = true;
                driver.getExecutionLogReporter().info("Textbox is cleared successfully.");
            } else {
                driver.getExecutionLogReporter().info("Textbox still contains value: " + sh);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while verifying textbox content: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-dropdowncheckvalueWithScroll", groupName = "Dropdown", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks a value in a dropdown list with scrolling support"))
    public boolean dropdowncheckvalueWithScroll(String valuetoenter) {
        boolean bStatus = false;
        try {
            if (valuetoenter == null || valuetoenter.trim().isEmpty()) {
                driver.getExecutionLogReporter().error("Invalid input: dropdown value is null or empty");
                return false;
            }
            String xpathvalue = getObjectProperty("xpath");
            //driver.clickUsingJs(FindBy.xpath(xpathvalue));
            if (valuetoenter != null && !valuetoenter.isEmpty()) {
                Thread.sleep(300);
                String[] values = valuetoenter.split("&");

                for (String value : values) {
                    boolean valueSelected = false;
                    String optionXPath = "(//div[@role='option' or @role='listbox']//div[text()='" + value + "'])[1]";
                    String scrollElementsXPath = "//div[@role='option' or @role='listbox']//div";

                    int attempts = 0;
                    while (attempts < 40) { // Limit attempts to avoid infinite loop
                        int visibleElementsCount = driver.findElements(FindBy.xpath(scrollElementsXPath)).size();
                        int matchCount = driver.findElements(FindBy.xpath(optionXPath)).size();

                        if (matchCount > 0) {
                            scrollIntoViewIfNeeded("xpath", optionXPath);
                            if (driver.clickUsingJs(FindBy.xpath(optionXPath))) {
                                driver.getExecutionLogReporter().info("Successfully selected option: " + value);
                                valueSelected = true;
                                break;
                            }
                        } else {
                            scrollIntoViewIfNeeded("xpath",
                                    "(" + scrollElementsXPath + ")[" + visibleElementsCount + "]");
                            Thread.sleep(300);
                            int newVisibleCount = driver.findElements(FindBy.xpath(scrollElementsXPath)).size();
                            if (newVisibleCount == visibleElementsCount) {
                                driver.getExecutionLogReporter().info("No more elements to scroll for value: " + value);
                                break; // Stop if no more elements are loading
                            }
                        }
                        attempts++;
                    }
                    if (!valueSelected) {
                        driver.getExecutionLogReporter().info("Failed to select dropdown option: " + value);
                        bStatus = false;
                        break; // Stop if any value fails
                    } else {
                        bStatus = true;
                    }
                }
                // Close dropdown
                driver.clickUsingJs(FindBy.xpath("//html"));
                Thread.sleep(300);
            } else {
                bStatus = false;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while selecting dropdown value: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-verifySumValue", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies that the total sum is correct"))
    public boolean verifySumValue(String expectedSum) {
        boolean bstatus = false;
        try {
            int number = Integer.parseInt(expectedSum);
            String xpath1 = getObjectProperty("xpath");
            List<IQAWebElement> elements = driver.findElements(FindBy.xpath(xpath1));
            int matchCount = driver.findElements(FindBy.xpath(xpath1)).size();
            driver.getExecutionLogReporter().info("Number of elements found: " + matchCount);
            List<Integer> values = new ArrayList<Integer>();
            for (IQAWebElement element : elements) {
                String elementText = element.getText().trim().toLowerCase();
                int value = Integer.parseInt(elementText);
                values.add(value);
            }
            int sum = 0;
            // List<Integer> str1 = new ArrayList<Integer>(strings);
            for (int value : values) {
                sum += value;
                System.out.println("Running sum: " + sum);
            }
            // boolean b1 = strings.equals(s);
            if (sum == number) {
                driver.getExecutionLogReporter().info("Sum matched: " + sum);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().error("Sum mismatch. Expected: " + number + ", Found: " + sum);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in verifySumValue: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-validateNotNull", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks that the value is not null"))
    public boolean validateNotNull(String param1) {
        boolean bstatus = false;
        try {
            if (param1 != null && !param1.equalsIgnoreCase("null") && !param1.equals("0")) {
                driver.getExecutionLogReporter().info("Value is valid: " + param1);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().info("Value is invalid (null or 0): " + param1);
                bstatus = false;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while validating value: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getDayLetter", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Gives the first letter of a day’s name"))
    public boolean getDayLetter(String dateString, IArgument value) {
        boolean bStatus = false;
        String Letter = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date date = sdf.parse(dateString);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            String day = dayFormat.format(date);
            day = day.toLowerCase();
            if (day.equals("monday")) {
                Letter = "M";
                bStatus = true;
            } else if (day.equals("tuesday")) {
                Letter = "T";
                bStatus = true;
            } else if (day.equals("wednesday")) {
                Letter = "W";
                bStatus = true;
            } else if (day.equals("thursday")) {
                Letter = "Th";
                bStatus = true;
            } else if (day.equals("friday")) {
                Letter = "F";
                bStatus = true;
            } else if (day.equals("saturday")) {
                Letter = "S";
                bStatus = true;
            } else if (day.equals("sunday")) {
                Letter = "Su";
                bStatus = true;
            } else {
                driver.getExecutionLogReporter().error("Invalid day derived from date: " + day);
                return bStatus = false;
            }
            value.updateValue(Letter);
            driver.getExecutionLogReporter().info("Day: " + day + ", Letter stored: " + Letter);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while getting day letter: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-036", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Gets the current time and converts it to EST"))
    public boolean getTimeAndConvertToEST(String value, IArgument formatte) {
        boolean bStatus = false;
        try {
            LocalDateTime currentTimeEST = LocalDateTime.now(ZoneId.of("America/New_York"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");
            String estTimeString = currentTimeEST.format(formatter);
            formatte.updateValue(estTimeString);
            driver.getExecutionLogReporter().info("Current time in EST: " + estTimeString);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while getting EST time: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-037", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Combine multiple strings and save the result"))
    public boolean concatandstoremultipleString(String phone1, String phone2, String phone3, IArgument phoneNumber) {
        boolean bstatus = false;
        try {
            String value = phone1 + phone2 + phone3;
            if (value == null || value.isEmpty()) {
                driver.getExecutionLogReporter().error("Concatenated value is empty.");
                bstatus = false;
            } else {
                if (value.matches("^\\+?\\d+$")) {
                    phoneNumber.updateValue(value);
                    driver.getExecutionLogReporter()
                            .info("Concatenated value stored. Key: " + phoneNumber + ", Value: " + value);
                    bstatus = true;
                } else {
                    driver.getExecutionLogReporter().error("Invalid phone number format: " + value);
                    bstatus = false;
                }
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while concatenating phone number: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-038", groupName = "Drag and Drop", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks if a value is missing from the dropdown list"))
    public boolean notExistInDropdown(String replaceValue) {
        boolean bstatus = false;
        try {
            if (replaceValue == null || replaceValue.isEmpty()) {
                driver.getExecutionLogReporter().error("Provided replaceValue is null or empty.");
                return false;
            }
            String LastElement = "//div[contains(@class,'menuable__content__active')]/div/div/div/div";
            String locatorvalue = getObjectProperty("xpath");
            if (!locatorvalue.contains("#replace")) {
                driver.getExecutionLogReporter().error("Locator does not contain '#replace': " + locatorvalue);
                return false;
            }
            locatorvalue = locatorvalue.replaceAll("#replace", replaceValue);
            boolean flag = true;
            int counter = 1;
            while (flag) {
                int lenCount = driver.findElements(FindBy.xpath(LastElement)).size();
                int elementCount = driver.findElements(FindBy.xpath(locatorvalue)).size();
                if (elementCount == 1) {
                    scrollIntoViewIfNeeded("xpath", locatorvalue);
                    break;
                } else {
                    scrollIntoViewIfNeeded("xpath", "(" + LastElement + ")[" + lenCount + "]");
                    Thread.sleep(300);
                    int len = driver.findElements(FindBy.xpath(LastElement)).size();
                    if (lenCount == len) {
                        if (!(elementCount == 1)) {
                            driver.getExecutionLogReporter()
                                    .info("Element with value '" + replaceValue + "' not found in dropdown.");
                            bstatus = true;
                            break;
                        } else {
                            bstatus = false;
                        }
                        break;
                    }
                    counter++;
                }
            }
            if (!bstatus) {
                driver.findElement(FindBy.xpath(locatorvalue)).click();
                driver.getExecutionLogReporter().info("Clicked on dropdown element with value: " + replaceValue);
                bstatus = true;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in notExistInDropdown: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-compareStringValues", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Check if two strings are the same"))
    public boolean compareStringValues(String Basestring, String valuetovalidate) {
        boolean bstatus = false;
        try {
            // Trim spaces to ensure accurate comparison
            String trimmedBase = Basestring != null ? Basestring.trim() : "";
            String trimmedValue = valuetovalidate != null ? valuetovalidate.trim() : "";

            if (trimmedBase.equals(trimmedValue)) {
                driver.getExecutionLogReporter()
                        .info("Strings match. Base: " + trimmedBase + ", Value: " + trimmedValue);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter()
                        .info("Strings do not match. Base: " + trimmedBase + ", Value: " + trimmedValue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in compareStringValues: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-040", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Converts a given value to GMT"))
    public String gmtConverter(String dateString) throws ParseException {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            Date date = inputFormat.parse(dateString);
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            outputFormat.setTimeZone(gmtTimeZone);
            // Format the date in GMT time zone
            String gmtDateString = outputFormat.format(date);
            driver.getExecutionLogReporter().info("Converted to GMT: " + gmtDateString);
            return gmtDateString;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Parse error in gmtConverter: " + e.toString());
            return null; // or return some default value
        }
    }

    @SyncAction(uniqueId = "custom-web-elementisSelected", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether the element can be selected"))
    public boolean elementisSelected() {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "  return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "return a ? (a.checked || a.getAttribute('aria-checked') === 'true') : false;",
                    new Object[0]).toString();
            if (sh.equalsIgnoreCase("true")) {
                driver.getExecutionLogReporter().info("Element is selected. Locator: " + locatorvalue);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().info("Element is NOT selected. Locator: " + locatorvalue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in elementIsSelected: " + e.toString());
            return false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-datePicker", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "selecting dates from a calendar"))
    public boolean datePicker(String Date) {
        boolean bStatus = false;
        try {
            if (Date == null || Date.trim().isEmpty()) {
                driver.getExecutionLogReporter().info("Provided date string is null or empty");
                return false;
            }
            driver.findElement(FindBy.xpath("//div[@class='v-date-picker-header__value']/div/button")).click();
            Thread.sleep(300);
            driver.findElement(FindBy.xpath("//div[@class='v-date-picker-header__value']/div/button")).click();
            Thread.sleep(300);
            String[] date1 = Date.split("/");
            String year = date1[2];
            String yearXpath = "//ul[@class='v-date-picker-years']/li[text()='" + year + "']";
            driver.findElement(FindBy.xpath(yearXpath)).click();
            driver.getExecutionLogReporter().info("Year selected: " + year);
            String month = date1[0];
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM");
            Date date = inputFormat.parse(month);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM");
            String mon = outputFormat.format(date);
            String monthXpath = "//div[@class='v-date-picker-table v-date-picker-table--month theme--light']/table/tbody/tr/td/button/div[text()='"
                    + mon + "']";
            driver.findElement(FindBy.xpath(monthXpath)).click();
            driver.getExecutionLogReporter().info("Month selected: " + mon);
            Thread.sleep(300);
            String day = date1[1];
            String day1 = Integer.toString(Integer.parseInt(day));
            String dateXpath = "//div[@class='v-date-picker-table v-date-picker-table--date theme--light']/table/tbody/tr/td/button[@class='v-btn v-btn--text v-btn--rounded theme--light']/div[text()='"
                    + day1 + "']";
            driver.findElement(FindBy.xpath(dateXpath)).click();
            driver.getExecutionLogReporter().info("Day selected: " + day1);
            Thread.sleep(300);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in datePicker: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-043", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Converts a given timestamp into a Date object in GMT"))
    public Date convertToGMTHour(String timestamp) {
        // Timestamp stamp = new Timestamp();
        Timestamp stamp = new Timestamp(Long.valueOf(timestamp));
        Date date = new Date(stamp.getTime());
        return date;
    }

    @SyncAction(uniqueId = "custom-web-getDownloadedFileName", groupName = "File Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Get the name of the downloaded file"))
    public boolean getDownloadedFileName(String downloadPath, IArgument value) {
        boolean bStatus = false;
        try {
            File dir = new File(downloadPath);
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                driver.getExecutionLogReporter().info("No files found in download path: " + downloadPath);
                return false;
            }
            String mynewfilename = files[0].toString();
            value.updateValue(mynewfilename);
            driver.getExecutionLogReporter().info("Downloaded file found: " + mynewfilename);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in getDownloadedFileName: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-verifySeriesVisit", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "checks if a series of scheduled visits are correctly created and appear as expected"))
    public boolean verifySeriesVisit(String StartDate, String EndDate, String StrTime, String EndTime, String Week,
            String view) {
        boolean bstatus = false;
        try {
            if (StartDate == null || EndDate == null || Week == null || view == null) {
                driver.getExecutionLogReporter().info("Invalid input: one or more parameters are null");
                return false;
            }
            String a = StartDate;
            String b = EndDate;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate startDate = LocalDate.parse(a, formatter);
            LocalDate endDate = LocalDate.parse(b, formatter);

            Week = Week.toUpperCase();
            LocalDate dday = null;
            if (Week.equals("MONDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            }
            if (Week.equals("TUESDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));
            }
            if (Week.equals("WEDNESDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
            }
            if (Week.equals("THURSDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
            }
            if (Week.equals("FRIDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
            }
            if (Week.equals("SATURDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            }
            if (Week.equals("SUNDAY")) {
                dday = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            }
            List<LocalDate> weekDates = new ArrayList<>();
            while (!dday.isAfter(endDate)) {
                weekDates.add(dday);
                dday = dday.plusWeeks(1);
            }
            List<String> formattedDates = new ArrayList<>();
            for (LocalDate date : weekDates) {
                String formatted = date.format(formatter);
                formattedDates.add(formatted);
                driver.getExecutionLogReporter().info("Series visit date: " + formatted);
            }
            String viewXpath = "//span[text()='" + view + "']";
            driver.findElement(FindBy.xpath(viewXpath)).click();
            for (String visitDate : formattedDates) {
                datePicker(visitDate);
                boolean flag = scheduleSingleEvent(visitDate, StrTime, visitDate, EndTime);
                if (!flag) {
                    driver.getExecutionLogReporter().error("Failed to schedule event on: " + visitDate);
                    return false;
                }
            }
            driver.getExecutionLogReporter().info("Successfully verified series visit for week: " + Week);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while verifying series visit");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-046", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Set up one event at a specific time"))
    public boolean scheduleSingleEvent(String StDate, String StTime, String EnDate, String EnTime) {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            String jsResult = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "  return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "function getdate() {" +
                            "  var ss = {};" +
                            "  var s = [];" +
                            "  for (let i in a.fcSeg) {" +
                            "    ss = {};" +
                            "    if (i == 'start' || i == 'end') {" +
                            "      ss.name = i;" +
                            "      ss.value = new Date(a.fcSeg[i]).getTime();" +
                            "      s.push(ss);" +
                            "    }" +
                            "  }" +
                            "  return s;" +
                            "}" +
                            "var attrb = getdate();" +
                            "return JSON.stringify(attrb);",
                    new Object[0]).toString();
            driver.getExecutionLogReporter().info("JS Execution Result: " + jsResult);
            JSONArray resultArray = new JSONArray(jsResult);
            if (!jsResult.isEmpty()) {
                // get Start date
                Date start = convertToGMTHour(resultArray.getJSONObject(0).get("value").toString());
                String StartDateString = gmtConverter(start.toString());
                String StartDate = convertToGMTDate(StartDateString);
                String StartTime = convertToGMTTime(StartDateString);
                // get End date
                Date end = convertToGMTHour(resultArray.getJSONObject(1).get("value").toString());
                String enddateString = gmtConverter(end.toString());
                String EndDate = convertToGMTDate(enddateString);
                String EndTime = convertToGMTTime(enddateString);

                if (StartDate.equalsIgnoreCase(StDate) && StartTime.equalsIgnoreCase(StTime)
                        && EndDate.equalsIgnoreCase(EnDate) && EndTime.equalsIgnoreCase(EnTime)) {
                    driver.getExecutionLogReporter().info("Event scheduled successfully with matching date and time");
                    bstatus = true;
                } else {
                    driver.getExecutionLogReporter().error("Event time mismatch! Expected: "
                            + StDate + " " + StTime + " - " + EnDate + " " + EnTime
                            + ", Found: " + StartDate + " " + StartTime + " - " + EndDate + " " + EndTime);
                    bstatus = false;
                }
            } else {
                driver.getExecutionLogReporter().error("No event data returned from script execution.");
                bstatus = false;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in scheduleSingleEvent");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-047", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Converts a given time to GMT"))
    public String convertToGMTTime(String dateString) throws ParseException {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            java.util.Date date = inputFormat.parse(dateString);
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            outputFormat.setTimeZone(gmtTimeZone);
            String gmtDateString = outputFormat.format(date);
            driver.getExecutionLogReporter().info("Date in GMT: " + gmtDateString);
            SimpleDateFormat outputFormat1 = new SimpleDateFormat("hh:mm a");
            outputFormat1.setTimeZone(gmtTimeZone);
            String gmtTime = outputFormat1.format(date);
            driver.getExecutionLogReporter().info("Formatted GMT Time: " + gmtTime);
            return gmtTime;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in convertToGMTTime " + e.toString());
            return null; // or return some default value
        }
    }

    @SyncAction(uniqueId = "Xfit-048", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Converts a given date to GMT"))
    public String convertToGMTDate(String dateString) throws ParseException {
        try {
            // Input format: Example -> "Wed Sep 04 19:30:00 IST 2025"
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            java.util.Date date = inputFormat.parse(dateString);
            // GMT timezone
            TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
            // Log full GMT date
            SimpleDateFormat fullFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            fullFormat.setTimeZone(gmtTimeZone);
            String gmtDateString = fullFormat.format(date);
            driver.getExecutionLogReporter().info("Date in GMT: " + gmtDateString);
            SimpleDateFormat outputFormat1 = new SimpleDateFormat("MM/dd/YYYY");
            outputFormat1.setTimeZone(gmtTimeZone);
            String gmtDate = outputFormat1.format(date);
            driver.getExecutionLogReporter().info("Formatted date: " + gmtDate);
            return gmtDate;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in convertToGMTDate " + e.toString());
            return null; // or return some default value
        }
    }

    @SyncAction(uniqueId = "custom-web-dropdowncheckvalue", groupName = "Dropdown", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Selects multiple values from a checkbox-style dropdown. The values are passed as a single string, separated by '&'"))
    public boolean dropdowncheckvalue(String valuetoenter) {
        boolean bStatus = false;
        try {
            if (valuetoenter != null && !valuetoenter.trim().isEmpty()) {
                // Open dropdown
                driver.clickUsingJs(null);
                Thread.sleep(300);

                String[] values = valuetoenter.split("&");
                for (String val : values) {
                    String optionXpath = "(//div[@role='option' or @role='listbox']//div[text()='" + val.trim()
                            + "'])[1]";

                    if (!driver.clickUsingJs(FindBy.xpath(optionXpath))) {
                        driver.getExecutionLogReporter().error("Failed to select dropdown option: " + val.trim());
                        bStatus = false;
                        break;
                    } else {
                        driver.getExecutionLogReporter().info("Successfully selected dropdown option: " + val.trim());
                        bStatus = true;
                    }
                    Thread.sleep(200);
                }
                // Close dropdown after all selections
                driver.clickUsingJs(FindBy.xpath("//html"));
                Thread.sleep(300);
            } else {
                driver.getExecutionLogReporter().info("Provided value is null or empty");
                bStatus = false;
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while selecting dropdown value(s)");
            driver.getExecutionLogReporter().error(e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-selectTherapist", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Select a therapist from the list. The values are passed as a single string, separated by '&'"))
    public boolean selectTherapist(String value) { // Phwintheiser, Phmargaretta&
        boolean bstatus = false;
        try {
            String[] v = value.split("&");
            for (int j = 0; j < v.length; j++) {
                String xpath = "//label[text()='Search Available']/../input";
                driver.findElement(FindBy.xpath(xpath)).click();
                driver.findElement(FindBy.xpath(xpath)).enterText("v[j]");
                String xpath2 = "//div[contains(@class,'item multi-select-border')]/span[text()='" + v[j] + "']";
                driver.findElement(FindBy.xpath(xpath2)).click();
                driver.findElement(FindBy.xpath(xpath2)).click();
                Thread.sleep(300);
                String xpath3 = "//label[text()='Search Available']/../input";
                driver.findElement(FindBy.xpath(xpath3)).clearText();
            }
            String xpath4 = "(//button[text()='Save'])[1]";
            driver.findElement(FindBy.xpath(xpath4)).click();
            Thread.sleep(400);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while selecting therapist");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-removeWidgets", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Removes all widgets from the patient dashboard"))
    public boolean removeWidgets() {
        boolean bStatus = false;
        try {
            String xpathvalue = getObjectProperty("xpath");
            int removeLength = driver.findElements(FindBy.xpath(xpathvalue)).size();
            for (int i = 1; i <= removeLength; i++) {
                String xpath = "(" + xpathvalue + ")[last()]";
                driver.findElement(FindBy.xpath(xpath)).click();
            }
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while removewidgets");
            driver.getExecutionLogReporter().error(e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyvalidationexists", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies validation message exists"))
    public boolean verifyvalidationexists(String replaceValue1, String replaceValue2) {
        boolean bstatus = false;
        try {
            if (replaceValue1 == null || replaceValue1.isEmpty() ||
                    replaceValue2 == null || replaceValue2.isEmpty()) {
                driver.getExecutionLogReporter().info("One or both replace values are empty.");
                return false;
            }
            String locatorValue = getObjectProperty("xpath");
            if (!locatorValue.contains("#replace1") || !locatorValue.contains("#replace2")) {
                driver.getExecutionLogReporter()
                        .error("XPath does not contain required placeholders: #replace1 and/or #replace2");
                return false;
            }
            locatorValue = locatorValue
                    .replace("#replace1", replaceValue1)
                    .replace("#replace2", replaceValue2);
            int elementCount = driver.findElements(FindBy.xpath(locatorValue)).size();

            if (elementCount == 1) {
                driver.getExecutionLogReporter().info("Validation element found for XPath: " + locatorValue);
                bstatus = true;
            } else if (elementCount > 1) {
                driver.getExecutionLogReporter()
                        .error("Multiple validation elements (" + elementCount + ") found for XPath: " + locatorValue);
                bstatus = false;
            } else {
                driver.getExecutionLogReporter().error("No validation element found for XPath: " + locatorValue);
                bstatus = false;
            }

        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while validating");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getEndTime", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Get the finish time of an event or task"))
    public boolean getEndTime(String startTime, String duration, IArgument store) {
        boolean bstatus = false;
        try {
            if (startTime == null || startTime.isEmpty() || duration == null || duration.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: startTime or duration is empty");
                return false;
            }
            int Dur = Integer.parseInt(duration);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            Date date = sdf.parse(startTime);
            long startTimeInMillis = date.getTime();
            long durationInMillis = Dur * 60 * 1000; // Convert minutes to milliseconds
            long resultTimeInMillis = startTimeInMillis + durationInMillis;
            Date resultDate = new Date(resultTimeInMillis);
            String endtime = sdf.format(resultDate).toUpperCase();
            store.updateValue(endtime);
            driver.getExecutionLogReporter().info("End time calculated successfully: " + endtime);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while calculating end time");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-phoneNumberFormate", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Formats a given phone number into the standard format"))
    public boolean phoneNumberFormate(String name, IArgument store) {
        boolean bstatus = false;
        try {
            if (name == null || name.length() < 10) {
                driver.getExecutionLogReporter().info("Invalid phone number input: " + name);
                return false;
            }
            String Ph = name.substring(0, 3) + "-" + name.substring(3, 6) + "-" + name.substring(6, 10);
            store.updateValue(Ph);
            driver.getExecutionLogReporter().info("Phone number formatted successfully: " + Ph);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred while formatting phone number");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-verifyElementisChecked", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Verifies that the element is checkable"))
    public boolean verifyElementisChecked(String replaceValue) {
        boolean bstatus = false;
        try {
            if (replaceValue == null || replaceValue.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: replaceValue is null or empty");
                return false;
            }
            String locatorvalue = getObjectProperty("xpath");
            if (!locatorvalue.contains("#replace")) {
                driver.getExecutionLogReporter().error("XPath does not contain #replace placeholder: " + locatorvalue);
                return false;
            }
            locatorvalue = locatorvalue.replaceAll("#replace", replaceValue);
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "return a ? a.checked : false;",
                    new Object[0]).toString();
            driver.getExecutionLogReporter().info("Element checked state for [" + replaceValue + "]: " + sh);
            if (sh.equalsIgnoreCase("True")) {
                bstatus = true;
                driver.getExecutionLogReporter().info("Element is checked for value: " + replaceValue);
        } else {
            driver.getExecutionLogReporter().info("Element is NOT checked for value: " + replaceValue);
        }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in verifyElementisChecked: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-getTimeAndConvertToEST", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Gets the current time and converts it to EST"))
    public boolean getTimeAndConvertToEST(IArgument value) {
        boolean bStatus = false;
        try {
            LocalDateTime currentTimeEST = LocalDateTime.now(ZoneId.of("America/New_York"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m");
            String estTimeString = currentTimeEST.format(formatter);
            // System.out.println("Current time in EST: " + estTimeString);
            value.updateValue(estTimeString);
            driver.getExecutionLogReporter()
                    .info("Current time in EST stored successfully. Key: " + value + ", Value: " + estTimeString);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while getting EST time: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-057", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether an element is disabled or not"))
    public boolean checkifElementisDisabled() {
        boolean bStatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "return a ? a.disabled : false;",
                    new Object[0]).toString().toLowerCase();
            if (sh.equals("true")) {
                driver.getExecutionLogReporter().info("Element is disabled for XPath: " + locatorvalue);
                bStatus = true;
            } else {
                driver.getExecutionLogReporter().info("Element is not disabled for XPath: " + locatorvalue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while checking element disabled state: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-058", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Checks whether an element is disabled or not"))
    public boolean verifyElementisNotChecked() {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            if (locatorvalue == null || locatorvalue.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: locatorValue is null or empty");
                return false;
            }
            String sh = this.driver.executeScript(
                    "function getElementByXpath(path) {" +
                            "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                            +
                            "}" +
                            "var a = getElementByXpath(\"" + locatorvalue + "\");" +
                            "return a.checked;",
                    new Object[0]).toString();
            if ("false".equals(sh)) {
                driver.getExecutionLogReporter().info("Element is not checked for XPath: " + locatorvalue);
                bstatus = true;
            } else {
                driver.getExecutionLogReporter().info("Element is checked for XPath: " + locatorvalue);
            }
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while verifying element checked state: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-059", groupName = "Scroll", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Scrolls in the page till the element comes into view"))
    public boolean scrollIntoViewIfNeeded(String replaceValue) {
        boolean bstatus = false;
        try {
            if (replaceValue == null || replaceValue.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: replaceValue is null or empty");
                return false;
            }
            String locatorvalue = getObjectProperty("xpath");
            locatorvalue = locatorvalue.replaceAll("#replace", replaceValue);
            bstatus = scrollIntoViewIfNeeded("xpath", locatorvalue);
            driver.getExecutionLogReporter().info("scrollIntoViewIfNeeded executed successfully for: " + locatorvalue);
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in scrollIntoViewIfNeeded: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-060", groupName = "Scroll", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Scrolls in the page till the element comes into view"))
    public boolean scrollIntoViewIfNeeded() {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            if (locatorvalue == null || locatorvalue.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid locator: XPath is null or empty");
                return false;
            }
            bstatus = scrollIntoViewIfNeeded("xpath", locatorvalue);
            driver.getExecutionLogReporter().info("scrollIntoViewIfNeeded executed successfully for: " + locatorvalue);
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in scrollIntoViewIfNeeded: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-061", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Set up one event at a specific time"))
    public boolean scheduleSingleEvent(String date1, String StartTime, String duration) {
        boolean bstatus = false;
        try {
            String xpathvalue = getObjectProperty("xpath");

            String script = "function getElementByXpath(path) {" +
                    "   return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;"
                    +
                    "}" +
                    "var a = getElementByXpath(\"" + xpathvalue + "\");" +
                    "function getdate() {" +
                    "   var ss = {};" +
                    "   var s = [];" +
                    "   for (let i in a.fcSeg) {" +
                    "       ss = {};" +
                    "       if (i=='start' || i=='end') {" +
                    "           ss.name = i;" +
                    "           ss.value = new Date(a.fcSeg[i]).getTime();" +
                    "           s.push(ss);" +
                    "       }" +
                    "   }" +
                    "   return s;" +
                    "}" +
                    "var attrb = getdate();" +
                    "return attrb;";
            JSONArray res1 = new JSONArray(this.driver.executeScript(script, new Object[0]));
            System.out.println(res1);
            if (!res1.isEmpty()) {
                Date start = convertToGMTHour(res1.getJSONObject(0).get("value").toString());
                Date end = convertToGMTHour(res1.getJSONObject(1).get("value").toString());
                Timestamp stamp = new Timestamp(Long.parseLong(res1.getJSONObject(0).get("value").toString()));
                Date date = new Date(stamp.getTime());
                DateFormat f = new SimpleDateFormat("MM/dd/YYYY");
                String startdate = f.format(date);

                Timestamp stamp1 = new Timestamp(Long.parseLong(res1.getJSONObject(1).get("value").toString()));
                date = new Date(stamp1.getTime());
                f = new SimpleDateFormat("MM/dd/YYYY");
                DateFormat gmtFormat = new SimpleDateFormat();
                TimeZone gmtTime = TimeZone.getTimeZone("GMT");
                gmtFormat.setTimeZone(gmtTime);
                String startTime = gmtFormat.format(start).split(" ")[1] + " " + gmtFormat.format(start).split(" ")[2];
                String endTime = gmtFormat.format(end).split(" ")[1] + " " + gmtFormat.format(end).split(" ")[2];
                String actduration = String.valueOf(TimeUnit.MILLISECONDS.toMinutes(end.getTime() - start.getTime()));
                Timestamp stamp2 = new Timestamp(Long.parseLong(res1.getJSONObject(0).get("value").toString()));
                f = new SimpleDateFormat("MM/dd/YYYY");
                Date actualenddate = convertToGMTHour(
                        String.valueOf(stamp2.getTime() + TimeUnit.MINUTES.toMillis(Long.parseLong(duration))));
                String actualendTime = gmtFormat.format(actualenddate).split(" ")[1] + " "
                        + gmtFormat.format(actualenddate).split(" ")[2];
                if (startTime.equalsIgnoreCase(StartTime) && date1.equalsIgnoreCase(startdate)
                        && actduration.equalsIgnoreCase(duration) && actualendTime.equalsIgnoreCase(endTime)) {
                    bstatus = true;
                    driver.getExecutionLogReporter()
                            .info("Single event scheduled successfully with correct start, end, and duration.");
                } else {
                    driver.getExecutionLogReporter()
                            .error("Event scheduling mismatch. Expected values did not match actual.");
                    bstatus = false;
                }
            } else
                driver.getExecutionLogReporter().error("No event data found for scheduling.");
            bstatus = false;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception occurred in scheduleSingleEvent: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-calculateMonthCountFromDOB", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Calculate's months from date of birth"))
    public boolean calculateMonthCountFromDOB(String dobString, IArgument Store) {
        boolean bStatus = false;
        try {
            if (dobString == null || dobString.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: dobString is null or empty");
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date dob = sdf.parse(dobString);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar currentCalendar = Calendar.getInstance();
            int years = currentCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            int months = currentCalendar.get(Calendar.MONTH) - dobCalendar.get(Calendar.MONTH);
            int monthCount = (years * 12) + months;
            if (monthCount < 0) {
                driver.getExecutionLogReporter().error("DOB is in the future: " + dobString);
                return false;
            }
            Store.updateValue(dobString);
            driver.getExecutionLogReporter().info("Month count from DOB (" + dobString + ") calculated: " + monthCount);
            bStatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while calculating month count from DOB: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }

    @SyncAction(uniqueId = "Xfit-063", groupName = "Dropdown", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Get the value selected from a dropdown menu"))
    public boolean getDropdownSelectedValue(IArgument storeValue) {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            if (locatorvalue == null || locatorvalue.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: XPath locator is null or empty");
                return false;
            }
            String a = (String) driver.executeScript(
                "function getElementByXpath(path) {" +
                "return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
            "}" +
            "var dropdown = getElementByXpath(\"" + locatorvalue + "\");" +
            "if (!dropdown) return ''; " +
            "var selectedOption = dropdown.options[dropdown.selectedIndex];" +
            "return selectedOption ? selectedOption.text : '';"
        );
            storeValue.updateValue(a);
            driver.getExecutionLogReporter().info("Dropdown selected value stored successfully: " + a);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while getting dropdown selected value: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-064", groupName = "File Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Deleting all files created within the directory"))
    public boolean clearDir(String path) {
        boolean bstatus = false;
        try {
            if (path == null || path.isEmpty()) {
                driver.getExecutionLogReporter().info("Invalid input: directory path is null or empty");
                return false;
            }
            File directory = new File(path);
            if (!directory.exists() || !directory.isDirectory()) {
                driver.getExecutionLogReporter().error("Invalid directory: " + path);
                return false;
            }
            File filesList[] = directory.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    if (file.isFile()) {
                        if (file.delete()) {
                            driver.getExecutionLogReporter().info("Deleted file: " + file.getName());
                        } else {
                            driver.getExecutionLogReporter().error("Failed to delete file: " + file.getName());
                        }
                    }
                }
            }
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while clearing directory: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-065", groupName = "Drag and Drop", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Drag the object from its original path and drop it at the target path"))
    public boolean dynamicDragAndDrop(String replaceValue1, String xpath2, String replaceValue2) {
        boolean bstatus = false;
        try {
            String xpath1 = getObjectProperty("xpath");
            xpath1 = xpath1.replaceAll("#replace1", replaceValue1);
            xpath2 = xpath2.replaceAll("#replace2", replaceValue2);
            // webdriver.dragandDrop("xpath","xpath1","xpath2");
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter()
                    .error("Exception occurred while performing drag and drop: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "Xfit-066", groupName = "Assertions", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Gets the element length"))
    public boolean elementLength(IArgument runtime) {
        boolean bstatus = false;
        try {
            String locatorvalue = getObjectProperty("xpath");
            int SearchLength = driver.findElements(FindBy.xpath(locatorvalue)).size();
            driver.getExecutionLogReporter().info("Number of elements found: " + SearchLength);
            String lengthStr = String.valueOf(SearchLength);
            driver.getExecutionLogReporter().info("Displayed element length: " + lengthStr);
            if (runtime != null) {
                runtime.updateValue(lengthStr);
                driver.getExecutionLogReporter().info("Stored element length in runtime variable: " + runtime);
            } else {
                driver.getExecutionLogReporter().info("Runtime variable not provided, skipping storeruntime.");
            }
            bstatus = true;
            driver.getExecutionLogReporter().info("elementLength method executed successfully.");
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception occurred while getting element length: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-dynamicReadTextandStore", groupName = "Get and Store", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Reads dynamic text from a web element at runtime and stores it in a variable"))
    public boolean dynamicReadTextandStore(String replaceValue, IArgument storeValue) {
        boolean bstatus = false;
        try {
            String xpath = getObjectProperty("xpath");
            if (xpath == null || xpath.isEmpty()) {
                driver.getExecutionLogReporter().error("Locator value is null or empty");
                return false;
            }
            if (storeValue == null) {
                driver.getExecutionLogReporter().error("Runtime variable to store value is null");
                return false;
            }
            xpath = xpath.replaceAll("#replace", replaceValue);
            String a = driver.findElement(FindBy.xpath(xpath)).getText();
            storeValue.updateValue(a);
            driver.getExecutionLogReporter().info("Successfully read and stored text for: " + replaceValue);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in dynamicReadTextandStore");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-web-dynamicMousehover", groupName = "Mouse Events", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Hovers over the target element within the static element"))
    public boolean dynamicMousehover(String replaceValue) {
        boolean bstatus = false;
        try {
            String xpath = getObjectProperty("xpath");
            if (xpath == null || xpath.isEmpty()) {
                driver.getExecutionLogReporter().error("Locator value is null or empty");
                return false;
            }
            xpath = xpath.replaceAll("#replace", replaceValue);
            bstatus = driver.actions().moveToElement(FindBy.xpath(xpath));
            driver.getExecutionLogReporter().info("Successfully read and stored text for: " + replaceValue);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().info("Exception occurred in dynamicMousehover");
            driver.getExecutionLogReporter().error(e.toString());
            bstatus = false;
        }
        return bstatus;
    }

    @SyncAction(uniqueId = "custom-generic-launchUrl", groupName = "Browser Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Opens a specified URL in the browser"))
    public boolean launchUrl(String URL) {
        boolean bstatus = false;
        try {
            if (URL == null || URL.isEmpty()) {
                driver.getExecutionLogReporter().error("URL is null or empty, cannot launch.");
                return false;
            }
            driver.launchUrlAndSwitch(URL);
            driver.getExecutionLogReporter().info("Successfully launched URL: " + URL);
            bstatus = true;
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception while launching URL: " + e.toString());
            bstatus = false;
        }
        return bstatus;
    }
    @SyncAction(uniqueId = "custom-web-calculateYearCountFromDOB", groupName = "Date Handlers", objectTemplate = @ObjectTemplate(name = TechnologyType.WEB, description = "Calculates age in years from the date of birth"))
    public boolean calculateYearCountFromDOB(String dobString , IArgument Store ) {
        boolean bStatus = false;
        try {
            if (dobString == null || dobString.trim().isEmpty()) {
                driver.getExecutionLogReporter().error("DOB string is null or empty.");
                return false;
            }
            driver.getExecutionLogReporter().info("DOB Input String: " + dobString);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date dob = sdf.parse(dobString);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar currentCalendar = Calendar.getInstance();
            int years = currentCalendar.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            String yearCount = "" + years;
            Store.updateValue(yearCount);
            driver.getExecutionLogReporter().info("Calculated age in years: " + yearCount);
            driver.getExecutionLogReporter().info("Stored in runtime variable: " + Store);     
            bStatus = true;
            driver.getExecutionLogReporter().info("calculateYearCountFromDOB executed successfully.");
        } catch (Exception e) {
            driver.getExecutionLogReporter().error("Exception in calculateYearCountFromDOB: " + e.toString());
            bStatus = false;
        }
        return bStatus;
    }
    

}