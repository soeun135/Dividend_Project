package com.zerobase.dividend.exception.Impl;

import com.zerobase.dividend.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotExistTicker extends AbstractException {
    private static String TICKER;
    public NotExistTicker(String ticker) {
        this.TICKER = ticker;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return TICKER + "는 존재하지 않는 회사입니다.";
    }
}
