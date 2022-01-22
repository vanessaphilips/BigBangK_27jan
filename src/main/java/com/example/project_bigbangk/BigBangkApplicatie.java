// Created by Deek
// Creation date 12/11/2021

package com.example.project_bigbangk;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.service.ClientFactory;
import com.example.project_bigbangk.service.priceHistoryUpdate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * defining rules glbal constants and initiating global services:
 * async priceHistoryUpdateTimer
 * async dataBase seeder
 * bigBank instance
 * @warning bigBangk static instance doe not contain a Wallet!
 * use getBigBangk() instead.
 * bigBangK static field is kept public for backwards compatability
 */
@Component
public class BigBangkApplicatie implements ApplicationListener<ContextRefreshedEvent> {

    public static final String CURRENT_CURRENCY = "EUR";
    public static final long DAYS_OF_PRICEHISTORY_CACHE =30 ;
    public static final int UPDATE_INTERVAL_PRICEUPDATESERVICE = 300000;//5min 300000
    private static final int NUMBER_OF_CLIENTS_TO_SEED = 3000;
    private static final int DELAY_PRICEHISTORYUPDATE = 3000;
    private static final int DELAY_DATABASES_SEEDING = 6000;
    public static final Bank bigBangk = new Bank("BigBangk", "BGBK", 0.01, 10000);
    public static Wallet prototypeWallet;
    private final PriceHistoryUpdateService priceHistoryUpdateService;
    private final ClientFactory clientFactory;
    private final Logger logger = LoggerFactory.getLogger(BigBangkApplicatie.class);


    public BigBangkApplicatie(PriceHistoryUpdateService priceHistoryUpdateService,
                              ClientFactory clientFactory) {
        super();
        logger.info("New BigBangkApplicatie");
        this.priceHistoryUpdateService = priceHistoryUpdateService;
        this.clientFactory = clientFactory;
    }

    @Override
    public  void onApplicationEvent(ContextRefreshedEvent event) {
        startPriceHistoryUpdateTimer();
        startDateBaseSeeding();
        setupBaseWallet();
    }

    private void setupBaseWallet(){
        Map<Asset, Double> assetMap = new HashMap<>();
        for (AssetCode_Name asset : EnumSet.allOf(AssetCode_Name.class)) {
            assetMap.put(new Asset(asset.getAssetCode(), asset.getAssetName()), 0.0);
        }
        prototypeWallet = new Wallet("none",bigBangk.getStartingcapital(), assetMap);
    }

    private void startPriceHistoryUpdateTimer() {
        Timer priceHistoryUpdateCallBack = new Timer(true);
        logger.info("priceHistoryUpdate in progress");
        priceHistoryUpdateCallBack.schedule(new UpdatePriceHisToryTask(), DELAY_PRICEHISTORYUPDATE, UPDATE_INTERVAL_PRICEUPDATESERVICE);
    }

    class UpdatePriceHisToryTask extends TimerTask {
        @Override
        public void run() {
            priceHistoryUpdateService.updatePriceHistory(CURRENT_CURRENCY);
        }
    }

    private void startDateBaseSeeding() {
        Timer seedDatabaseTimer = new Timer(true);
        seedDatabaseTimer.schedule(new SeedDatabase(), DELAY_DATABASES_SEEDING);
    }

    class SeedDatabase extends TimerTask {

        @Override
        public void run() {
            logger.info("Start database seeding? (Y/N): ");
            Scanner scanner = new Scanner(System.in);
            if (scanner.nextLine().equalsIgnoreCase("Y")) {
                clientFactory.seedDataBase(NUMBER_OF_CLIENTS_TO_SEED);
            }
        }
    }
}
