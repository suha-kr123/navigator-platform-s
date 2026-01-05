package com.nivasafinance.redash.client;

import com.nivasafinance.redash.config.RedashConfig;
import com.nivasafinance.redash.dto.RedashQueryResponse;
import com.nivasafinance.redash.dto.RedashQueryResultRequest;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "redash", url = "${redash.url}", configuration = RedashConfig.class)
public interface RedashClient {

    @PostMapping("/api/queries/{id}/results")
    RedashQueryResponse executeQuery(@PathVariable("id") Long queryId, @RequestBody RedashQueryResultRequest payload);

    @GetMapping("/api/jobs/{jobId}")
    RedashQueryResponse getJobStatus(@PathVariable("jobId") String jobId);

    @GetMapping("/api/query_results/{queryResultId}.{fileExtension}")
    Response downloadQueryResult(@PathVariable("queryResultId") String queryResultId, @PathVariable("fileExtension") String fileExtension);
}
