package com.zayhu.server.linkpreview.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkPreview {
    @Indexed
    @Id
    public String url;
    public String image="";
    public String title="";
    public String description="";
    public String locale="";
    public String site="";
    public String type="";
    public String shorturl="";
    public String favicon = "";
    public int imgW=0;
    public int imgH=0;
    public long mtime;

    public LinkPreview() {
    }
}
