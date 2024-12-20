package com.colorpl.show.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Category {
    PLAY("연극"),
    MOVIE("영화"),
    PERFORMANCE("공연"),
    CONCERT("콘서트"),
    MUSICAL("뮤지컬"),
    EXHIBITION("전시회"),
    ETC("기타");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    private static final Map<String, Category> stringToEnum = new HashMap<>();

    static {
        for (Category e : values()) {
            stringToEnum.put(e.toString(), e);
            stringToEnum.put(e.name(), e);
        }
        stringToEnum.put("무용", PERFORMANCE);
        stringToEnum.put("대중무용", PERFORMANCE);
        stringToEnum.put("서양음악(클래식)", CONCERT);
        stringToEnum.put("한국음악(국악)", CONCERT);
        stringToEnum.put("대중음악", CONCERT);
        stringToEnum.put("복합", ETC);
        stringToEnum.put("서커스/마술", PERFORMANCE);
    }

    public static Optional<Category> fromString(String name) {
        return Optional.ofNullable(stringToEnum.get(name));
    }
}
