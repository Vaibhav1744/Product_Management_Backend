package com.bills.billingsoftware.service;

import com.bills.billingsoftware.dto.InvoiceRequestDTO;
import com.bills.billingsoftware.entity.*;
import com.bills.billingsoftware.exception.ResourceNotFoundException;
import com.bills.billingsoftware.repository.CustomerRepository;
import com.bills.billingsoftware.repository.InvoiceRepository;
import com.bills.billingsoftware.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public InvoiceServiceImpl(
            InvoiceRepository invoiceRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Invoice createInvoice(InvoiceRequestDTO dto) {

        Customer customer = customerRepository.getCustomerById(dto.getCustomer().getId());
        if (customer == null) {
            throw new ResourceNotFoundException("Customer not found");
        }

        double totalAmount = 0;
        double totalTax = 0;

        List<InvoiceItem> items = dto.getItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Invoice items cannot be empty");
        }

        for (InvoiceItem item : items) {

            Product product = productRepository.getProductById(item.getProduct().getId());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            double price = product.getPrice() * item.getQuantity();
            double tax = price * product.getGstPercentage() / 100;

            item.setPrice(price);
            item.setTaxAmount(tax);
            item.setTotal(price + tax - item.getDiscount());

            totalAmount += price;
            totalTax += tax;

            productRepository.updatePrice_stock(
                    product.getId(),
                    product.getPrice(),
                    product.getStockQuantity() - item.getQuantity()
            );
        }

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setTotalAmount(totalAmount);
        invoice.setTotalTax(totalTax);
        invoice.setDiscout(dto.getDiscount());
        invoice.setFinalAmount(totalAmount + totalTax - dto.getDiscount());

        invoiceRepository.saveInvoice(invoice);

        for (InvoiceItem item : items) {
            item.setInvoiceId(invoice.getInvoiceId());
        }

        invoiceRepository.saveInvoiceItems(items);

        return invoice;
    }

    @Override
    public Invoice getOrderById(long invoiceId) {
        return invoiceRepository.getInvoiceById(invoiceId);
    }

    @Override
    public List<Invoice> getAllInvoice() {
        return invoiceRepository.getAllInvoices();
    }
}
