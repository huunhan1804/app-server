package com.example.shoppingsystem.enums;

import java.util.stream.Stream;

public enum Rating {
    ONE_STAR(1),
    TWO_STARS(2),
    THREE_STARS(3),
    FOUR_STARS(4),
    FIVE_STARS(5);

    private final int value;

    Rating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Rating fromValue(int value) {
        return Stream.of(Rating.values())
                .filter(rating -> rating.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid rating value: " + value));
    }
}
