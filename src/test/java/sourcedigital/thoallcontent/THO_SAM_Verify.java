package sourcedigital.thoallcontent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class THO_SAM_Verify 
{
	String parentTab;
	String childTab1;
	String childTab2;
	THO_SAM_Verify tsv;
	int sheetRowCount;
	int sheetFirstCell = 1;
	int sheetSecondCell = 2;
	public static void main(String[] args) throws IOException
	{
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-notifications");
		options.addArguments("headless");
		WebDriver driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		Actions actions = new Actions(driver);
		
		WebDriverWait wait = new WebDriverWait(driver,Duration.ofSeconds(20));
		
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet videoData = wb.createSheet("videoData1");
		
		THO_SAM_Verify tsv = new THO_SAM_Verify();
		
		
		//Launch URL
		driver.get("https://todayshomeowner.com/dev/thlogin/?loggedout=true&wp_lang=en_US");
		
		//Login
		driver.findElement(By.id("user_login")).sendKeys("sunil@sourcedigital.net");
		driver.findElement(By.id("user_pass")).sendKeys("kZGB1)o!U%G8rr&Hi23ZzSxM");
		driver.findElement(By.id("wp-submit")).click();
		
		//Navigating to Video-Category Page
		driver.get("https://todayshomeowner.com/dev/wp-admin/edit-tags.php?taxonomy=category&post_type=video");
//		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu-posts-video")));
//		actions.moveToElement(driver.findElement(By.id("menu-posts-video"))).click().perform();
//		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@id='menu-posts-video']/ul/li[4]/a")));
//		driver.findElement(By.xpath("//li[@id='menu-posts-video']/ul/li[4]/a")).click();
//		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//tbody[@id='the-list']/tr")));
//		tsv.hitCategory(driver, js, actions,tsv,wb,videoData,wait);
		
		//Pagination Change of Video Category 
		for(int i=11;i<=12;i++)
		{
			driver.get("https://todayshomeowner.com/dev/wp-admin/edit-tags.php?taxonomy=category&post_type=video&paged="+i);
			tsv.hitCategory(driver, js, actions,tsv,wb,videoData,wait);
		}
	}
	
	//This method hit categories one by one
	public void hitCategory(WebDriver driver,JavascriptExecutor js,Actions actions, THO_SAM_Verify tsv, XSSFWorkbook wb, XSSFSheet videoData,WebDriverWait wait) throws IOException
	{
		List<WebElement> categoryTRTag = driver.findElements(By.xpath("//tbody[@id='the-list']/tr"));
		int count=1;
		parentTab = driver.getWindowHandle();
		FileOutputStream fos = new FileOutputStream("/home/softraw/Desktop/THO5.xlsx");
		
		for(WebElement temp : categoryTRTag)
		{
			List<String> SAMReflectedTitle = new ArrayList<String>();
			List<String> SAMNotReflectedTitle = new ArrayList<String>();
			String categoryName = driver.findElement(By.xpath("//tbody[@id='the-list']/tr["+(count)+"]/td/strong/a")).getText();
			if(categoryName.equals("Home Improvement") || categoryName.equals("Podcast Episodes") || categoryName.equals("— Podcast Archive") || categoryName.equals("— Kuppersmith House Video Blog"))
			{
				System.out.println();
	        	System.out.println("Category : "+categoryName);
	        	++sheetRowCount;
	        	videoData.createRow(sheetRowCount++).createCell(0).setCellValue(categoryName);
	        	String hitView = "//tbody[@id='the-list']/tr["+count+"]/td/div[@class='row-actions']/span[@class='view']/a";
	        	String viewURL = "'"+driver.findElement(By.xpath(hitView)).getAttribute("href")+"'";
	        	js.executeScript("window.open("+viewURL+", '_blank');");
	        	Set<String> allWindows = driver.getWindowHandles();
	        	for(String childTab1 : allWindows)
	        	{
	        		if(!childTab1.equals(parentTab))
	        		{
	        			this.childTab1 = childTab1;
	        			driver.switchTo().window(this.childTab1);
	        			tsv.sortURLs(driver, js, tsv,wb,videoData,fos,wait,SAMReflectedTitle,SAMNotReflectedTitle);
	        			String cURL = driver.getCurrentUrl();
	        			for(int page=2;page<=tsv.contentPagination(driver);page++)
	        			{
	        				System.out.println("Moving to page no. "+page);
	        				String newURL = cURL+"/page/"+page+"/";
	        				driver.get(newURL);
	        				tsv.sortURLs(driver, js, tsv,wb,videoData,fos,wait,SAMReflectedTitle,SAMNotReflectedTitle);
	        			}
	        		}
	        	}
	        	count++;
	        	driver.close();
	        	driver.switchTo().window(parentTab);
	        	tsv.flushDataToExcel(SAMReflectedTitle, SAMNotReflectedTitle, videoData);
			}
		}
	}
	
	//After hitting categories this method hit on only Video Content one by one 
	public void sortURLs(WebDriver driver,JavascriptExecutor js, THO_SAM_Verify tsv, XSSFWorkbook wb, XSSFSheet videoData,FileOutputStream fos,WebDriverWait wait,List<String> SAMReflectedTitle,List<String> SAMNotReflectedTitle) throws IOException
	{
		List<WebElement> totalLinks = driver.findElements(By.xpath("//a[starts-with(@href,'https://todayshomeowner.com/dev/video/')]/img/parent::a"));
		for(WebElement temp : totalLinks)
		{
			String contentURL = "'"+temp.getAttribute("href")+"'";
			js.executeScript("window.open("+contentURL+", '_blank');");
			Set<String> allWindows = driver.getWindowHandles();
			for(String childTab2 : allWindows)
			{
				if(!childTab2.equals(parentTab) && !childTab2.equals(childTab1))
				{
					this.childTab2 = childTab2;
					driver.switchTo().window(this.childTab2);
					String entryTitle = driver.findElement(By.xpath("//h1")).getText();
					System.out.print(entryTitle);
//					XSSFRow row = videoData.createRow(sheetRowCount);
//					row.createCell(sheetFirstCell).setCellValue(entryTitle);
					tsv.verifyVideoSAM(driver,wb,videoData,fos,wait,entryTitle,SAMReflectedTitle,SAMNotReflectedTitle);
				}
			}
			System.out.println();
			driver.close();
			driver.switchTo().window(childTab1);
		}
	}
	
	//This method verifies if the SAM is injected in particular video or not
	public void verifyVideoSAM(WebDriver driver, XSSFWorkbook wb, XSSFSheet videoData,FileOutputStream fos,WebDriverWait wait,String entryTitle,List<String> SAMReflectedTitle,List<String> SAMNotReflectedTitle) throws IOException
	{
		try
		{
//			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
			driver.findElement(By.xpath("//span[@sd-interactive-meta='https://experience.sourcesync.io/723']"));
			System.out.print(" --- SAM Injected");
			SAMReflectedTitle.add(entryTitle);
//			row.createCell(sheetFirstCell).setCellValue(entryTitle);
			wb.write(fos);
		}
		catch(Exception e)
		{
			System.out.print(" --- SAM Not Injected");
			SAMNotReflectedTitle.add(entryTitle);
//			row.createCell(sheetSecondCell).setCellValue(entryTitle);
			wb.write(fos);
		}
//		sheetRowCount++;
	}
	
	//This method return pagination count after hitting any category that till how many pages video content is available
	public int contentPagination(WebDriver driver)
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
			return 0;
		}
	}
	
	//This method write data to excel sheet after one category process completion
	public void flushDataToExcel(List<String> SAMReflectedTitle,List<String> SAMNotReflectedTitle,XSSFSheet videoData)
	{
		Integer availableCount = (Integer) SAMReflectedTitle.size();
		System.out.println("Available count :"+availableCount);
		Integer notAvailableCount = (Integer) SAMNotReflectedTitle.size();
		System.out.println("Not Available count :"+notAvailableCount);
		Integer maxCount = 0;
		if(availableCount>=notAvailableCount)
			maxCount = availableCount;
		else
			maxCount = notAvailableCount;
		System.out.println("Max count :"+maxCount);
		int tempSheetRowCount = sheetRowCount;
		System.out.println("Temp Sheet Row Count : "+tempSheetRowCount);
		for(int i=tempSheetRowCount;i<=tempSheetRowCount+maxCount;i++)
		{
			System.out.println("Row Created :"+i);
			videoData.createRow(i);
		}
		
		for(String temp : SAMReflectedTitle)
		{
			System.out.println("SAM Reflected Title : "+temp);
			videoData.getRow(tempSheetRowCount).createCell(sheetFirstCell).setCellValue(temp);
			tempSheetRowCount++;
		}
		
		tempSheetRowCount = sheetRowCount;
		
		for(String temp : SAMNotReflectedTitle)
		{
			System.out.println("SAM Not Reflected Title : "+temp);
			videoData.getRow(tempSheetRowCount).createCell(sheetSecondCell).setCellValue(temp);
			tempSheetRowCount++;
		}
		sheetRowCount = sheetRowCount+maxCount;
	}
}
