package com.zayhu.server.linkpreview.rest.api.inner;

import com.google.inject.Inject;
import com.zayhu.server.httpapi.rest.api.AbstractInnerServerResource;
import com.zayhu.server.linkpreview.model.LinkPreview;
import com.zayhu.server.linkpreview.service.LinkPreviewService;
import org.apache.commons.configuration.Configuration;
import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/*
 * @author: daisyw
 * @data: 2019/4/27 下午3:59
 */

public class LinkPreviewInnerServerResource extends AbstractInnerServerResource {
    Configuration conf;
    private static final Logger sLogger = LoggerFactory.getLogger(LinkPreviewInnerServerResource.class);
    LinkPreviewService linkPreviewService;

    @Inject
    public LinkPreviewInnerServerResource(Configuration configuration,LinkPreviewService linkPreviewService){
        conf = configuration;
        this.linkPreviewService = linkPreviewService;
    }

    public Representation getDoc5(Form form) throws IOException {
        String link = this.getParamFirstValueOrThrow400("linkUrl",form);
        LinkPreview preview = linkPreviewService.explainUrlFromCache(link);
        retrievalResponse.setResponse(preview);
        return retrievalResponse.buildJsonResponse(pretty);
    }



}
