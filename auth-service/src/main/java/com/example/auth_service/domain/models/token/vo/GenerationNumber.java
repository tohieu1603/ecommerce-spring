package com.example.auth_service.domain.models.token.vo;


/**
 * Value Object: monotonically increasing rotation depth with a token family
 * 
 * Root token starts at 0. Each successful rotation increments by 1
 * A gap or replay of an older generation signals a potential token theft attack
 */

public record GenerationNumber(int value) {
    public GenerationNumber(int value) {
        if(value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
        this.value = value;
    }
    public static GenerationNumber of(int value) {
        return new GenerationNumber(value);
    }
    public static GenerationNumber root() {
        return new GenerationNumber(0);
    }
    public boolean isRoot() {
        return value == 0;
    }
    public GenerationNumber next() {
        if(value == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("value must be less than or equal to Integer.MAX_VALUE");
        }
        return new GenerationNumber(value + 1);
    }
    public boolean isAfter(GenerationNumber other) {
        return value > other.value;
    }
    public boolean isBefore(GenerationNumber other) {
        return value < other.value;
    }
    public boolean isSame(GenerationNumber other) {
        return value == other.value;
    }
    public int difference(GenerationNumber other) {
        return value - other.value;
    }
}