package com.supermarket.model;

public enum Category {
    GROCERY("Grocery & Staples", "🌾"),
    DAIRY_BAKERY("Dairy & Bakery", "🥛"),
    BEVERAGES("Beverages & Drinks", "🧃"),
    SNACKS("Snacks & Confectionery", "🍪"),
    FRUITS_VEGGIES("Fruits & Vegetables", "🍎"),
    PERSONAL_CARE("Personal Care & Hygiene", "🧴"),
    HOUSEHOLD("Household & Cleaning", "🧼");

    private final String displayName;
    private final String emoji;

    Category(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }
}

