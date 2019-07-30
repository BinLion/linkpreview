package com.zayhu.server.linkpreview.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.mongodb.Mongo;
import com.mongodb.WriteResult;
import com.zayhu.server.httpapi.service.SimpleDAO;
import com.zayhu.server.linkpreview.model.LinkPreview;
import com.zayhu.server.linkpreview.util.UrlUtils;
import com.zayhu.server.redis.RedisService;
import com.zayhu.server.spider.SimpleHttpClient;
import com.zayhu.server.util.ConfigUtils;
import com.zayhu.server.util.StatLogger;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Site;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
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

    @Inject
    public LinkPreviewService(@Named("zayhu") Mongo m, Morphia mor, @Named("zayhu") RedisService redisService, Configuration conf) {
        this.conf = conf;
        this.redisService = redisService;
        final String dbName = ConfigUtils.getDefDBName(conf);
        this.linkPreviewDAO = new SimpleDAO(LinkPreview.class, m, mor, dbName);
    }

    public LinkPreview explainUrlFromCache(String linkUrl) throws IOException {
        Long start = System.currentTimeMillis();
        linkUrl = linkUrl.replace("\\", "");
        LinkPreview link = redisService.get(LINK_URL_CACHE + linkUrl, LinkPreview.class);
        String from = "redis";
        if (link == null || StringUtils.isEmpty(link.title)) {
            from = "mongo";
            link = linkPreviewDAO.createQuery().field("url").equal(linkUrl).get();
            if (link == null) {
                from = "http";
                link = getPreviewInfo(linkUrl);
                if (link != null && StringUtils.isNotEmpty(link.title)) {
                    linkPreviewDAO.save(link);
                }
            }
            redisService.set(LINK_URL_CACHE + linkUrl, link);
            redisService.expire(LINK_URL_CACHE + linkUrl, conf.getInt("explain.url.expire.time", 600));
        }
        Long cost = System.currentTimeMillis() - start;
        StatLogger.info("link preview request.host:{},url:{},from:{},cost:{}", new URL(linkUrl).getHost(), linkUrl, from, cost);
        return link;
    }

    public LinkPreview getPreviewInfo(String url) throws MalformedURLException {
        URL parsedURL = new URL(url);
        String host = parsedURL.getHost();
        int timeout = conf.getInt("spider.timeout", 20000);
        HashSet<Integer> statCodes = new HashSet<>();
        statCodes.add(200);

        Site site = Site.me().setUserAgent("WhatsApp/2.19.50 i").addHeader("Referer", url).setTimeOut(timeout).setAcceptStatCode(statCodes);
        if (Arrays.asList(conf.getString("spider.use.chrome.hosts","www.bilibili.com,m.bilibili.com,www.baidu.com,m.baidu.com").split(",")).contains(host)) {
            site.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        }
        SimpleHttpClient client = new SimpleHttpClient(site);

        LinkPreview preview = null;
        try {
            preview = client.get(url, LinkPreview.class);
        } catch (Exception e) {
            logger.error("get preview fail. error:{}", e.getMessage());
            return new LinkPreview();
        }

        if (preview == null) {
            preview = new LinkPreview();
            //preview.title = parsedURL.getPath();
        }

        try {
            for (int i = 0; i < 3; i++) {
                if (ifEncode(preview.title)) {
                    preview.title = URLDecoder.decode(preview.title, "UTF8");
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.info("url encoede error,title:{}", preview.title);
        }

        preview.url = url;
        preview.shorturl = UrlUtils.getShortUrl(url, conf.getString("shorturl_base", "http://in.debug.yeecall.com:5080/short/"));

        if (StringUtils.isEmpty(preview.site)) {
            preview.site = host;
        }

        if (StringUtils.isEmpty(preview.favicon)) {
            preview.favicon = parsedURL.getProtocol() + "://" + host + "/favicon.ico";
        }
        preview.favicon = UrlUtils.fixImageUrl(preview.favicon, parsedURL);

        return preview;
    }

    public LinkPreview updatePreviewInfo(String url) {
        LinkPreview preview = null;
        try {
            preview = getPreviewInfo(url);
        } catch (MalformedURLException e) {
            logger.error("updatePreviewInfo. error:{}", e.getMessage());
            e.printStackTrace();
        }

        if (preview != null) {
            setPreviewToRedisCache(url, preview);
            updatePreviewToMongo(preview);
        }

        return preview;
    }

    private void setPreviewToRedisCache(String url, LinkPreview linkPreview) {
        url = url.replace("\\", "");
        try {
            redisService.set(LINK_URL_CACHE + url, linkPreview);
            redisService.expire(LINK_URL_CACHE + url, conf.getInt("explain.url.expire.time", 600));
        } catch (IOException e) {
            logger.error("setPreviewToRedisCache. error:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePreviewToMongo(LinkPreview linkPreview) {
        if (StringUtils.isEmpty(linkPreview.title)) {
            WriteResult delete = linkPreviewDAO.delete(linkPreview);
            logger.info("updatePreviewToMongo. deleted:{}", delete.getN());
        } else {
            UpdateResults updateResults = linkPreviewDAO.updateOrCreate(linkPreviewDAO.createQuery().field("url").equal(linkPreview.url), linkPreview);
            logger.info("updatePreviewToMongo. inserted:{}, updated:{}", updateResults.getInsertedCount(), updateResults.getUpdatedCount());
        }
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

    public static void main(String[] args) throws MalformedURLException {
        String url = "http://app.cntv.cn/special/cportal/columnv722/index.html?id=21289b31793348fca42cb8ec29caf877&columnID=ColuYwc9FPZgzisJl9UMhyUb160812&fromapp=cctvnews&from=timeline&isappinstalled=0";
        URL newUrl = new URL(url);
        System.out.println(newUrl.getAuthority());
        System.out.println(newUrl.getHost());
        System.out.println(newUrl.getPath());
        System.out.println(newUrl.getProtocol());

    }

}
