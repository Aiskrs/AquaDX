package icu.samnyan.aqua.sega.ongeki.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import icu.samnyan.aqua.sega.ongeki.handler.BaseHandler;
import icu.samnyan.aqua.sega.util.jackson.BasicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@Component("OngekiGetGamePointHandler")
public class GetGamePointHandler implements BaseHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetGamePointHandler.class);

    private final BasicMapper mapper;

    @Autowired
    public GetGamePointHandler(BasicMapper mapper) {
        this.mapper = mapper;
    }


    @Override
    public String handle(Map<String, Object> request) throws JsonProcessingException {
        Boolean isAllGP = (Boolean) request.get("isAllGP");

        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("length", 0);
        resultMap.put("gamePointList", new List[]{});

        String json = mapper.write(resultMap);

        logger.info("Response: " + json);
        return json;
    }
}
