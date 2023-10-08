package com.zerobase.dividend.exception.Impl;

import com.zerobase.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyExistTicker extends AbstractException {
    private static String TICKER;
    public AlreadyExistTicker(String ticker) {
        this.TICKER = ticker;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return TICKER + " 회사정보가 존재합니다. 조회기능을 이용해주세요.";
    }
}
