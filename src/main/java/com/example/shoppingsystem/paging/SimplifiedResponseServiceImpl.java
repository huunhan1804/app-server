package com.example.shoppingsystem.paging;

import com.example.shoppingsystem.responses.SimplifiedResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SimplifiedResponseServiceImpl implements SimplifiedResponseService{

    @Override
    public <T> SimplifiedResponse<T> simplifiedResponse(Page<T> page) {
        SimplifiedResponse<T> response = new SimplifiedResponse<>();
        response.setContent(page.getContent());
        response.setSize(page.getSize());
        response.setCurrentPage(page.getPageable().getPageNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        return response;
    }
}
