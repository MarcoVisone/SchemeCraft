package com.xyra.schemecraft.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all Data Access Objects (DAOs) in the SchemeCraft platform.
 */
public abstract class BaseDAO {

    /**
     * Category-specific logger for the active subclass.
     * Inherited by all concrete DAO implementations.
     */
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Protected constructor to restrict instantiation solely to inheriting subclasses
     * within the DAO package hierarchy.
     */
    protected BaseDAO() {
    }
}
