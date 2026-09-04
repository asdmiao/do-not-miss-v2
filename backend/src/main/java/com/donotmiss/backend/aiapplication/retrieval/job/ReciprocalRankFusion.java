package com.donotmiss.backend.aiapplication.retrieval.job;

import java.util.*;

/** Provider-neutral RRF: absent vector candidates simply contribute no score. */
public final class ReciprocalRankFusion {
    private ReciprocalRankFusion() { }
    public static List<RankedJobChunk> fuse(List<RankedJobChunk> bm25, List<RankedJobChunk> vector, int limit) {
        Map<Long,Double> scores=new LinkedHashMap<>(); add(scores,bm25,1.0); add(scores,vector,1.15);
        return scores.entrySet().stream().sorted(Map.Entry.<Long,Double>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(Math.max(1,limit)).map(e->new RankedJobChunk(e.getKey(),e.getValue())).toList();
    }
    private static void add(Map<Long,Double> scores,List<RankedJobChunk> hits,double weight){int rank=1;for(RankedJobChunk hit:hits==null?List.<RankedJobChunk>of():hits){if(hit!=null&&hit.chunkId()!=null)scores.merge(hit.chunkId(),weight/(60.0+rank),Double::sum);rank++;}}
}
