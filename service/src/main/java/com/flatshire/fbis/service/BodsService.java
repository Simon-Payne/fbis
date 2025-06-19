package com.flatshire.fbis.service;

import com.flatshire.fbis.domain.BusInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalDateTime;
import java.util.List;

public interface BodsService {

    /**
     * Fetch real-time bus position coordinates from the BODS web services.
     * @param lineRef String the bus line reference for which to get the position data
     * @return Triple&lt;LocalDateTime, String, String&gt; a triple of recordedTime/latitude/longitude coordinates representing the
     * current position of the bus matching the line reference.
     */
    Triple<LocalDateTime, String, String> readPositionFromDataFeed(String lineRef);

    /**
     * Fetch buses operated by the supplied operator reference.
     * @param operatorRef String the operator reference on which to search
     * @return List&lt;{@link BusInfo} a list containing data for the buses run by the operator
     */
    List<BusInfo> readBusInfo(String operatorRef);

}
