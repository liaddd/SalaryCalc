package com.liad.salarycalc.entities;

public class MainCardItem {

    private int background;
    private String title , subtitle;


    public MainCardItem(String title, String subtitle, int background) {
        this.title = title;
        this.subtitle = subtitle;
        this.background = background;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getBackground() {
        return background;
    }
}
