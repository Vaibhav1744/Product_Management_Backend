package com.bills.billingsoftware.service;

import com.bills.billingsoftware.dto.InvoiceRequestDTO;
import com.bills.billingsoftware.entity.Invoice;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(InvoiceRequestDTO invoiceRequestDTO);
    Invoice getOrderById(long invoiceId);
    List<Invoice> getAllInvoice();
}
