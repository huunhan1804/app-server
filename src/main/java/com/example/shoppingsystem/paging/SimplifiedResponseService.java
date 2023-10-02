package com.example.shoppingsystem.paging;

import com.example.shoppingsystem.responses.SimplifiedResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface SimplifiedResponseService {
    <T> SimplifiedResponse<T> simplifiedResponse(Page<T> page);
}

