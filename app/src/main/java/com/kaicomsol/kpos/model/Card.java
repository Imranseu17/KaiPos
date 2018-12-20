package com.kaicomsol.kpos.model;

public class Card {

    public String label;
    public String value;

    public Card(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
