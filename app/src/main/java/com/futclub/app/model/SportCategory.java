package com.futclub.app.model;

import com.google.gson.annotations.SerializedName;

public class SportCategory {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon_name")
    private String iconName;

    // Field ini BUKAN dari API, dipakai cuma di UI untuk menandai "lagi dipilih atau tidak"
    private transient boolean selected = false;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIconName() { return iconName; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
