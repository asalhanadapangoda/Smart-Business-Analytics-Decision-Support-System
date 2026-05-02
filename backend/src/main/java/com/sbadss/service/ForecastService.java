package com.sbadss.service;

import com.sbadss.dto.ForecastRequest;
import com.sbadss.dto.ForecastResponse;

public interface ForecastService {
    ForecastResponse getSalesForecast(ForecastRequest request);
}
