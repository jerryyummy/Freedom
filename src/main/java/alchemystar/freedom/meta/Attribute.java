package alchemystar.freedom.meta;

import alchemystar.freedom.meta.value.Value;
import alchemystar.freedom.meta.value.ValueBoolean;
import alchemystar.freedom.meta.value.ValueInt;
import alchemystar.freedom.meta.value.ValueLong;
import alchemystar.freedom.meta.value.ValueString;

/**
 * 属性
 *
 * @Author lizhuyang
 */
public class Attribute {
    // 属性名称
    private String name;
    // 属性类型
    private int type;
    // 在TupleDesc中的位置
    private int index;
    // 注释
    private String comment;

    private boolean isPrimaryKey;

    /**
     * Instantiates a new Attribute.
     */
    public Attribute() {
    }

    /**
     * Instantiates a new Attribute.
     *
     * @param name    the name
     * @param type    the type
     * @param index   the index
     * @param comment the comment
     */
    public Attribute(String name, int type, int index, String comment) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.comment = comment;
    }

    /**
     * Gets default value.
     *
     * @return the default value
     */
    public Value getDefaultValue() {

        switch (type) {
            case Value.STRING:
                return new ValueString("");
            case Value.INT:
                return new ValueInt(0);
            case Value.LONG:
                return new ValueLong(0);
            case Value.BOOLEAN:
                return new ValueBoolean(false);
            default:
                throw new RuntimeException("not support this type :" + type);
        }
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     * @return the name
     */
    public Attribute setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     * @return the type
     */
    public Attribute setType(int type) {
        this.type = type;
        return this;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets index.
     *
     * @param index the index
     * @return the index
     */
    public Attribute setIndex(int index) {
        this.index = index;
        return this;
    }

    /**
     * Gets comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets comment.
     *
     * @param comment the comment
     * @return the comment
     */
    public Attribute setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Is primary key boolean.
     *
     * @return the boolean
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Sets primary key.
     *
     * @param primaryKey the primary key
     */
    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }
}
