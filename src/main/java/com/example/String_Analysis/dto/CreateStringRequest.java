package com.example.String_Analysis.dto;

public class CreateStringRequest {
    private String value;

    public CreateStringRequest() {}

    public CreateStringRequest(String value) { this.value = value; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
