package com.gurucool.paymentservice.repository;

import com.gurucool.paymentservice.entity.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, UUID> {
    List<PaymentLedger> findByPaymentOrderId(UUID paymentOrderId);
}
