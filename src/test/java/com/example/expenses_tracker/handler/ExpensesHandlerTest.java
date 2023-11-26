package com.example.expenses_tracker.handler;

import com.example.expenses_tracker.MainVerticle;
import com.example.expenses_tracker.model.Expenses;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
@Slf4j
class ExpensesHandlerTest {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void addExpense(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    final Expenses expenses = new Expenses(100L, "test", "test", 100.0, LocalDate.now());
    client.request(HttpMethod.POST, 8080, "localhost", "/expenses")
      .compose(req -> req.putHeader("content-type", "application/json").send(JsonObject.mapFrom(expenses).encode())
      .compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> {
        testContext.verify(() -> {
          JsonObject expensesJson = buffer.toJsonObject();
          assertEquals(expenses.getName(), expensesJson.getString("name"));
          assertEquals(expenses.getDescription(), expensesJson.getString("description"));
          assertEquals(expenses.getAmount(), expensesJson.getDouble("amount"));
          assertEquals(Arrays.asList(expenses.getDate().toString().split("-")).toString(), expensesJson.getString("date"));
          testContext.completeNow();
        });
      }));
  }

  @Test
  void getAllExpenses(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8080, "localhost", "/expenses")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> {
        testContext.verify(() -> {
          JsonArray expenses = buffer.toJsonArray();
          log.info("expenses: {}", expenses);
          assertTrue(expenses.size() > 0);
          testContext.completeNow();
        });
      }));
  }

  @Test
  void getExpenseById(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.GET, 8080, "localhost", "/expenses/2")
      .compose(req -> req.send().compose(HttpClientResponse::body))
      .onComplete(testContext.succeeding(buffer -> {
        testContext.verify(() -> {
          JsonObject expenses = buffer.toJsonObject();
          assertEquals(2L, expenses.getLong("ID"));
          testContext.completeNow();
        });
      }));
  }

  @Test
  void deleteExpenseById(Vertx vertx, VertxTestContext testContext) {
    HttpClient client = vertx.createHttpClient();
    client.request(HttpMethod.DELETE, 8080, "localhost", "/expenses/1")
      .compose(req-> req.send()).onComplete(testContext.succeeding(response -> {
        testContext.verify(() -> {
          assertEquals(204, response.statusCode());
          testContext.completeNow();
        });
      }));
  }
}
