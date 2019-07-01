package com.example.wasteclassifier;

public class ImageAndType {
    private String id;
    private String imageUrl;
    private String wasteType;

    public ImageAndType(){}


    public ImageAndType(String id, String imageUrl, String wasteType) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.wasteType = wasteType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }
}
