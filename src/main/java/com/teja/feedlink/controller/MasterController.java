package com.teja.feedlink.controller;

import com.teja.feedlink.model.MasterData;
import com.teja.feedlink.service.MasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/master")
public class MasterController {

    @Autowired
    MasterDataService masterDataService;

    @GetMapping
    public ResponseEntity<MasterData> getMasterData() {
        return new ResponseEntity<>(masterDataService.fetchMasterData(), HttpStatus.OK);
    }
}
