package com.xyra.schemecraft.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class providing centralized access to the SQL database connection pool.
 * This implementation relies on JNDI (Java Naming and Directory Interface) to look up
 * the {@link DataSource} managed by the Servlet Container.
 */
public final class ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    private static final String JNDI_LOOKUP_PATH = "java:comp/env/jdbc/schemecraftdb";
    private static final DataSource dataSource;

    static {
        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(JNDI_LOOKUP_PATH);
            LOGGER.info("JNDI DataSource successfully bound from path: {}", JNDI_LOOKUP_PATH);
        } catch (NamingException e) {
            String errorMessage = "CRITICAL: Failed to locate JNDI DataSource 'jdbc/schemecraftdb'. " +
                    "Verify that the Resource is correctly declared in your 'context.xml' or 'server.xml', " +
                    "and that the servlet container's library folder contains the MySQL JDBC Driver.";
            LOGGER.error(errorMessage, e);
            throw new ExceptionInInitializerError(new RuntimeException(errorMessage, e));
        }
    }

    /**
     * Private constructor to prevent external instantiation of this utility class.
     *
     * @throws AssertionError if this constructor is called via reflection
     */
    private ConnectionPool() {
        throw new AssertionError("No ConnectionPool instances allowed!");
    }

    /**
     * Obtains a physical database connection from the pre-configured connection pool.
     *
     * @return A valid {@link Connection} object ready for database operations
     * @throws SQLException if a database access error occurs or the pool is exhausted
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is uninitialized. " +
                    "Check application startup logs for configuration errors.");
        }
        return dataSource.getConnection();
    }
}
