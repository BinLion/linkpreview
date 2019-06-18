package com.zayhu.server.linkpreview.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.model.AfterExtractor;
import us.codecraft.webmagic.model.annotation.ExtractBy;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkPreview implements AfterExtractor {
    @Indexed
    @Id
    public String url;

    @ExtractBy("//meta[@property='og:image']/@content | //img[@class='rich_pages']/@data-src")
    public String image = "";

    @ExtractBy("//meta[@property='og:title']/@content | //h2/tidyText() | //title/tidyText()")
    public String title = "";

    @ExtractBy("//h2/tidyText()")
    public String h2 = "";

    @ExtractBy("//meta[@property='og:description']/@content | //meta[@name='description']/@content")
    public String description = "";

    @ExtractBy("//meta[@property='og:local']/@content")
    public String locale = "";

    @ExtractBy("//meta[@property='og:site_name']/@content")
    public String site = "";

    @ExtractBy("//meta[@property='og:type']/@content")
    public String type = "";

    public String shorturl = "";

    @ExtractBy("//link[@rel='shortcut icon']/@href | //link[@rel='icon']/@href")
    public String favicon = "";

    @ExtractBy("//meta[@property='og:image:width']/@content")
    public int imgW = 0;

    @ExtractBy("//meta[@property='og:image:height']/@content")
    public int imgH = 0;

    public long mtime;

    public LinkPreview() {
    }

    public void afterProcess(Page page) {
        title = title.trim();
        if (StringUtils.isEmpty(title)) {
            title = h2.trim();
        }
    }
}
