package com.flatshire.fbis.components;

import com.flatshire.fbis.domain.BusInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class FbisRestController {

    private final BodsService bodsService;

    @Autowired
    FbisRestController(BodsService bodsService) {
        this.bodsService = bodsService;
    }


    @GetMapping("/operators/{operatorRef}/buses")
    List<BusInfo> getBuses(@PathVariable String operatorRef) {
        if(!operatorRef.matches("^[a-zA-Z]+$")) {
            throw new OperatorNotSuppliedException("Operator reference must be a single word " +
                    "containing only alphabetical characters");
        }
        return bodsService.readBusInfo(operatorRef);
    }


}
