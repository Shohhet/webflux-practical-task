package com.shoggoth.webfluxfileserver.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Data
public class UploadState {
    private String fileName;
    private String contentType;
    private String uploadId = "";
    private int partCounter = 0;
    private int buffered = 0;
    private Map<Integer, CompletedPart> completedParts = new HashMap<>();

    public void addBuffered(int buffered) {
        this.buffered += buffered;
    }

    public int getIncPartCounter() {
        return ++this.partCounter;
    }

}
