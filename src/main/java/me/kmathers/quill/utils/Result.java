package me.kmathers.quill.utils;

import java.util.Optional;

public class Result {

    public record BooleanResult(boolean success, Optional<String> message) {

        public static BooleanResult ok() {
            return new BooleanResult(true, Optional.empty());
        }

        public static BooleanResult ok(String msg) {
            return new BooleanResult(true, Optional.of(msg));
        }

        public static BooleanResult fail(String msg) {
            return new BooleanResult(false, Optional.of(msg));
        }
    }
}