package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {

  @Override
  public void start(Future<Void> future) {
    super.start();

    //TODO
    //----
    String company = TraderUtils.pickACompany();
    int numberOfShares = TraderUtils.pickANumber();
    System.out.println("Java compulsive trader configured for company " + company + " and shares: " + numberOfShares);

    Future<PortfolioService> portfolioServiceFuture = Future.future();
    Future<MessageConsumer<JsonObject>> marketServiceFuture = Future.future();

    MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"), marketServiceFuture.completer());
    EventBusService.getProxy(discovery, PortfolioService.class, portfolioServiceFuture.completer());

    CompositeFuture.all(portfolioServiceFuture, marketServiceFuture).setHandler( ar -> {
      if (ar.failed()) {
        future.fail("One of the required service cannot be retrieved: " + ar.cause());
      } else {
        PortfolioService portfolioService = portfolioServiceFuture.result();
        MessageConsumer<JsonObject> marketConsumer = marketServiceFuture.result();

        marketConsumer.handler(message -> {
          JsonObject quote = message.body();
          TraderUtils.dumbTradingLogic(company, numberOfShares, portfolioService, quote);
        });
      }
    });

    // ----
  }


}
