package com.zayhu.server.linkpreview.rest.api;

import com.google.inject.Inject;
import com.zayhu.server.httpapi.rest.api.AbstractServerResource;
import com.zayhu.server.linkpreview.model.LinkPreview;
import com.zayhu.server.linkpreview.service.LinkPreviewService;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class LinkPreviewServerResource extends AbstractServerResource {
    private static final Logger sLogger = LoggerFactory.getLogger(LinkPreviewServerResource.class);
    LinkPreviewService linkPreviewService;

    @Inject
    public LinkPreviewServerResource(LinkPreviewService linkPreviewService) {
        this.linkPreviewService = linkPreviewService;
    }

    public Representation preview(Form form, Representation rep) throws IOException {
        Form post = new Form(rep);
        String url = this.getParamFirstValueOrThrow400("url",post);
        LinkPreview map = linkPreviewService.explainUrlFromCache(url);
        retrievalResponse.setResponse(map);
        return this.retrievalResponse.buildJsonResponse(pretty);
    }
}
