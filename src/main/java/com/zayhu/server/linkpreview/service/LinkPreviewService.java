package com.zayhu.server.linkpreview.service;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: daisyw
 * @data: 2019/4/27 下午5:26
 */
public class LinkPreviewService {


    protected static final Logger logger = LoggerFactory.getLogger(LinkPreviewService.class);
    private Configuration conf;
    WebDriver driver;
    @Inject
    public LinkPreviewService(Configuration conf){
        this.conf = conf;
        loadDriver();
    }

    public static final String[] SHORT_URL_DICT = new String[]{          //要使用生成URL的字符
            "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    private void loadDriver() {
        System.setProperty("webdriver.chrome.driver", conf.getString("webdriver.chrome.driver","/Users/daisyw/Downloads/chromedriver"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("blink-settings=imagesEnabled=false");
        driver = new ChromeDriver(options);
    }

    public Map<String,Object> explainUrl(String linkUrl) throws MalformedURLException {
        Map<String, Object> map = Maps.newHashMap();
        String title = "", description = "", image = "", locale = "", site = "",
                description2 = "", type = "", image2 = "", title2 = "";
        int width = 0, height = 0;
        URL parsedURL = new URL(linkUrl);
        String uri = parsedURL.getHost();
        Document doc =null;
        try {
            driver.get(linkUrl);
            Thread.sleep(conf.getLong("driver.link.sleep.time",3000l));
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource);
        } catch (Exception e) {
            logger.info("get doc 5 error.link:{}",linkUrl);
        }finally {
            driver.close();
        }
        if (doc != null){
            Elements metas = doc.head().select("meta");
            for (Element meta : metas) {
                if ("og:site_name".equalsIgnoreCase(meta.attr("property"))){
                    site = meta.attr("content");
                }
                if ("og:title".equalsIgnoreCase(meta.attr("property"))) {
                    title = meta.attr("content");
                }
                if ("keyword".equalsIgnoreCase(meta.attr("name"))) {
                    title2 = meta.attr("content");
                }
                if (Arrays.asList(conf.getString("explain.url.video.link","www.youtube.com,m.youtube.com").split(",")).contains(parsedURL.getHost())){
                    if ("og:type".equalsIgnoreCase(meta.attr("property"))) {
                        type = meta.attr("content");
                    }
                }
                if ("og:description".equalsIgnoreCase(meta.attr("property"))) {
                    description = meta.attr("content");
                }
                if ("description".equalsIgnoreCase(meta.attr("name"))){
                    description2 = meta.attr("content");
                }
                if ("og:image".equalsIgnoreCase(meta.attr("property"))) {
                    image = meta.attr("content");
                }
                if ("og:locale".equalsIgnoreCase(meta.attr("property"))) {
                    locale = meta.attr("content");
                }
                if ("image".equalsIgnoreCase(meta.attr("itemprop"))){
                    image2 = meta.attr("content");
                }
                if ("og:image:width".equalsIgnoreCase(meta.attr("property"))){
                    if (StringUtils.isNotEmpty(meta.attr("content")))
                        width = Integer.valueOf(meta.attr("content"));
                }
                if ("og:image:height".equalsIgnoreCase(meta.attr("property"))){
                    if (StringUtils.isNotEmpty(meta.attr("content")))
                        height = Integer.valueOf(meta.attr("content"));
                }
            }
            if (StringUtils.isEmpty(title)) { title = title2; }
            if (StringUtils.isEmpty(description)) { description = description2; }
            if (StringUtils.isEmpty(title)) {
                title = doc.title();
                try {
                    for (int i = 0; i < 3; i++) {
                        if (ifEncode(title)) {
                            title = URLDecoder.decode(title, "UTF8");
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.info("url encoede error,title:{}", doc.title());
                    title = doc.title();
                }
            }
        }
        image = fixImage(image, parsedURL);
        image2 = fixImage(image2, parsedURL);
        if (StringUtils.isEmpty(image)) {
            image = image2;
        }
        //amazon image
        image = addAmazonImage(image,doc,uri);
        if (image.startsWith("{")) {
            String[] arr = image.split("\"");
            if (arr.length == 3) {
                image = arr[1];
                String[] arrSize = arr[2].split(",");
                if (arrSize.length == 2) {
                    width = Integer.valueOf(arrSize[0].substring(2));
                    height = Integer.valueOf(arrSize[1]);
                }
            }
        }
        if (width !=0 && height !=0){
            map.put("imgW",width);
            map.put("imgH",height);
        }
        if(StringUtils.isEmpty(site)){ site = uri; }
        map.put("title",title);
        map.put("description",description);
        map.put("image",image);
        map.put("url",linkUrl);
        map.put("locale",locale);
        map.put("site",site);
        map.put("type",type);
        map.put("shorturl",getShortUrl(parsedURL.toString()));
        map.put("favicon",parsedURL.getProtocol()+"://"+parsedURL.getHost()+"/favicon.ico");
        return map;
    }

    public static Boolean ifEncode(String encodeValue){
        Pattern pattern = Pattern.compile(".*%[a-fA-F0-9]{2}.*");
        Matcher matcher = pattern.matcher(encodeValue);
        if(matcher.matches()){
            return true;
        }else {
            return false;
        }
    }

    private String addAmazonImage(String image,Document doc,String uri) {
        if (StringUtils.isEmpty(image) && Arrays.asList(conf.getString("amazon.image.url","www.amazon.com,www.amazon.cn").split(",")).contains(uri)){
            Element landImg = doc.body().getElementById("landingImage");
            if (landImg == null){
                landImg = doc.body().getElementById("imgBlkFront");
            }
            if (landImg != null){
                return landImg.attr("data-a-dynamic-image").split("]")[0];
            }
        }
        return image;
    }

    public  String getShortUrl(String url) {
        String md5 = DigestUtils.md5Hex("yeetoken" + url);
        int start = 0;
        String sub = md5.substring(start, start + 8);
        long idx = Long.valueOf("3FFFFFFF", 16) & Long.valueOf(sub, 16);
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < 6; k++) {
            int index = (int) (Long.valueOf("0000003D", 16) & idx);
            sb.append(SHORT_URL_DICT[index]);
            idx = idx >> 5;
        }
        return conf.getString("shorturl.base","http://in.debug.yeecall.com:5080/short/")+sb.toString();
    }

    private static String fixImage(String image,URL uri){
        if (StringUtils.isNotEmpty(image) && !image.startsWith("http")){
            if (image.startsWith("//")){
                image = uri.getProtocol()+":"+image;
            }else if (image.startsWith("/")){
                image = uri.getProtocol()+"://"+ uri.getHost() +image;
            }
        }
        return image;
    }


    public static void main(String[] args) throws MalformedURLException {
        String url= "http://app.cntv.cn/special/cportal/columnv722/index.html?id=21289b31793348fca42cb8ec29caf877&columnID=ColuYwc9FPZgzisJl9UMhyUb160812&fromapp=cctvnews&from=timeline&isappinstalled=0";
        URL newUrl = new URL(url);
        System.out.println(newUrl.getAuthority());
        System.out.println(newUrl.getHost());
        System.out.println(newUrl.getPath());
        System.out.println(newUrl.getProtocol());



    }

}
