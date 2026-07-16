package com.xyra.schemecraft.constant;

/**
 * Utility class containing standard database error codes and SQL states.
 */
public final class DatabaseConstants {

    /**
     * Standard ANSI SQLState representing integrity constraint violations.
     * This includes duplicate keys, foreign key failures, and non-null violations.
     */
    public static final String SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";

    /**
     * MySQL Error Code: 1062 (ER_DUP_ENTRY).
     * Occurs when trying to insert or update a row that violates a UNIQUE index or PRIMARY KEY constraint.
     */
    public static final int MYSQL_ERR_DUPLICATE_KEY = 1062;

    /**
     * MySQL Error Code: 1451 (ER_ROW_IS_REFERENCED_2).
     * Occurs when trying to delete or update a parent row that is referenced by a foreign key in a child table.
     */
    public static final int MYSQL_ERR_ROW_IS_REFERENCED = 1451;

    /**
     * MySQL Error Code: 1452 (ER_NO_REFERENCED_ROW_2).
     * Occurs when trying to insert or update a child row with a foreign key value that does not exist
     * in the parent table.
     */
    public static final int MYSQL_ERR_NO_REFERENCED_ROW = 1452;

    /**
     * MySQL Error Code: 1216 (ER_NO_REFERENCED_ROW).
     * Legacy error code for foreign key insertion failures.
     */
    public static final int MYSQL_ERR_NO_REFERENCED_ROW_LEGACY = 1216;

    /**
     * MySQL Error Code: 1217 (ER_ROW_IS_REFERENCED).
     * Legacy error code for foreign key deletion failures.
     */
    public static final int MYSQL_ERR_ROW_IS_REFERENCED_LEGACY = 1217;

    /**
     * Private constructor to prevent instantiation of this constant utility class.
     * @throws AssertionError if this constructor is called via reflection
     */
    private DatabaseConstants() {
        throw new AssertionError("No DatabaseConstants instances for you!");
    }
}
