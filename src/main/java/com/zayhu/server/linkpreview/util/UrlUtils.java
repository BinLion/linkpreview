package com.zayhu.server.linkpreview.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    // short characters
    public static final String[] SHORT_URL_DICT = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p",
            "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    public static String convertUrl(String url) {
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
        return sb.toString();
    }

    public static String getShortUrl(String url, String baseUrl) {
        return baseUrl + convertUrl(url);
    }

    public static String getHostFromUrl(String url) throws MalformedURLException {
        URL parsedURL = new URL(url);
        return parsedURL.getHost();
    }

    public static String fixImageUrl(String image, URL parsedUrl) {
        if (StringUtils.isNotEmpty(image) && !image.startsWith("http")) {
            if (image.startsWith("//")) {
                image = parsedUrl.getProtocol() + ":" + image;
            } else if (image.startsWith("/")) {
                image = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + image;
            }
        }
        return image;
    }
}
