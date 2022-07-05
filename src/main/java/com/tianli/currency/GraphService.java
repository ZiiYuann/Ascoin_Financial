package com.tianli.currency;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.tianli.tool.MapTool;
import org.mountcloud.graphql.GraphqlClient;
import org.mountcloud.graphql.request.query.DefaultGraphqlQuery;
import org.mountcloud.graphql.request.query.GraphqlQuery;
import org.mountcloud.graphql.request.result.ResultAttributtes;
import org.mountcloud.graphql.response.GraphqlResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class GraphService {

    public Long getGraphLastBlock(GraphqlClient graphqlClient) throws IOException {
        GraphqlQuery query = new DefaultGraphqlQuery("_meta");
        ResultAttributtes asset = new ResultAttributtes("block");
        asset.addResultAttributes("number");
        query.addResultAttributes(asset);
        String s = doQueryList(graphqlClient, query);
        JsonObject jsonObject = new Gson().fromJson(s, JsonObject.class);
        return jsonObject.getAsJsonObject("block").get("number").getAsLong();
    }



    public List<TransferGraphVO> getTransfer(GraphqlClient graphqlClient,Long startBlock, Long endBlock,List<String> addressList) throws IOException {
        GraphqlQuery query = new DefaultGraphqlQuery("transferLogs");
        query.getRequestParameter()
                .addObjectParameter("where", MapTool.Map().put("block_gte",startBlock).put("block_lt",endBlock).put("to_in",addressList))
                .addObjectParameter("orderBy", "block");
        query.addResultAttributes("id","value","from","to","block","transferTime","coinAddress");
        String json = doQueryList(graphqlClient, query);
        return new Gson().fromJson(json,new TypeToken<List<TransferGraphVO>>(){}.getType());
    }

    public List<TransferGraphVO> getTransferByFrom(GraphqlClient graphqlClient,Long startBlock, Long endBlock,String address) throws IOException {
        GraphqlQuery query = new DefaultGraphqlQuery("transferLogs");
        query.getRequestParameter()
                .addObjectParameter("where", MapTool.Map().put("block_gte",startBlock).put("block_lt",endBlock).put("from",address))
                .addObjectParameter("orderBy", "block");
        query.addResultAttributes("id","value","from","to","block","transferTime","coinAddress");
        String json = doQueryList(graphqlClient, query);
        return new Gson().fromJson(json,new TypeToken<List<TransferGraphVO>>(){}.getType());
    }

    public List<TransferGraphVO> getTransferByTo(GraphqlClient graphqlClient,Long startBlock, Long endBlock,String address) throws IOException {
        GraphqlQuery query = new DefaultGraphqlQuery("transferLogs");
        query.getRequestParameter()
                .addObjectParameter("where", MapTool.Map().put("block_gte",startBlock).put("block_lt",endBlock).put("to",address))
                .addObjectParameter("orderBy", "block");
        query.addResultAttributes("id","value","from","to","block","transferTime","coinAddress");
        String json = doQueryList(graphqlClient, query);
        return new Gson().fromJson(json,new TypeToken<List<TransferGraphVO>>(){}.getType());
    }

    private String doQueryList(GraphqlClient graphqlClient, GraphqlQuery graphqlQuery) throws IOException {
        String requestName = graphqlQuery.getRequestName();
        GraphqlResponse graphqlResponse = graphqlClient.doQuery(graphqlQuery);
        Map data = graphqlResponse.getData();
        Map data1 = (Map) data.get("data");
        return MapTool.json(data1.get(requestName));
    }
}
