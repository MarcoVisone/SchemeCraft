package com.xyra.schemecraft.service;

import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.AddressBean;
import com.xyra.schemecraft.model.PaymentMethodBean;
import com.xyra.schemecraft.exception.BadCredentialsException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InactiveEntityException;

import java.util.List;

public class AccountService {

    public AccountBean login(String usernameOrEmail, String password)
            throws BadCredentialsException, InactiveEntityException {
        throw new UnsupportedOperationException("TODO: login");
    }

    public void changePassword(String accountId, String oldPassword, String newPassword)
            throws BadCredentialsException, EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: changePassword");
    }

    public AccountBean registerAccount(AccountBean newAccount, String plainTextPassword)
            throws DuplicateEntityException {
        throw new UnsupportedOperationException("TODO: registerAccount");
    }

    public boolean checkEmailExists(String email) {

        throw new UnsupportedOperationException("TODO: checkEmailExists");
    }

    public boolean checkUsernameExists(String username) {
        throw new UnsupportedOperationException("TODO: checkUsernameExists");
    }

    public AccountBean getAccountById(String accountId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: getAccountById");
    }

    public void updateProfile(AccountBean updatedAccount) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: updateProfile");
    }

    public void deactivateAccount(String accountId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: deactivateAccount");
    }

    public List<AddressBean> listAddresses(String accountId) {
        throw new UnsupportedOperationException("TODO: listAddresses");
    }

    public void addAddress(AddressBean address) {
        throw new UnsupportedOperationException("TODO: addAddress");
    }

    public void removeAddress(String addressId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: removeAddress");
    }

    public void setDefaultAddress(String accountId, String addressId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: setDefaultAddress");
    }

    public List<PaymentMethodBean> listPaymentMethods(String accountId) {
        throw new UnsupportedOperationException("TODO: listPaymentMethods");
    }

    public void addPaymentMethod(PaymentMethodBean method, String rawCardData) {
        throw new UnsupportedOperationException("TODO: addPaymentMethod");
    }

    public void removePaymentMethod(String paymentMethodId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: removePaymentMethod");
    }

    public void setDefaultPaymentMethod(String accountId, String paymentMethodId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: setDefaultPaymentMethod");
    }
}