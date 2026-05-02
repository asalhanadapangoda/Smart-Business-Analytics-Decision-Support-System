package com.sbadss.mapper;

import com.sbadss.dto.DataPointResponse;
import com.sbadss.entity.Sale;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SaleMapper {

    public List<DataPointResponse> toTrendDataPoints(List<Object[]> rawData) {
        List<DataPointResponse> points = new ArrayList<>();
        if (rawData == null) return points;
        
        for (Object[] row : rawData) {
            points.add(new DataPointResponse(row[0].toString(), (BigDecimal) row[1]));
        }
        return points;
    }
}
