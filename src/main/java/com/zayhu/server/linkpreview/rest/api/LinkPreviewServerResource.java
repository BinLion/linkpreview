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
import java.net.URL;

public class LinkPreviewServerResource extends AbstractServerResource {
    private static final Logger sLogger = LoggerFactory.getLogger(LinkPreviewServerResource.class);
    LinkPreviewService linkPreviewService;

    @Inject
    public LinkPreviewServerResource(LinkPreviewService linkPreviewService) {
        this.linkPreviewService = linkPreviewService;
    }

    public Representation preview(Form form, Representation rep) throws IOException {
        Form post = new Form(rep);
        String url = this.getParamFirstValueOrThrow400("url", post);

        try {
            URL parsedURL = new URL(url);
            String protocol = parsedURL.getProtocol().toLowerCase();
            if (!protocol.contains("http")) {
                throw new Exception("protocol is not http");
            }
            LinkPreview preview = linkPreviewService.explainUrlFromCache(url);
            retrievalResponse.setResponse(preview);
        } catch (Exception e) {
            sLogger.error("url can not be pharsed, error:{}", e.getMessage());
            retrievalResponse.setResponse(new LinkPreview());
        }

        return this.retrievalResponse.buildJsonResponse(pretty);
    }

    public Representation update(Form form, Representation rep) throws IOException {
        Form post = new Form(rep);
        String url = this.getParamFirstValueOrThrow400("url", post);

        try {
            URL parsedURL = new URL(url);
            String protocol = parsedURL.getProtocol().toLowerCase();
            if (!protocol.contains("http")) {
                throw new Exception("protocol is not http");
            }
            LinkPreview preview = linkPreviewService.updatePreviewInfo(url);
            retrievalResponse.setResponse(preview);
        } catch (Exception e) {
            sLogger.error("url can not be pharsed, error:{}", e.getMessage());
            retrievalResponse.setResponse(new LinkPreview());
        }

        return this.retrievalResponse.buildJsonResponse(pretty);
    }
}
