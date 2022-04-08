package sourcedigital.thoallcontent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SortingOfTHOVideoAndNonVideoContent 
{
	static ChromeOptions options;
	static WebDriver driver;
	static WebDriverWait wait;
	static JavascriptExecutor js;
	int videoRowCount=0;
	int nonVideoRowCount=0;
	
	public static void main(String[] args) throws IOException
	{
		WebDriverManager.chromedriver().setup();
		options = new ChromeOptions();
		options.addArguments("--disable-notifications");
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		
		wait = new WebDriverWait(driver,Duration.ofSeconds(10));
		
		js = (JavascriptExecutor) driver;
		
//		Properties p = new Properties();
//		FileInputStream fis = new FileInputStream("/home/softraw/eclipse-workspace1/lambdatest/imp.properties");
//		p.load(fis);
		
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet videoSheet = wb.createSheet("4seasonsVideoSheet");
		XSSFSheet nonVideoSheet = wb.createSheet("4seasonsNonVideoSheet");
		
		driver.get("https://todayshomeowner.com/dev/category/4seasons/");
		driver.findElement(By.id("password_protected_pass")).sendKeys("TEST");
		driver.findElement(By.id("wp-submit")).click();
		js.executeScript("window.scrollBy(0,1000)", "");
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='_close']/i")));
		driver.findElement(By.xpath("//div[@class='_close']/i")).click();
		
		SortingOfTHOVideoAndNonVideoContent tho = new SortingOfTHOVideoAndNonVideoContent();
		System.out.println("Total paginations :"+tho.pagination());
		String cURL = driver.getCurrentUrl();
		tho.sortURLs(videoSheet,nonVideoSheet,wb);
		for(int page=2;page<=tho.pagination();page++)
		{
			String newURL = cURL+"/page/"+page+"/";
			driver.get(newURL);
			tho.sortURLs(videoSheet,nonVideoSheet,wb);
		}
	}
	
	public int pagination()
	{
		try
		{
		driver.findElement(By.xpath("//div[@class='page-nav td-pb-padding-side']"));
		List<WebElement> toalAnchorTagCount = driver.findElements(By.xpath("//div[@class='page-nav td-pb-padding-side']/a"));
		int lastPageCount = Integer.parseInt(driver.findElement(By.xpath("//div[@class='page-nav td-pb-padding-side']/a["+(toalAnchorTagCount.size()-1)+"]")).getAttribute("title"));
		return lastPageCount;
		}
		catch(Exception e)
		{
			System.out.println("Pagination not available...");
			return 0;
		}
	}
	
	public void sortURLs(XSSFSheet videoSheet,XSSFSheet nonVideoSheet,XSSFWorkbook wb) throws IOException
	{
		List<WebElement> totalLinks = driver.findElements(By.xpath("//div[@class='td_module_1 td_module_wrap td-animation-stack td-meta-info-hide']/div/div/a"));
		for(WebElement temp : totalLinks)
		{
			String url = temp.getAttribute("href");
			String title = temp.getAttribute("title");
			String videoURL = "https://todayshomeowner.com/dev/video/";
			String matchedString = null;
			try
			{
				matchedString = url.substring(0,38);
			}
			catch(Exception e)
			{
				XSSFRow row = nonVideoSheet.createRow(nonVideoRowCount);
				row.createCell(0).setCellValue(url);
				row.createCell(1).setCellValue(title);
				nonVideoRowCount++;
			}
			if(videoURL.equals(matchedString))
			{
				XSSFRow row = videoSheet.createRow(videoRowCount);
				row.createCell(0).setCellValue(url);
				row.createCell(1).setCellValue(title);
				videoRowCount++;
			}
			else
			{
				XSSFRow row = nonVideoSheet.createRow(nonVideoRowCount);
				row.createCell(0).setCellValue(url);
				row.createCell(1).setCellValue(title);
				nonVideoRowCount++;
			}
		}
		FileOutputStream fos = new FileOutputStream("/home/softraw/Desktop/4seasons.xlsx");
		wb.write(fos);
	}
}
