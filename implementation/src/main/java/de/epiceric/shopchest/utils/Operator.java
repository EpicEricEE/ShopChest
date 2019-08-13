package de.epiceric.shopchest.utils;

public enum Operator {

    EQUAL("==") {
        @Override
        public boolean compare(double a, double b) {
            return Double.compare(a, b) == 0;
        }
        @Override
        public boolean compare(String a, String b) {
            return a.equals(b);
        }
    },

    NOT_EQUAL("!=") {
        @Override
        public boolean compare(double a, double b) {
            return Double.compare(a, b) != 0;
        }
        @Override
        public boolean compare(String a, String b) {
            return !a.equals(b);
        }
    },

    GREATER_THAN(">") {
        @Override
        public boolean compare(double a, double b) {
            return a > b;
        }
    },

    GREATER_THAN_OR_EQUAL(">=") {
        @Override
        public boolean compare(double a, double b) {
            return a >= b;
        }
    },

    LESS_THAN("<") {
        @Override
        public boolean compare(double a, double b) {
            return a < b;
        }
    },

    LESS_THAN_OR_EQUAL("<=") {
        @Override
        public boolean compare(double a, double b) {
            return a <= b;
        }
    };

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public static Operator from(String symbol) {
        for (Operator operator : values()) {
            if (operator.symbol.equals(symbol)) {
                return operator;
            }
        }
        throw new IllegalArgumentException();
    }

    public abstract boolean compare(double a, double b);

    public boolean compare(String a, String b) {
        throw new UnsupportedOperationException();
    }
}
