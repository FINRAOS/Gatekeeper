package org.finra.gatekeeper.common.services.eventlogging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestEventLogger{
    private final static Logger log = LoggerFactory.getLogger(RequestEventLogger.class);
    public RequestEventLogger(){}

    /**
 * Helper event to log Events to Splunk in Json Format.
 *
 * @param eventType
 * @param data
 */

    public static void logEventToJson(EventType eventType, Object data){
        ObjectMapper objectMapper=new ObjectMapper();
        try{
            log.info("{\"event\": \""+ eventType + "\", \"request\":"+ objectMapper.writeValueAsString(data)+"}");
        }catch(JsonProcessingException jpe){
             log.error("Could not log event in Json. Outputting standard log",jpe);
        }
    }
}
