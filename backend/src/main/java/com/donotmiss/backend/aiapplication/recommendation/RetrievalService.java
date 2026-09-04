package com.donotmiss.backend.aiapplication.recommendation;

import java.util.List;

/** Adapter boundary over the current HybridEventRetrievalService and future JD retrieval. */
public interface RetrievalService {
    List<RetrievalDocument> retrieveEvents(EventRetrievalQuery query);
}
