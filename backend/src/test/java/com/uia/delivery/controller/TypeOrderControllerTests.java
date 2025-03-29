package com.uia.delivery.controller;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.uia.delivery.entity.subsidiary.TypeOrder;
import com.uia.delivery.service.TypeOrderService;

@ExtendWith(MockitoExtension.class)
class TypeOrderControllerTests 
{
    private MockMvc mockMvc;

    @Mock
    private TypeOrderService typeOrderService;

    @InjectMocks
    private TypeOrderController typeOrderController;

    @BeforeEach
    void setUp()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(typeOrderController).build();
    }

    @Test
    void getTypesOrder_ReturnsTypesOrder() throws Exception
    {
        List<TypeOrder> lto = List.of(new TypeOrder(1L, "type1"), new TypeOrder(2L, "type2"));

        when(typeOrderService.getTypesOrder()).thenReturn(lto);

        mockMvc.perform(get("/api/type-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(lto.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("type1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].type").value("type2"));
    }
}
