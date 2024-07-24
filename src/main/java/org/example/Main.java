package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

class TestData {
    String amount;

    TestData() {}
    TestData(String amount) {
        this.amount = amount;
    }
}

public class Main {

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver","C:\\Users\\huany\\Downloads\\chromedriver-win32\\chromedriver.exe");

        // Define test data
        List<TestData> testDataList = Arrays.asList(
                new TestData("100"),
                new TestData("0"),
                new TestData("-100"),
                new TestData("abcdefg")
        );

        // Run tests
        for (TestData testData : testDataList) {
            runTest(testData);
        }
    }

    private static void runTest(TestData testData) {
        WebDriver driver = new ChromeDriver();
        driver.get("https://www.xe.com/");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        tryCheckValidConversion(driver, testData.amount);
        driver.quit();
    }

    //FUNCTION
    private static void tryCheckValidConversion(WebDriver driver, String amount) {
        Duration timeout = Duration.ofSeconds(10);
        WebDriverWait wait = new WebDriverWait(driver, timeout);

        WebElement fromCurrencyInput = driver.findElement(By.id("midmarketFromCurrency"));
        String fromCurrencyInputText = fromCurrencyInput.getText();

        WebElement toCurrencyInput = driver.findElement(By.id("midmarketToCurrency"));
        String toCurrencyInputText = toCurrencyInput.getText();
        WebElement amountInput = driver.findElement(By.xpath("//*[@id=\"amount\"]"));

        amountInput.clear();
        amountInput.sendKeys(amount);

        //TEST 5: CHECK is Digit Amount
        if (!isDigit(amount)) {
            try {
                WebElement convertButton = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/button"));
                if (!convertButton.isEnabled()) {
                    System.out.println("Test 5 PASS: Amount is not numeric. Convert Button is disabled. Cannot perform conversion with non-numeric amount.");
                } else {
                    System.out.println("Test 5 FAIL: Amount is not numeric. Convert button is enabled.");
                }
            } catch (Exception e) {
                System.out.println("Test 5: ERROR");
            }
        } else {
            double amountValue = Double.parseDouble(amount);

            //TEST 3: CHECK Amount = 0
            if (amountValue == 0) {
                try {
                    WebElement convertButton = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/button"));
                    if (!convertButton.isEnabled()) {
                        System.out.println("Test 3 PASS: Amount is 0. Convert Button is disabled. Cannot perform conversion with 0 amount.");
                    } else {
                        System.out.println("Test 3 FAIL: Amount is 0. Convert button is enabled.");
                    }
                } catch (Exception e) {
                    System.out.println("Test 3: ERROR");
                }

                //TEST 4: CHECK Amount -ve
            } else if (amountValue < 0) {
                try {
                    WebElement convertButton = driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/button"));
                    if (!convertButton.isEnabled()) {
                        System.out.println("Test 4 PASS: Amount is -ve. Convert Button is disabled. Cannot perform conversion with -ve amount.");
                    } else {
                        System.out.println("Test 4 FAIL: Amount is -ve. Convert button is enabled.");
                    }
                } catch (Exception e) {
                    System.out.println("Test 4: ERROR");
                }

            } else {
                //TEST 1: CHECK Convert USD to EUR
                driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/button")).click();

                try {
                    // explicit wait
                    // command to wait until the element becomes visible
                    //Result Text
                    WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/div[1]/div/p[2]")));

                    String actualResultText = result.getText();
                    double actualResult = Double.parseDouble(actualResultText.replaceAll("[^\\d.]", ""));

                    // Exchange rate
                    WebElement rate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[3]/section/div/div[1]/table/tbody/tr[1]/td[2]")));
                    String rateText = rate.getText().replaceAll("[^\\d.]", "");
                    double exchangeRate = Double.parseDouble(rateText);

                    double expectedResult = amountValue * exchangeRate;

                    if (Math.abs(actualResult - expectedResult) < 0.01) {
                        System.out.println("Test 1 PASS: Valid Currency Conversion from " + fromCurrencyInputText + " to " + toCurrencyInputText + " with exchange rate: " + exchangeRate);
                    } else {
                        System.out.println("Test 1 FAIL: Expected result (" + expectedResult + ") does not match the actual result (" + actualResult + ").");
                    }

                    //TEST 2: CHECK Convert USD to EUR after click exchange button
                    if (amountValue > 0) {
                        driver.findElement(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[1]/div[6]/button")).click();

                        WebElement newRate = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[3]/section/div/div[1]/table/tbody/tr[1]/td[2]")));
                        String newRateText = newRate.getText().replaceAll("[^\\d.]", "");
                        double newExchangeRate = Double.parseDouble(newRateText);

                        WebElement newResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"__next\"]/div/div[4]/div[2]/section/div[2]/div/main/div/div[2]/div[1]/div/p[2]")));
                        String newActualResultText = newResult.getText();

                        double newActualResult = Double.parseDouble(newActualResultText.replaceAll("[^\\d.]", ""));
                        double newExpectedResult = amountValue * newExchangeRate;

                        System.out.println("New actual result: " + newActualResultText);
                        System.out.println("New exchange rate: " + newRateText);

                        if (Math.abs(newActualResult - newExpectedResult) < 0.01) {
                            System.out.println("Test 2 PASS: New conversion amount is correct after clicking the exchange button.");
                        } else {
                            System.out.println("Test 2 FAIL: Expected result (" + newExpectedResult + ") does not match the actual result (" + newActualResult + ") after clicking the exchange button.");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isDigit(String str) {
        if (str.isEmpty()) {
            return false;
        }
        int start = 0;
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {  //if '-' only not valid
                return false;
            }
            start = 1;  // Start checking digits from the next character.
        }
        for (int i = start; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
//
//private static boolean isNumeric(String str) {
//    if (str == null || str.isEmpty()) {
//        return false;
//    }
//    int start = (str.charAt(0) == '-') ? 1 : 0;
//    for (int i = start; i < str.length(); i++) {
//        if (!Character.isDigit(str.charAt(i))) {
//            return false;
//        }
//    }
//    return true;
//}