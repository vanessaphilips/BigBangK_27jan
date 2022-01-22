

package com.example.project_bigbangk.service.priceHistoryUpdate;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * This class talks to the CoinMarketCap.com api and get information about assets and converts it to PriceHistory
 *
 * @author Pieter Jan - Deek
 * Creation date 12/11/2021
 */
@Service
public class CoinMarketCapNegotiator implements ICryptoApiNegotiator {


    private final Logger logger = LoggerFactory.getLogger(CoinMarketCapNegotiator.class);
    private final static String API_KEY = "9a38f7d6-3288-491a-8a0d-0338079527bc";
    private final static String ASSET_DATA_URL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest";
    private final static String SERVER_INFO_URL = "https://pro-api.coinmarketcap.com/v1/key/info";
    private final static String ALLASSETS_URL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private final static int[] ASSET_IDS = new int[]{1, 1027, 1839, 825, 5426, 3408, 2010, 52, 6636, 74, 4172, 5805, 5994, 4687, 3635, 3890, 3717, 2, 7083, 1027, 4943};
    private final static int NUMBER_OF_ASSETS = 20;
    private static final int STATUS_OK = 200;
    private final CloseableHttpClient HTTPClIENT;

    private ICryptoApiSwitcher cryptoApiNegotiatorStrategy;

    public CoinMarketCapNegotiator(ICryptoApiSwitcher cryptoApiNegotiatorStrategy) {
        super();
        logger.info("New CoinMarketCapNegotiator");
        HTTPClIENT = HttpClients.createDefault();
        cryptoApiNegotiatorStrategy.addNegotiator(this);
    }

/**
 * checks if the server is online
 * @return true if online
 */
    @Override
    public boolean isAvailable() {
        String result = null;
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("id", getCoinIdsAsString()));
        parameters.add(new BasicNameValuePair("aux", "status"));
        try {
            HttpGet request = makeApiRequest(SERVER_INFO_URL, parameters);
            return getStatusCode(request) == STATUS_OK;
        } catch (IOException e) {
            System.out.println("Error: can not access content - " + e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println("Error: Invalid URL " + e.getMessage());
        }
        return true;
    }

    /**
     * retrieves the priceHistoryData
     * @param currency the currency of the prices returned
     * @return a list of PriceHistory data for all Assets
     */

    @Override
    public List<PriceHistory> getPriceHistory(String currency) {
        List<PriceHistory> priceHistories = null;
        String response_AssetListJSON = null;
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("id", getCoinIdsAsString()));
        parameters.add(new BasicNameValuePair("convert", currency));
        try {
            HttpGet request = makeApiRequest(ASSET_DATA_URL, parameters);
            response_AssetListJSON = makeAPiCall(request);
        } catch (IOException e) {
            logger.error("Error: can not access content - " + e.getMessage());
        } catch (URISyntaxException e) {
            logger.error("Error: Invalid URL " + e.getMessage());
        }
        if (response_AssetListJSON != null) {
            priceHistories = convertJSonToPriceHistory(response_AssetListJSON);
        }
        return priceHistories;
    }

    /**
     * @return a list of currentPrices for the first 20 most populair assets
     */
    public List<PriceHistory> getRangeOfCoins() {
        List<PriceHistory> priceHistories = null;
        String response_AssetListJSON = null;
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("limit", String.valueOf(NUMBER_OF_ASSETS)));
        parameters.add(new BasicNameValuePair("convert", "EUR"));
        try {
            HttpGet request = makeApiRequest(ALLASSETS_URL, parameters);
            response_AssetListJSON = makeAPiCall(request);
        } catch (IOException e) {
            logger.error("Error: cannont access content - " + e.getMessage());
        } catch (URISyntaxException e) {
            logger.error("Error: Invalid URL " + e.getMessage());
        }
        if (response_AssetListJSON != null) {
            priceHistories = convertJSonToPriceHistory(response_AssetListJSON);
        }
        return priceHistories;
    }

    private List<PriceHistory> convertJSonToPriceHistory(String result) {
        ObjectNode node = null;
        try {
            node = new ObjectMapper().readValue(result, ObjectNode.class);
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
        }
        List<PriceHistory> priceHistories = null;

        if (node != null && node.has("data")) {
            priceHistories = new ArrayList<>();
            for (JsonNode json : node.findValues("data")) {
                for (JsonNode coin : json) {
                    double price = Double.parseDouble(coin.get("quote").get("EUR").get("price").toString());
                    String assetCode = coin.get("symbol").textValue();
                    String assetName = coin.get("name").textValue();
                    PriceDate priceDate = new PriceDate(LocalDateTime.now(), price);
                    priceHistories.add(new PriceHistory(new ArrayList<>(List.of(priceDate)), new Asset(assetCode, assetName, price)));
                }
            }
        }
        return priceHistories;
    }

    private String getCoinIdsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (int assetCode : ASSET_IDS) {
            stringBuilder.append(assetCode);
            i++;
            if (i < ASSET_IDS.length) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private HttpGet makeApiRequest(String uri, List<NameValuePair> parameters)
            throws URISyntaxException, IOException {
        URIBuilder query = new URIBuilder(uri);
        query.addParameters(parameters);
        HttpGet request = new HttpGet(query.build());
        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader("X-CMC_PRO_API_KEY", API_KEY);
        return request;
    }


    private int getStatusCode(HttpGet request) throws URISyntaxException, IOException {
        CloseableHttpResponse response = HTTPClIENT.execute(request);
        int responseStatusCode;
        try {
            responseStatusCode = response.getStatusLine().getStatusCode();
        } finally {
            response.close();
        }
        return responseStatusCode;
    }

    private String makeAPiCall(HttpGet request) throws URISyntaxException, IOException {
        CloseableHttpResponse response = HTTPClIENT.execute(request);
        String response_AssetListJSON;
        try {
            HttpEntity entity = response.getEntity();
            response_AssetListJSON = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
        return response_AssetListJSON;
    }
}