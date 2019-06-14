package com.zayhu.server.linkpreview.service;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.Mongo;
import com.zayhu.server.httpapi.service.SimpleDAO;
import com.zayhu.server.linkpreview.model.LinkPreview;
import com.zayhu.server.linkpreview.util.UrlUtils;
import com.zayhu.server.redis.RedisService;
import com.zayhu.server.spider.SimpleHttpClient;
import com.zayhu.server.util.ConfigUtils;
import com.zayhu.server.util.JsonUtils;
import com.zayhu.server.util.StatLogger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mongodb.morphia.Morphia;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Site;

import java.io.IOException;
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

    private static final String LINK_URL_CACHE = "linkpreview_cache_";
    protected SimpleDAO<LinkPreview, String> linkPreviewDAO;
    RedisService redisService;
    protected static final Logger logger = LoggerFactory.getLogger(LinkPreviewService.class);
    private Configuration conf;
    private WebDriver driver;

    @Inject
    public LinkPreviewService(@Named("zayhu") Mongo m, Morphia mor, @Named("zayhu") RedisService redisService, Configuration conf, @Named("chrome") WebDriver driver) {
        this.conf = conf;
        this.redisService = redisService;
        final String dbName = ConfigUtils.getDefDBName(conf);
        this.linkPreviewDAO = new SimpleDAO(LinkPreview.class, m, mor, dbName);
        this.driver = driver;

    }

    public LinkPreview explainUrlFromCache(String linkUrl) throws IOException {

        LinkPreview link = redisService.get(LINK_URL_CACHE + linkUrl, LinkPreview.class);
        boolean ifcache = true;
        if (link == null || StringUtils.isEmpty(link.title)) {
            ifcache = false;
            link = linkPreviewDAO.createQuery().field("url").equal(linkUrl).get();
            if (link == null) {
                link = getPreviewInfo(linkUrl);
                if (link != null && StringUtils.isNotEmpty(link.title)) {
                    link.mtime = System.currentTimeMillis();
                    linkPreviewDAO.save(link);
                }
            }
            redisService.set(LINK_URL_CACHE + linkUrl, link);
            redisService.expire(LINK_URL_CACHE + linkUrl, conf.getInt("explain.url.expire.time", 60));
        }
        StatLogger.info("link preview request.uri:{},url:{},ifcache:{}", new URL(linkUrl).getHost(), linkUrl, ifcache);
        return link;
    }

    public LinkPreview getPreviewInfo(String url) throws MalformedURLException {
        Site site = Site.me().setUserAgent("WhatsApp/2.19.50 i").addHeader("Referer", url);
        SimpleHttpClient client = new SimpleHttpClient(site);
        LinkPreview preview = client.get(url, LinkPreview.class);

        URL parsedURL = new URL(url);
        String host = parsedURL.getHost();

        if (preview == null) {
            preview = new LinkPreview();
            preview.title = host;
        }

        preview.url = url;
        preview.shorturl = UrlUtils.getShortUrl(url, conf.getString("shorturl_base", "http://in.debug.yeecall.com:5080/short/"));

        if (StringUtils.isEmpty(preview.site)) {
            preview.site = host;
        }

        if (StringUtils.isEmpty(preview.favicon)) {
            preview.favicon = parsedURL.getProtocol() + "://" + host + "/favicon.ico";
        }

        return preview;
    }

    public Map<String, Object> explainUrl(String linkUrl) throws MalformedURLException {
        Map<String, Object> map = Maps.newHashMap();
        String title = "", description = "", image = "", locale = "", site = "",
                description2 = "", type = "", image2 = "", title2 = "";
        int width = 0, height = 0;
        URL parsedURL = new URL(linkUrl);
        String uri = parsedURL.getHost();
        Document doc = null;
        try {
            driver.get(linkUrl);
            Thread.sleep(conf.getLong("driver.link.sleep.time", 3000l));
            String pageSource = driver.getPageSource();
            doc = Jsoup.parse(pageSource);
        } catch (Exception e) {
            logger.info("get doc 5 error.link:{}", linkUrl);
        }

        if (doc != null) {
            Elements metas = doc.head().select("meta");
            for (Element meta : metas) {
                if ("og:site_name".equalsIgnoreCase(meta.attr("property"))) {
                    site = meta.attr("content");
                }
                if ("og:title".equalsIgnoreCase(meta.attr("property"))) {
                    title = meta.attr("content");
                }
                if ("title".equalsIgnoreCase(meta.attr("name"))) {
                    title2 = meta.attr("content");
                }
                if (Arrays.asList(conf.getString("explain.url.video.link", "www.youtube.com,m.youtube.com").split(",")).contains(parsedURL.getHost())) {
                    if ("og:type".equalsIgnoreCase(meta.attr("property"))) {
                        type = meta.attr("content");
                    }
                }
                if ("og:description".equalsIgnoreCase(meta.attr("property"))) {
                    description = meta.attr("content");
                }
                if ("description".equalsIgnoreCase(meta.attr("name"))) {
                    description2 = meta.attr("content");
                }
                if ("og:image".equalsIgnoreCase(meta.attr("property"))) {
                    image = meta.attr("content");
                }
                if ("og:locale".equalsIgnoreCase(meta.attr("property"))) {
                    locale = meta.attr("content");
                }
                if ("image".equalsIgnoreCase(meta.attr("itemprop"))) {
                    image2 = meta.attr("content");
                }
                if ("og:image:width".equalsIgnoreCase(meta.attr("property"))) {
                    if (StringUtils.isNotEmpty(meta.attr("content")))
                        width = Integer.valueOf(meta.attr("content"));
                }
                if ("og:image:height".equalsIgnoreCase(meta.attr("property"))) {
                    if (StringUtils.isNotEmpty(meta.attr("content")))
                        height = Integer.valueOf(meta.attr("content"));
                }
            }
            if (StringUtils.isEmpty(title)) {
                title = title2;
            }
            if (StringUtils.isEmpty(description)) {
                description = description2;
            }
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
        image = addAmazonImage(image, doc, uri);
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
        if (width != 0 && height != 0) {
            map.put("imgW", width);
            map.put("imgH", height);
        }
        if (StringUtils.isEmpty(site)) {
            site = uri;
        }
        map.put("title", title);
        map.put("description", description);
        map.put("image", image);
        map.put("url", linkUrl);
        map.put("locale", locale);
        map.put("site", site);
        map.put("type", type);
        map.put("shorturl", UrlUtils.getShortUrl(linkUrl, conf.getString("shorturl_base", "http://in.debug.yeecall.com:5080/short/")));
        map.put("favicon", parsedURL.getProtocol() + "://" + parsedURL.getHost() + "/favicon.ico");
        return map;
    }

    public static Boolean ifEncode(String encodeValue) {
        Pattern pattern = Pattern.compile(".*%[a-fA-F0-9]{2}.*");
        Matcher matcher = pattern.matcher(encodeValue);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    private String addAmazonImage(String image, Document doc, String uri) {
        if (StringUtils.isEmpty(image) && Arrays.asList(conf.getString("amazon.image.url", "www.amazon.com,www.amazon.cn").split(",")).contains(uri)) {
            Element landImg = doc.body().getElementById("landingImage");
            if (landImg == null) {
                landImg = doc.body().getElementById("imgBlkFront");
            }
            if (landImg != null) {
                return landImg.attr("data-a-dynamic-image").split("]")[0];
            }
        }
        return image;
    }

    private static String fixImage(String image, URL uri) {

        return image;
    }


    public static void main(String[] args) throws MalformedURLException {
        String url = "http://app.cntv.cn/special/cportal/columnv722/index.html?id=21289b31793348fca42cb8ec29caf877&columnID=ColuYwc9FPZgzisJl9UMhyUb160812&fromapp=cctvnews&from=timeline&isappinstalled=0";
        URL newUrl = new URL(url);
        System.out.println(newUrl.getAuthority());
        System.out.println(newUrl.getHost());
        System.out.println(newUrl.getPath());
        System.out.println(newUrl.getProtocol());

    }

}
