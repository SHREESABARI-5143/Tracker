package com.chronotrack.dto;

public class InsightResponse {

    private String id;
    private String type;     // SUCCESS, INFO, WARNING, ACHIEVEMENT
    private String category; // LEARNING, PRODUCTIVITY, GOAL, HABIT
    private String title;
    private String message;
    private String metric;
    private String icon;

    public InsightResponse() {
    }

    public InsightResponse(String id, String type, String category, String title, String message, String metric, String icon) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.title = title;
        this.message = message;
        this.metric = metric;
        this.icon = icon;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
