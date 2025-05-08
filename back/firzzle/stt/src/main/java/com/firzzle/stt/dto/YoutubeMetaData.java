package com.firzzle.stt.dto;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YoutubeMetaData {

    private String title;
    private String channel;
    private String description;

    public YoutubeMetaData(String title,String channel, String description) {
        this.title = title;
        this.channel  = channel;
        this.description = description;
    }

    public static YoutubeMetaData fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        String title = root.path("title").asText();
        String channel = root.path("channel").asText();
        String description = root.path("description").asText();
        return new YoutubeMetaData(title,channel,description);
    }

    public String getTitle() {
        return title;
    }
    
    public String getChannel() {
        return channel;
    }

    public String getDescription() {
        return description;
    }
}
