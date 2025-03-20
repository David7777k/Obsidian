package net.optifine.util;

import lombok.Getter;

import java.util.Optional;

@Getter
public class Either<L, R> {
    private final Optional<L> left;
    private final Optional<R> right;

    private Either(Optional<L> leftIn, Optional<R> rightIn) {
        this.left = leftIn;
        this.right = rightIn;

        if (!this.left.isPresent() && !this.right.isPresent()) {
            throw new IllegalArgumentException("Both left and right are not present");
        } else if (this.left.isPresent() && this.right.isPresent()) {
            throw new IllegalArgumentException("Both left and right are present");
        }
    }

    public static <L, R> Either<L, R> makeLeft(L value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }

    public static <L, R> Either makeRight(R value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }
}
