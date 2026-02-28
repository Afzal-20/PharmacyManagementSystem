package com.my.pharmacy.dao;

import com.my.pharmacy.model.LedgerRecord;
import com.my.pharmacy.model.Payment;
import java.util.List;

public interface PaymentDAO {
    void recordPayment(Payment payment);
    List<LedgerRecord> getCustomerLedger(int customerId);
    List<LedgerRecord> getDealerLedger(int dealerId);
}