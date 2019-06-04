package com.hyperwallet.android.receipt;

/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * Holds common types of Transaction content.
 */
public final class TransactionTypes {
    public static final String ANNUAL_FEE = "ANNUAL_FEE";
    public static final String ANNUAL_FEE_REFUND = "ANNUAL_FEE_REFUND";
    public static final String CUSTOMER_SERVICE_FEE = "CUSTOMER_SERVICE_FEE";
    public static final String CUSTOMER_SERVICE_FEE_REFUND = "CUSTOMER_SERVICE_FEE_REFUND";
    public static final String EXPEDITED_SHIPPING_FEE = "EXPEDITED_SHIPPING_FEE";
    public static final String GENERIC_FEE_REFUND = "GENERIC_FEE_REFUND";
    public static final String MONTHLY_FEE = "MONTHLY_FEE";
    public static final String MONTHLY_FEE_REFUND = "MONTHLY_FEE_REFUND";
    public static final String PAYMENT_EXPIRY_FEE = "PAYMENT_EXPIRY_FEE";
    public static final String PAYMENT_FEE = "PAYMENT_FEE";
    public static final String PROCESSING_FEE = "PROCESSING_FEE";
    public static final String STANDARD_SHIPPING_FEE = "STANDARD_SHIPPING_FEE";
    public static final String TRANSFER_FEE = "TRANSFER_FEE";


    public static final String ADJUSTMENT = "ADJUSTMENT";
    public static final String FOREIGN_EXCHANGE = "FOREIGN_EXCHANGE";
    public static final String DEPOSIT = "DEPOSIT";
    public static final String MANUAL_ADJUSTMENT = "MANUAL_ADJUSTMENT";
    public static final String PAYMENT_EXPIRATION = "PAYMENT_EXPIRATION";

    public static final String BANK_ACCOUNT_TRANSFER_FEE = "BANK_ACCOUNT_TRANSFER_FEE";
    public static final String BANK_ACCOUNT_TRANSFER_RETURN = "BANK_ACCOUNT_TRANSFER_RETURN";
    public static final String BANK_ACCOUNT_TRANSFER_RETURN_FEE = "BANK_ACCOUNT_TRANSFER_RETURN_FEE";
    public static final String TRANSFER_TO_BANK_ACCOUNT = "TRANSFER_TO_BANK_ACCOUNT";

    public static final String CARD_ACTIVATION_FEE = "CARD_ACTIVATION_FEE";
    public static final String CARD_ACTIVATION_FEE_WAIVER = "CARD_ACTIVATION_FEE_WAIVER";
    public static final String CARD_FEE = "CARD_FEE";
    public static final String MANUAL_TRANSFER_TO_PREPAID_CARD = "MANUAL_TRANSFER_TO_PREPAID_CARD";
    public static final String PREPAID_CARD_BALANCE_INQUIRY_FEE = "PREPAID_CARD_BALANCE_INQUIRY_FEE";
    public static final String PREPAID_CARD_CASH_ADVANCE = "PREPAID_CARD_CASH_ADVANCE";
    public static final String PREPAID_CARD_DISPUTED_CHARGE_REFUND = "PREPAID_CARD_DISPUTED_CHARGE_REFUND";
    public static final String PREPAID_CARD_DISPUTE_DEPOSIT = "PREPAID_CARD_DISPUTE_DEPOSIT";
    public static final String PREPAID_CARD_DOMESTIC_CASH_WITHDRAWAL_FEE = "PREPAID_CARD_DOMESTIC_CASH_WITHDRAWAL_FEE";
    public static final String PREPAID_CARD_EXCHANGE_RATE_DIFFERENCE = "PREPAID_CARD_EXCHANGE_RATE_DIFFERENCE";
    public static final String PREPAID_CARD_MANUAL_UNLOAD = "PREPAID_CARD_MANUAL_UNLOAD";
    public static final String PREPAID_CARD_OVERSEAS_CASH_WITHDRAWAL_FEE = "PREPAID_CARD_OVERSEAS_CASH_WITHDRAWAL_FEE";
    public static final String PREPAID_CARD_PIN_CHANGE_FEE = "PREPAID_CARD_PIN_CHANGE_FEE";
    public static final String PREPAID_CARD_REFUND = "PREPAID_CARD_REFUND";
    public static final String PREPAID_CARD_REPLACEMENT_FEE = "PREPAID_CARD_REPLACEMENT_FEE";
    public static final String PREPAID_CARD_SALE = "PREPAID_CARD_SALE";
    public static final String PREPAID_CARD_SALE_REVERSAL = "PREPAID_CARD_SALE_REVERSAL";
    public static final String PREPAID_CARD_UNLOAD = "PREPAID_CARD_UNLOAD";
    public static final String TRANSFER_TO_PREPAID_CARD = "TRANSFER_TO_PREPAID_CARD";

    public static final String DONATION = "DONATION";
    public static final String DONATION_FEE = "DONATION_FEE";
    public static final String DONATION_RETURN = "DONATION_RETURN";

    public static final String MERCHANT_PAYMENT = "MERCHANT_PAYMENT";
    public static final String MERCHANT_PAYMENT_FEE = "MERCHANT_PAYMENT_FEE";
    public static final String MERCHANT_PAYMENT_REFUND = "MERCHANT_PAYMENT_REFUND";
    public static final String MERCHANT_PAYMENT_RETURN = "MERCHANT_PAYMENT_RETURN";

    public static final String MONEYGRAM_TRANSFER_RETURN = "MONEYGRAM_TRANSFER_RETURN";
    public static final String TRANSFER_TO_MONEYGRAM = "TRANSFER_TO_MONEYGRAM";

    public static final String PAPER_CHECK_FEE = "PAPER_CHECK_FEE";
    public static final String PAPER_CHECK_REFUND = "PAPER_CHECK_REFUND";
    public static final String TRANSFER_TO_PAPER_CHECK = "TRANSFER_TO_PAPER_CHECK";

    public static final String ACCOUNT_CLOSURE = "ACCOUNT_CLOSURE";
    public static final String ACCOUNT_CLOSURE_FEE = "ACCOUNT_CLOSURE_FEE";
    public static final String ACCOUNT_UNLOAD = "ACCOUNT_UNLOAD";
    public static final String DORMANT_USER_FEE = "DORMANT_USER_FEE";
    public static final String DORMANT_USER_FEE_REFUND = "DORMANT_USER_FEE_REFUND";
    public static final String PAYMENT = "PAYMENT";
    public static final String PAYMENT_CANCELLATION = "PAYMENT_CANCELLATION";
    public static final String PAYMENT_REVERSAL = "PAYMENT_REVERSAL";
    public static final String PAYMENT_REVERSAL_FEE = "PAYMENT_REVERSAL_FEE";
    public static final String PAYMENT_RETURN = "PAYMENT_RETURN";
    public static final String TRANSFER_TO_PROGRAM_ACCOUNT = "TRANSFER_TO_PROGRAM_ACCOUNT";
    public static final String TRANSFER_TO_USER = "TRANSFER_TO_USER";

    public static final String VIRTUAL_INCENTIVE_CANCELLATION = "VIRTUAL_INCENTIVE_CANCELLATION";
    public static final String VIRTUAL_INCENTIVE_ISSUANCE = "VIRTUAL_INCENTIVE_ISSUANCE";
    public static final String VIRTUAL_INCENTIVE_PURCHASE = "VIRTUAL_INCENTIVE_PURCHASE";
    public static final String VIRTUAL_INCENTIVE_REFUND = "VIRTUAL_INCENTIVE_REFUND";

    public static final String TRANSFER_TO_WESTERN_UNION = "TRANSFER_TO_WESTERN_UNION";
    public static final String TRANSFER_TO_WUBS_WIRE = "TRANSFER_TO_WUBS_WIRE";
    public static final String WESTERN_UNION_TRANSFER_RETURN = "WESTERN_UNION_TRANSFER_RETURN";
    public static final String WUBS_WIRE_TRANSFER_RETURN = "WUBS_WIRE_TRANSFER_RETURN";

    public static final String TRANSFER_TO_WIRE = "TRANSFER_TO_WIRE";
    public static final String WIRE_TRANSFER_FEE = "WIRE_TRANSFER_FEE";
    public static final String WIRE_TRANSFER_RETURN = "WIRE_TRANSFER_RETURN";
}
