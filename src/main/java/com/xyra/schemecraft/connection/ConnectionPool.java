package com.xyra.schemecraft.connection;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionPool {
    private static final DataSource ds;

    static {
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/schemecraftdb");
        } catch (NamingException e) {
            throw new RuntimeException("Failed to look up JNDI DataSource 'jdbc/schemecraftdb'. " +
                    "Ensure the resource is properly configured in context.xml.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
