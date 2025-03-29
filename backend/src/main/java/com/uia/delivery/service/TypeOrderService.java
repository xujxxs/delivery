package com.uia.delivery.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uia.delivery.entity.subsidiary.TypeOrder;
import com.uia.delivery.exception.NullFieldException;
import com.uia.delivery.repository.TypeOrderRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TypeOrderService 
{
    private final TypeOrderRepository typeOrderRepository;

    public TypeOrderService(TypeOrderRepository typeOrderRepository)
    {
        this.typeOrderRepository = typeOrderRepository;
    }

    public List<TypeOrder> getTypesOrder()
    {
        return typeOrderRepository.findAll();
    }

    public TypeOrder safetySaveType(TypeOrder typeOrder)
    {
        log.debug("Safety save type orders from TypeOrder: {}", typeOrder);
        if(typeOrder == null)
        {
            log.error("Error: empty type order");
            throw new NullFieldException("Type order");
        }
        List<TypeOrder> savedTypeOrder = safetySaveTypes(List.of(typeOrder));
        log.debug("Safety saved TypeOrder: {}", savedTypeOrder.get(0));
        return savedTypeOrder.get(0);
    }

    public List<TypeOrder> safetySaveTypes(List<TypeOrder> typesOrder)
    {
        log.debug("Safety save type orders from List<TypeOrder>: {}", typesOrder);
        if(typesOrder == null)
            throw new NullFieldException("List<TypeOrder>");
        
        List<TypeOrder> ans = new ArrayList<>();
        for(TypeOrder typeOrder : typesOrder)
        {
            Optional<TypeOrder> opTypeOrder = typeOrderRepository.findByType(typeOrder.getType());
            if(opTypeOrder.isPresent())
            {
                log.debug("Type order: {}, already exsist", typeOrder);
                ans.add(opTypeOrder.get());
            } 
            else {
                log.debug("Type order: {}, not exsist. Creating", typeOrder);
                ans.add(typeOrderRepository.save(typeOrder));
            }
        }

        log.debug("Safety saved List<TypeOrder>: {}", ans);
        return ans;
    } 
}
