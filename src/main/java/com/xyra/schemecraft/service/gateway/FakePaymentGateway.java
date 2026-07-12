package com.xyra.schemecraft.service.gateway;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FakePaymentGateway {
    public ChargeResult charge(String paymentToken, BigDecimal amount, String currency) {
        if(paymentToken == null || !paymentToken.startsWith("tok_")){
            return ChargeResult.failure("INVALID_TOKEN");
        }

        boolean success = ThreadLocalRandom.current().nextInt(100) < 90;

        if(success){
            return ChargeResult.success("txn_" + UUID.randomUUID());
        }

        return ChargeResult.failure("CARD_DECLINED_MOCK");
    }
}
