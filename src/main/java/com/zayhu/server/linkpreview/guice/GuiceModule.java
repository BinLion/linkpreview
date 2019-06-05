package com.zayhu.server.linkpreview.guice;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * @author: daisyw
 * @data: 2019/4/27 下午2:06
 */
public class GuiceModule extends com.yeecall.yeetoken.yeeapi.guice.GuiceModule {


    protected void loadMoreConf(CompositeConfiguration conf) throws ConfigurationException {
        super.loadMoreConf(conf);
        loadConf(conf, "linkpreview.properties");
    }

    public GuiceModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
    }

    @Provides
    @Named("chrome")
    @Singleton
    public WebDriver provideWebDriver(Configuration conf) {
        System.setProperty("webdriver.chrome.driver", conf.getString("webdriver.chrome.driver","/Users/daisyw/Downloads/chromedriver"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("blink-settings=imagesEnabled=false");
        WebDriver driver = new ChromeDriver(options);

        return driver;
    }

}
