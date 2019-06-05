package com.zayhu.server.linkpreview.service;

import org.jsoup.nodes.Document;

public interface LinkDocument {
    public Document getDocument(String url);
}
