package com.bills.billingsoftware.repository;

import com.bills.billingsoftware.entity.Invoice;
import com.bills.billingsoftware.entity.InvoiceItem;
import com.bills.billingsoftware.entity.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public InvoiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Invoice> invoiceRowMapper = (rs, rowNum) -> {
        Invoice invoice = new Invoice();

        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        invoice.setCustomer(customer);

        invoice.setTotalAmount(rs.getDouble("total_amount"));
        invoice.setTotalTax(rs.getDouble("total_tax"));
        invoice.setDiscout(rs.getDouble("final_amount"));

        return invoice;
    };

    public void saveInvoice(Invoice invoice) {
        String sql = """
                INSERT INTO invoice
                (invoice_id, invoice_date, customer_id, total_amount, total_tax, discount, final_amount)
                VALUES (?,?,?,?,?,?,?)
                """;

        jdbcTemplate.update(
                sql,
                invoice.getInvoiceId(),
                Date.valueOf(invoice.getInvoiceDate()),
                invoice.getCustomer().getId(),
                invoice.getTotalAmount(),
                invoice.getTotalTax(),
                invoice.getDiscout(),
                invoice.getFinalAmount()
        );
    }

    public void saveInvoiceItems(List<InvoiceItem> items) {
        String sql = """
                INSERT INTO invoice_item
                (invoice_id, product_id, quantity, price, tax_amount, discount, total)
                VALUES (?,?,?,?,?,?,?)
                """;

        for (InvoiceItem item : items) {
            jdbcTemplate.update(
                    sql,
                    item.getInvoiceId(),
                    item.getProduct().getId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getTaxAmount(),
                    item.getDiscount(),
                    item.getTotal()
            );
        }
    }

    public Invoice getInvoiceById(long invoiceId) {
        String sql = "SELECT * FROM invoice WHERE invoice_id = ?";
        return jdbcTemplate.query(sql, invoiceRowMapper, invoiceId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<Invoice> getAllInvoices() {
        String sql = "SELECT * FROM invoice";
        return jdbcTemplate.query(sql, invoiceRowMapper);
    }
}
