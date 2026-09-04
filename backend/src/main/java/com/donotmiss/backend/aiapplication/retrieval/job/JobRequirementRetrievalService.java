package com.donotmiss.backend.aiapplication.retrieval.job;

import com.donotmiss.backend.aiapplication.career.job.*;
import com.donotmiss.backend.aiapplication.trace.TraceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets; import java.security.*; import java.util.*;

/** Dedicated retrieval boundary: Job chunks + Job index only, with BM25 fallback when embeddings are disabled. */
@Service
public class JobRequirementRetrievalService {
    private final JobRequirementChunkRepository chunks; private final JobRequirementSearchIndexService index; private final TraceService trace;
    public JobRequirementRetrievalService(JobRequirementChunkRepository chunks,JobRequirementSearchIndexService index,TraceService trace){this.chunks=chunks;this.index=index;this.trace=trace;}
    @Transactional(readOnly=true) public List<JobRetrievalResult> retrieve(Long runId,Long versionId,String query,String section,String skillCode,int limit){
        int safeLimit=Math.min(Math.max(1,limit),30); List<RankedJobChunk> bm25=index.bm25Search(versionId,query,section,skillCode,safeLimit*3); List<RankedJobChunk> vector=index.vectorSearch(versionId,query,section,skillCode,safeLimit*3); List<RankedJobChunk> fused=ReciprocalRankFusion.fuse(bm25,vector,safeLimit);
        Map<Long,JobRequirementChunkEntity> byId=new HashMap<>();for(JobRequirementChunkEntity c:chunks.findByJobRequirementVersionIdOrderByPositionAsc(versionId))byId.put(c.getId(),c);
        List<JobRetrievalResult> result=new ArrayList<>();int rank=1;for(RankedJobChunk item:fused){JobRequirementChunkEntity chunk=byId.get(item.chunkId());if(chunk!=null){JobRetrievalResult row=new JobRetrievalResult(chunk.getId(),item.score(),rank++,chunk.getSection().name(),chunk.getContentHash(),"jobRequirementVersion:"+versionId+":position:"+chunk.getPosition());result.add(row);trace.recordArtifact(runId,null,"JOB_RETRIEVAL",Map.of("queryHash",hash(query),"chunkId",row.chunkId(),"section",row.section(),"rank",row.rank(),"score",row.score(),"mode",vector.isEmpty()?"BM25":"RRF"));}}
        return List.copyOf(result);
    }
    private String hash(String v){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((v==null?"":v).getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    public record JobRetrievalResult(Long chunkId,double score,int rank,String section,String contentHash,String sourceReference) { }
}
