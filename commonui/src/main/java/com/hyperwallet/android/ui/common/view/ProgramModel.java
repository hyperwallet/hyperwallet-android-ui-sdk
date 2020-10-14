package com.hyperwallet.android.ui.common.view;

public enum ProgramModel {
    WALLET_MODEL,
    PAY2CARD_MODEL,
    CARD_ONLY_MODEL;

    public static boolean isWalletModel(ProgramModel programModel) {
        return WALLET_MODEL == programModel;
    }

    public static boolean isCardModel(ProgramModel programModel) {
        return PAY2CARD_MODEL == programModel
                || ProgramModel.CARD_ONLY_MODEL == programModel;
    }
}

