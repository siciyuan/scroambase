package com.scroam.db.data;

import java.sql.Timestamp;
import java.util.UUID;

public class TransactionRecord {

    public String id;
    public UUID fromUuid;
    public String fromName;
    public UUID toUuid;
    public String toName;
    public double amount;
    public double taxAmount;
    public TransactionType type;
    public String serviceId;
    public String description;
    public Timestamp createdAt;

    public enum TransactionType {
        DEPOSIT("存入"),
        WITHDRAW("取出"),
        TRANSFER("转账"),
        PAYMENT("支付"),
        EXCHANGE("兑换"),
        TAX("税收"),
        TREASURY("公库"),
        ADMIN("管理");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static TransactionType fromString(String str) {
            try {
                return valueOf(str.toUpperCase());
            } catch (Exception e) {
                return ADMIN;
            }
        }
    }

    public TransactionRecord() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public TransactionRecord(String id, UUID fromUuid, String fromName, UUID toUuid, String toName,
                            double amount, double taxAmount, TransactionType type, String serviceId, String description) {
        this.id = id;
        this.fromUuid = fromUuid;
        this.fromName = fromName;
        this.toUuid = toUuid;
        this.toName = toName;
        this.amount = amount;
        this.taxAmount = taxAmount;
        this.type = type;
        this.serviceId = serviceId;
        this.description = description;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public static String generateId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}