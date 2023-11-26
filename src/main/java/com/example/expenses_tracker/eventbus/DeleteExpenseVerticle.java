package com.example.expenses_tracker.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeleteExpenseVerticle extends AbstractVerticle {
  public static final String EVENT_BUS_NAME = "com.example.expenses_tracker.delete";

  private final JDBCPool jdbcPool;
  @Override
  public void start() throws Exception {
    super.start();
    vertx.eventBus().consumer(EVENT_BUS_NAME, this::onMessage);
  }

  public <T> void onMessage(Message<T> message) {
    JsonObject expensesJson = (JsonObject) message.body();
    log.info("Received expense to delete: {}", expensesJson.encodePrettily());
    jdbcPool.preparedQuery("DELETE FROM expenses WHERE id = ?")
      .execute(
        Tuple.of(expensesJson.getLong("ID")))
      .onFailure(err -> {
        log.error("Failed to delete expenses", err);
        message.fail(500, err.getMessage());
      })
      .onSuccess(rows -> {
        message.reply("Deleted");
      });
  }
}
