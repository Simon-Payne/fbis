package com.flatshire.fbis.helpers;

import com.flatshire.fbis.domain.BusInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.time.LocalDateTime;
import java.util.List;

public interface BodsServiceHelper {

    /**
     * Fetch bus position data for a given line reference.
     * @param lineRef String
     * @return Triple&lt;LocalDateTime, String, String&gt; a triple of recordedTime/latitude/longitude coordinates representing the
     *      timed position of the bus matching the line reference.
     */
    Triple<LocalDateTime, String, String> fetchData(String lineRef);

    /**
     * Fetch buses operated by the referenced operatopr.
     * @param operatorRef String the operator reference
     * @return List&lt;{@link BusInfo}&gt; the buses operated by the operator
     */
    List<BusInfo> fetchBusInfo(String operatorRef);
}
