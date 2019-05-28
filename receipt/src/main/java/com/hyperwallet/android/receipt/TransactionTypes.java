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

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holds common types of Transaction content.
 */
public interface TransactionTypes {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            GenericFeeTypes.ANNUAL_FEE,
            GenericFeeTypes.ANNUAL_FEE_REFUND,
            GenericFeeTypes.CUSTOMER_SERVICE_FEE,
            GenericFeeTypes.CUSTOMER_SERVICE_FEE_REFUND,
            GenericFeeTypes.EXPEDITED_SHIPPING_FEE,
            GenericFeeTypes.GENERIC_FEE_REFUND,
            GenericFeeTypes.MONTHLY_FEE,
            GenericFeeTypes.MONTHLY_FEE_REFUND,
            GenericFeeTypes.PAYMENT_EXPIRY_FEE,
            GenericFeeTypes.PAYMENT_FEE,
            GenericFeeTypes.PROCESSING_FEE,
            GenericFeeTypes.STANDARD_SHIPPING_FEE,
            GenericFeeTypes.TRANSFER_FEE
    })
    public @interface GenericFee {
    }

    public interface GenericFeeTypes {
        String ANNUAL_FEE = "ANNUAL_FEE";
        String ANNUAL_FEE_REFUND = "ANNUAL_FEE_REFUND";
        String CUSTOMER_SERVICE_FEE = "CUSTOMER_SERVICE_FEE";
        String CUSTOMER_SERVICE_FEE_REFUND = "CUSTOMER_SERVICE_FEE_REFUND";
        String EXPEDITED_SHIPPING_FEE = "EXPEDITED_SHIPPING_FEE";
        String GENERIC_FEE_REFUND = "GENERIC_FEE_REFUND";
        String MONTHLY_FEE = "MONTHLY_FEE";
        String MONTHLY_FEE_REFUND = "MONTHLY_FEE_REFUND";
        String PAYMENT_EXPIRY_FEE = "PAYMENT_EXPIRY_FEE";
        String PAYMENT_FEE = "PAYMENT_FEE";
        String PROCESSING_FEE = "PROCESSING_FEE";
        String STANDARD_SHIPPING_FEE = "STANDARD_SHIPPING_FEE";
        String TRANSFER_FEE = "TRANSFER_FEE";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            GenericPaymentTypes.ADJUSTMENT,
            GenericPaymentTypes.FOREIGN_EXCHANGE,
            GenericPaymentTypes.DEPOSIT,
            GenericPaymentTypes.MANUAL_ADJUSTMENT,
            GenericPaymentTypes.PAYMENT_EXPIRATION
    })
    public @interface GenericPayment {

    }

    public interface GenericPaymentTypes {
        String ADJUSTMENT = "ADJUSTMENT";
        String FOREIGN_EXCHANGE = "FOREIGN_EXCHANGE";
        String DEPOSIT = "DEPOSIT";
        String MANUAL_ADJUSTMENT = "MANUAL_ADJUSTMENT";
        String PAYMENT_EXPIRATION = "PAYMENT_EXPIRATION";
    }


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            BankAccountTypes.BANK_ACCOUNT_TRANSFER_FEE,
            BankAccountTypes.BANK_ACCOUNT_TRANSFER_RETURN,
            BankAccountTypes.BANK_ACCOUNT_TRANSFER_RETURN_FEE,
            BankAccountTypes.TRANSFER_TO_BANK_ACCOUNT
    })
    public @interface BankAccount {

    }

    public interface BankAccountTypes {
        String BANK_ACCOUNT_TRANSFER_FEE = "BANK_ACCOUNT_TRANSFER_FEE";
        String BANK_ACCOUNT_TRANSFER_RETURN = "BANK_ACCOUNT_TRANSFER_RETURN";
        String BANK_ACCOUNT_TRANSFER_RETURN_FEE = "BANK_ACCOUNT_TRANSFER_RETURN_FEE";
        String TRANSFER_TO_BANK_ACCOUNT = "TRANSFER_TO_BANK_ACCOUNT";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CardTypes.CARD_ACTIVATION_FEE,
            CardTypes.CARD_ACTIVATION_FEE_WAIVER,
            CardTypes.CARD_FEE,
            CardTypes.MANUAL_TRANSFER_TO_PREPAID_CARD,
            CardTypes.PREPAID_CARD_BALANCE_INQUIRY_FEE,
            CardTypes.PREPAID_CARD_CASH_ADVANCE,
            CardTypes.PREPAID_CARD_DISPUTED_CHARGE_REFUND,
            CardTypes.PREPAID_CARD_DISPUTE_DEPOSIT,
            CardTypes.PREPAID_CARD_DOMESTIC_CASH_WITHDRAWAL_FEE,
            CardTypes.PREPAID_CARD_EXCHANGE_RATE_DIFFERENCE,
            CardTypes.PREPAID_CARD_MANUAL_UNLOAD,
            CardTypes.PREPAID_CARD_OVERSEAS_CASH_WITHDRAWAL_FEE,
            CardTypes.PREPAID_CARD_PIN_CHANGE_FEE,
            CardTypes.PREPAID_CARD_REFUND,
            CardTypes.PREPAID_CARD_REPLACEMENT_FEE,
            CardTypes.PREPAID_CARD_SALE,
            CardTypes.PREPAID_CARD_SALE_REVERSAL,
            CardTypes.PREPAID_CARD_UNLOAD,
            CardTypes.TRANSFER_TO_PREPAID_CARD
    })
    public @interface Card {

    }

    public interface CardTypes {
        String CARD_ACTIVATION_FEE = "CARD_ACTIVATION_FEE";
        String CARD_ACTIVATION_FEE_WAIVER = "CARD_ACTIVATION_FEE_WAIVER";
        String CARD_FEE = "CARD_FEE";
        String MANUAL_TRANSFER_TO_PREPAID_CARD = "MANUAL_TRANSFER_TO_PREPAID_CARD";
        String PREPAID_CARD_BALANCE_INQUIRY_FEE = "PREPAID_CARD_BALANCE_INQUIRY_FEE";
        String PREPAID_CARD_CASH_ADVANCE = "PREPAID_CARD_CASH_ADVANCE";
        String PREPAID_CARD_DISPUTED_CHARGE_REFUND = "PREPAID_CARD_DISPUTED_CHARGE_REFUND";
        String PREPAID_CARD_DISPUTE_DEPOSIT = "PREPAID_CARD_DISPUTE_DEPOSIT";
        String PREPAID_CARD_DOMESTIC_CASH_WITHDRAWAL_FEE = "PREPAID_CARD_DOMESTIC_CASH_WITHDRAWAL_FEE";
        String PREPAID_CARD_EXCHANGE_RATE_DIFFERENCE = "PREPAID_CARD_EXCHANGE_RATE_DIFFERENCE";
        String PREPAID_CARD_MANUAL_UNLOAD = "PREPAID_CARD_MANUAL_UNLOAD";
        String PREPAID_CARD_OVERSEAS_CASH_WITHDRAWAL_FEE = "PREPAID_CARD_OVERSEAS_CASH_WITHDRAWAL_FEE";
        String PREPAID_CARD_PIN_CHANGE_FEE = "PREPAID_CARD_PIN_CHANGE_FEE";
        String PREPAID_CARD_REFUND = "PREPAID_CARD_REFUND";
        String PREPAID_CARD_REPLACEMENT_FEE = "PREPAID_CARD_REPLACEMENT_FEE";
        String PREPAID_CARD_SALE = "PREPAID_CARD_SALE";
        String PREPAID_CARD_SALE_REVERSAL = "PREPAID_CARD_SALE_REVERSAL";
        String PREPAID_CARD_UNLOAD = "PREPAID_CARD_UNLOAD";
        String TRANSFER_TO_PREPAID_CARD = "TRANSFER_TO_PREPAID_CARD";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            DonationTypes.DONATION,
            DonationTypes.DONATION_FEE,
            DonationTypes.DONATION_RETURN
    })
    public @interface Donation {

    }

    public interface DonationTypes {
        String DONATION = "DONATION";
        String DONATION_FEE = "DONATION_FEE";
        String DONATION_RETURN = "DONATION_RETURN";

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            MerchantPaymentTypes.MERCHANT_PAYMENT,
            MerchantPaymentTypes.MERCHANT_PAYMENT_FEE,
            MerchantPaymentTypes.MERCHANT_PAYMENT_REFUND,
            MerchantPaymentTypes.MERCHANT_PAYMENT_RETURN
    })
    public @interface MerchantPayment {

    }

    public interface MerchantPaymentTypes {
        String MERCHANT_PAYMENT = "MERCHANT_PAYMENT";
        String MERCHANT_PAYMENT_FEE = "MERCHANT_PAYMENT_FEE";
        String MERCHANT_PAYMENT_REFUND = "MERCHANT_PAYMENT_REFUND";
        String MERCHANT_PAYMENT_RETURN = "MERCHANT_PAYMENT_RETURN";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            MoneyGramTypes.MONEYGRAM_TRANSFER_RETURN,
            MoneyGramTypes.TRANSFER_TO_MONEYGRAM
    })
    public @interface MoneyGram {

    }

    public interface MoneyGramTypes {
        String MONEYGRAM_TRANSFER_RETURN = "MONEYGRAM_TRANSFER_RETURN";
        String TRANSFER_TO_MONEYGRAM = "TRANSFER_TO_MONEYGRAM";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            PaperCheckTypes.PAPER_CHECK_FEE,
            PaperCheckTypes.PAPER_CHECK_REFUND,
            PaperCheckTypes.TRANSFER_TO_PAPER_CHECK
    })
    public @interface PaperCheck {

    }

    public interface PaperCheckTypes {
        String PAPER_CHECK_FEE = "PAPER_CHECK_FEE";
        String PAPER_CHECK_REFUND = "PAPER_CHECK_REFUND";
        String TRANSFER_TO_PAPER_CHECK = "TRANSFER_TO_PAPER_CHECK";

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            UserAndProgramAccountTypes.ACCOUNT_CLOSURE,
            UserAndProgramAccountTypes.ACCOUNT_CLOSURE_FEE,
            UserAndProgramAccountTypes.ACCOUNT_UNLOAD,
            UserAndProgramAccountTypes.DORMANT_USER_FEE,
            UserAndProgramAccountTypes.DORMANT_USER_FEE_REFUND,
            UserAndProgramAccountTypes.PAYMENT,
            UserAndProgramAccountTypes.PAYMENT_CANCELLATION,
            UserAndProgramAccountTypes.PAYMENT_REVERSAL,
            UserAndProgramAccountTypes.PAYMENT_REVERSAL_FEE,
            UserAndProgramAccountTypes.PAYMENT_RETURN,
            UserAndProgramAccountTypes.TRANSFER_TO_PROGRAM_ACCOUNT,
            UserAndProgramAccountTypes.TRANSFER_TO_USER
    })
    public @interface UserAndProgramAccount {

    }

    public interface UserAndProgramAccountTypes {
        String ACCOUNT_CLOSURE = "ACCOUNT_CLOSURE";
        String ACCOUNT_CLOSURE_FEE = "ACCOUNT_CLOSURE_FEE";
        String ACCOUNT_UNLOAD = "ACCOUNT_UNLOAD";
        String DORMANT_USER_FEE = "DORMANT_USER_FEE";
        String DORMANT_USER_FEE_REFUND = "DORMANT_USER_FEE_REFUND";
        String PAYMENT = "PAYMENT";
        String PAYMENT_CANCELLATION = "PAYMENT_CANCELLATION";
        String PAYMENT_REVERSAL = "PAYMENT_REVERSAL";
        String PAYMENT_REVERSAL_FEE = "PAYMENT_REVERSAL_FEE";
        String PAYMENT_RETURN = "PAYMENT_RETURN";
        String TRANSFER_TO_PROGRAM_ACCOUNT = "TRANSFER_TO_PROGRAM_ACCOUNT";
        String TRANSFER_TO_USER = "TRANSFER_TO_USER";

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            VirtualIncentiveTypes.VIRTUAL_INCENTIVE_CANCELLATION,
            VirtualIncentiveTypes.VIRTUAL_INCENTIVE_ISSUANCE,
            VirtualIncentiveTypes.VIRTUAL_INCENTIVE_PURCHASE,
            VirtualIncentiveTypes.VIRTUAL_INCENTIVE_REFUND
    })
    public @interface VirtualIncentive {

    }

    public interface VirtualIncentiveTypes {
        String VIRTUAL_INCENTIVE_CANCELLATION = "VIRTUAL_INCENTIVE_CANCELLATION";
        String VIRTUAL_INCENTIVE_ISSUANCE = "VIRTUAL_INCENTIVE_ISSUANCE";
        String VIRTUAL_INCENTIVE_PURCHASE = "VIRTUAL_INCENTIVE_PURCHASE";
        String VIRTUAL_INCENTIVE_REFUND = "VIRTUAL_INCENTIVE_REFUND";


    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            WesternUnionAndWUBSTypes.TRANSFER_TO_WESTERN_UNION,
            WesternUnionAndWUBSTypes.TRANSFER_TO_WUBS_WIRE,
            WesternUnionAndWUBSTypes.WESTERN_UNION_TRANSFER_RETURN,
            WesternUnionAndWUBSTypes.WUBS_WIRE_TRANSFER_RETURN,
    })
    public @interface WesternUnionAndWUBS {

    }

    public interface WesternUnionAndWUBSTypes {
        String TRANSFER_TO_WESTERN_UNION = "TRANSFER_TO_WESTERN_UNION";
        String TRANSFER_TO_WUBS_WIRE = "TRANSFER_TO_WUBS_WIRE";
        String WESTERN_UNION_TRANSFER_RETURN = "WESTERN_UNION_TRANSFER_RETURN";
        String WUBS_WIRE_TRANSFER_RETURN = "WUBS_WIRE_TRANSFER_RETURN";

    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            WireTransferTypes.TRANSFER_TO_WIRE,
            WireTransferTypes.WIRE_TRANSFER_FEE,
            WireTransferTypes.WIRE_TRANSFER_RETURN
    })
    public @interface WireTransfer {

    }

    public interface WireTransferTypes {
        String TRANSFER_TO_WIRE = "TRANSFER_TO_WIRE";
        String WIRE_TRANSFER_FEE = "WIRE_TRANSFER_FEE";
        String WIRE_TRANSFER_RETURN = "WIRE_TRANSFER_RETURN";


    }

}
