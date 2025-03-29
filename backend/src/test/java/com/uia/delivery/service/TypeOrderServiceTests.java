package com.uia.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uia.delivery.entity.subsidiary.TypeOrder;
import com.uia.delivery.exception.NullFieldException;
import com.uia.delivery.repository.TypeOrderRepository;

@ExtendWith(MockitoExtension.class)
class TypeOrderServiceTests 
{
    @Mock
    private TypeOrderRepository typeOrderRepository;

    @InjectMocks
    private TypeOrderService typeOrderService;

    @Test
    void getTypesOrder_ReturnsListTypeOrder()
    {
        when(typeOrderRepository.findAll()).thenReturn(List.of(new TypeOrder(1L, "type1")));

        List<TypeOrder> result = typeOrderService.getTypesOrder();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void safetySaveType_ReturnsTypeOrder()
    {
        TypeOrder typeOrder = new TypeOrder(1L, "type1");

        when(typeOrderRepository.findByType(typeOrder.getType())).thenReturn(Optional.of(typeOrder));

        TypeOrder result = typeOrderService.safetySaveType(typeOrder);
        assertNotNull(result);
        assertEquals(typeOrder.getType(), result.getType());
    }

    @Test
    void safetySaveType_ThrowsNullFieldException()
    {
        assertThrows(NullFieldException.class, () -> typeOrderService.safetySaveType(null));
    }

    @Test
    void safetySaveTypes_ReturnsTypeOrder()
    {
        List<TypeOrder> listTypeOrders = List.of(
            new TypeOrder(1L, "type1"),
            new TypeOrder(2L, "type2")
        );

        when(typeOrderRepository.findByType(listTypeOrders.get(0).getType())).thenReturn(Optional.of(listTypeOrders.get(0)));
        when(typeOrderRepository.findByType(listTypeOrders.get(1).getType())).thenReturn(Optional.empty());
        when(typeOrderRepository.save(listTypeOrders.get(1))).thenAnswer(invocation -> invocation.getArgument(0));

        List<TypeOrder> result = typeOrderService.safetySaveTypes(listTypeOrders);
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(typeOrderRepository, times(2)).findByType(any());
        verify(typeOrderRepository, times(1)).save(any());
    }

    @Test
    void safetySaveTypes_ThrowsNullFieldException()
    {
        assertThrows(NullFieldException.class, () -> typeOrderService.safetySaveTypes(null));
    }
}
