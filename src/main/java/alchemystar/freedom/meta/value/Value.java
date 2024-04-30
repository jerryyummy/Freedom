package alchemystar.freedom.meta.value;

/**
 * Value
 *
 * @Author lizhuyang
 */
public abstract class Value {

    /**
     * The constant UNKNOWN.
     */
    public static final byte UNKNOWN = 100;
    /**
     * The constant STRING.
     */
    public static final byte STRING = 1;
    /**
     * The constant INT.
     */
    public static final byte INT = 2;
    /**
     * The constant LONG.
     */
    public static final byte LONG = 3;
    /**
     * The constant BOOLEAN.
     */
    public static final byte BOOLEAN = 4;

    /**
     * Gets length.
     *
     * @return the length
     */
    public abstract int getLength();

    /**
     * Gets type.
     *
     * @return the type
     */
    public abstract byte getType();

    /**
     * Get bytes byte [ ].
     *
     * @return the byte [ ]
     */
    public abstract byte[] getBytes();

    /**
     * Read.
     *
     * @param bytes the bytes
     */
    public abstract void read(byte[] bytes);

    /**
     * Compare int.
     *
     * @param value the value
     * @return the int
     */
    public abstract int compare(Value value);

    /**
     * And value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean and(Value value) {
        if (!(this instanceof ValueBoolean)) {
            throw new RuntimeException("left value must be boolean");
        }
        if (!(value instanceof ValueBoolean)) {
            throw new RuntimeException("right value must be boolean");
        }

        boolean result = ((ValueBoolean) this).getBoolean() && ((ValueBoolean) value).getBoolean();
        return new ValueBoolean(result);
    }

    /**
     * Or value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean or(Value value) {
        if (!(this instanceof ValueBoolean)) {
            throw new RuntimeException("left value must be boolean");
        }
        if (!(value instanceof ValueBoolean)) {
            throw new RuntimeException("right value must be boolean");
        }

        boolean result = ((ValueBoolean) this).getBoolean() || ((ValueBoolean) value).getBoolean();
        return new ValueBoolean(result);
    }

    /**
     * Equality value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean equality(Value value) {
        if (compare(value) == 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    /**
     * Greater than or equal value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean greaterThanOrEqual(Value value) {
        if (compare(value) >= 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    /**
     * Greater than value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean greaterThan(Value value) {
        if (compare(value) > 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    /**
     * Less than or equal value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean lessThanOrEqual(Value value) {
        if (compare(value) <= 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    /**
     * Less than value boolean.
     *
     * @param value the value
     * @return the value boolean
     */
    public ValueBoolean lessThan(Value value) {
        if (compare(value) < 0) {
            return new ValueBoolean(true);
        } else {
            return new ValueBoolean(false);
        }
    }

    /**
     * Add value.
     *
     * @param v the v
     * @return the value
     */
    public Value add(Value v) {
        throw new RuntimeException("UnSupport Plus Function");
    }

    /**
     * Gets int.
     *
     * @return the int
     */
    public int getInt() {
        throw new RuntimeException("UnSupport get int");
    }

    /**
     * Gets long.
     *
     * @return the long
     */
    public long getLong() {
        throw new RuntimeException("UnSupport get long");
    }

    /**
     * Gets string.
     *
     * @return the string
     */
    public abstract String getString();

    /**
     * Subtract value.
     *
     * @param v the v
     * @return the value
     */
    public Value subtract(Value v) {
        throw new RuntimeException("UnSupport Minus Function");
    }

    /**
     * Divide value.
     *
     * @param v the v
     * @return the value
     */
    public Value divide(Value v) {
        throw new RuntimeException("UnSupport divide Function");
    }

    /**
     * Multiply value.
     *
     * @param v the v
     * @return the value
     */
    public Value multiply(Value v) {
        throw new RuntimeException("UnSupport multiply Function");
    }

    /**
     * Concat value.
     *
     * @param v the v
     * @return the value
     */
    public Value concat(Value v) {
        throw new RuntimeException("UnSupport concat Function");
    }
}
