package com.example.expenses_tracker;

import com.example.expenses_tracker.config.JdbcConfig;
import com.example.expenses_tracker.eventbus.DeleteExpenseVerticle;
import com.example.expenses_tracker.handler.ExpensesHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  static {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();

    final JdbcConfig jdbcConfig = new JdbcConfig(vertx);
    vertx.deployVerticle(new DeleteExpenseVerticle(jdbcConfig.getJdbcPool()));

    final ExpensesHandler expensesHandler = new ExpensesHandler(jdbcConfig.getJdbcPool(), vertx);

    Router router = Router.router(vertx);
    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler);

    router.post("/expenses").handler(expensesHandler::addExpense);
    router.get("/expenses").handler(expensesHandler::getAllExpenses);
    router.get("/expenses/:id").handler(expensesHandler::getExpenseById);
    router.delete("/expenses/:id").handler(expensesHandler::deleteExpenseById);

   server.requestHandler(router).listen(8080, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        log.info("HTTP server started on port 8080");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
