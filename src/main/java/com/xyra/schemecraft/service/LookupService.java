package com.xyra.schemecraft.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.ServiceException;

/**
 * Read-only service for lookup/dictionary tables (country, currency, language, order status).
 * Used to populate dropdowns and reference data across the application.
 */
public class LookupService {

    private static final Logger logger = LoggerFactory.getLogger(LookupService.class);

    private final CountryDAO countryDAO;
    private final CurrencyDAO currencyDAO;
    private final LanguageDAO languageDAO;
    private final OrderStatusDAO orderStatusDAO;
    private final PaymentMethodTypeDAO paymentMethodTypeDAO;

    public LookupService() {
        this.countryDAO = new CountryDAO();
        this.currencyDAO = new CurrencyDAO();
        this.languageDAO = new LanguageDAO();
        this.orderStatusDAO = new OrderStatusDAO();
        this.paymentMethodTypeDAO = new PaymentMethodTypeDAO();
    }

    public List<CountryBean> listActiveCountries() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return countryDAO.findAllActive(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve active countries", e);
            throw new ServiceException("Unable to retrieve active countries", e);
        }
    }

    public List<CountryBean> listAllCountries() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return countryDAO.findAll(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve all countries", e);
            throw new ServiceException("Unable to retrieve all countries", e);
        }
    }

    public List<CurrencyBean> listActiveCurrencies() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return currencyDAO.findAllActive(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve active currencies", e);
            throw new ServiceException("Unable to retrieve active currencies", e);
        }
    }

    public List<CurrencyBean> listAllCurrencies() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return currencyDAO.findAll(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve all currencies", e);
            throw new ServiceException("Unable to retrieve all currencies", e);
        }
    }

    public List<LanguageBean> listAllLanguages() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return languageDAO.findAll(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve all languages", e);
            throw new ServiceException("Unable to retrieve all languages", e);
        }
    }

    /**
     * Retrieves all configured order statuses.
     *
     * @return List of all registered order status beans
     * @throws ServiceException if a database error or DAO failure occurs
     */
    public List<OrderStatusBean> listOrderStatuses() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return orderStatusDAO.findAll(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve order statuses", e);
            throw new ServiceException("Unable to retrieve order statuses", e);
        }
    }

    public List<PaymentMethodTypeBean> listPaymentMethodTypes() {
        try (Connection conn = ConnectionPool.getConnection()) {
            return paymentMethodTypeDAO.findAll(conn);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve payment method types", e);
            throw new ServiceException("Unable to retrieve payment method types", e);
        }
    }

    /**
     * Retrieves a single Currency by its unique ID.
     * Used for display purposes (e.g. resolving a currency symbol) rather than full listings.
     *
     * @param currencyId Unique identifier of the currency
     * @return An Optional containing the currency, or empty if not found
     * @throws ServiceException if a database error or DAO failure occurs
     */
    public Optional<CurrencyBean> getCurrencyById(String currencyId) {
        if (currencyId == null || currencyId.trim().isEmpty()) {
            return Optional.empty();
        }
        try (Connection conn = ConnectionPool.getConnection()) {
            return currencyDAO.findById(conn, currencyId);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to retrieve currency by ID: {}", currencyId, e);
            throw new ServiceException("Unable to retrieve currency", e);
        }
    }
}
