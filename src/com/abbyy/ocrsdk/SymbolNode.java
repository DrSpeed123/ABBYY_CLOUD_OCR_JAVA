package com.abbyy.ocrsdk;

/**
 * Created by Анечка on 03.07.2017.
 */
public class SymbolNode {
    private String value;
    private int weight;
    private int total;

    public SymbolNode(String value, int weight, int total) {
        this.value = value;
        this.weight = weight;
        this.total = total;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
