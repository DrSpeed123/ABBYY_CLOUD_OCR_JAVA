package com.abbyy.ocrsdk;

/**
 * Created by Анечка on 04.07.2017.
 */
public class WordDto {
    private String value;
    private Boolean suspicious;

    public WordDto(String value, Boolean suspicious) {
        this.value = value;
        this.suspicious = suspicious;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSuspicious() {
        return suspicious;
    }

    public void setSuspicious(Boolean suspicious) {
        this.suspicious = suspicious;
    }
}
