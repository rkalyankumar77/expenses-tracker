package com.example.expenses_tracker.handler;

import com.example.expenses_tracker.eventbus.DeleteExpenseVerticle;
import com.example.expenses_tracker.model.Expenses;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpensesHandler {
  private final JDBCPool jdbcPool;
  private final Vertx vertx;

  public void addExpense(RoutingContext routingContext) {
    JsonObject expensesJson = routingContext.body().asJsonObject();
    log.info("Received expenses: {}", routingContext.body().asString());

    jdbcPool.query("CREATE TABLE IF NOT EXISTS expenses (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), description VARCHAR(255), amount DOUBLE, date DATE)")
      .execute()
      .onFailure(err -> {
        log.error("Failed to create expenses table", err);
        routingContext.response().setStatusCode(500).end();
      });

    // Convert expensesJson to Expenses object
    Expenses expenses = expensesJson.mapTo(Expenses.class);
    // Save the expenses object to the database
    jdbcPool.preparedQuery("INSERT INTO expenses (name, description, amount, date) VALUES (?, ?, ?, ?)")
      .execute(Tuple.of(expenses.getName(), expenses.getDescription(), expenses.getAmount(), expenses.getDate()))
      .onFailure(err -> {
        log.error("Failed to insert expenses", err);
        routingContext.response().setStatusCode(500).end();
      })
      .onSuccess(rows -> {
        Row lastInsertId = rows.property(JDBCPool.GENERATED_KEYS);
        expenses.setId(lastInsertId.getLong(0));
        routingContext.response().setStatusCode(201)
          .send(JsonObject.mapFrom(expenses).encode());
      });
  }

  public void getAllExpenses(RoutingContext ctx) {
    jdbcPool.query("SELECT * FROM expenses")
      .execute()
      .onFailure(err -> {
        log.error("Failed to get expenses", err);
        ctx.response().setStatusCode(500).end();
      })
      .onSuccess(rows -> {
        JsonArray expenses = new JsonArray();
        for (var row : rows) {
          expenses.add(row.toJson());
        }
        ctx.response().setStatusCode(200)
          .send(expenses.encode());
      });
  }

  public void getExpenseById(RoutingContext ctx) {
    Long id = Long.valueOf(ctx.pathParam("id").trim());
    jdbcPool.preparedQuery("SELECT * FROM expenses WHERE id = ?")
      .execute(Tuple.of(id))
      .onFailure(err -> {
        log.error("Failed to get expenses", err);
        ctx.response().setStatusCode(500).end();
      })
      .onSuccess(rows -> {
        if (rows.size() == 0) {
          ctx.response().setStatusCode(404).end();
        } else {
          ctx.response().setStatusCode(200)
            .send(rows.iterator().next().toJson().encode());
        }
      });
  }

  public void deleteExpenseById(RoutingContext ctx) {
    JsonObject expensesJson = new JsonObject();
    expensesJson.put("ID", Long.parseLong(ctx.pathParam("id")));
    log.info("Delete: {}", expensesJson.encodePrettily());
    vertx.eventBus().request(DeleteExpenseVerticle.EVENT_BUS_NAME, expensesJson, reply -> {
      if (reply.succeeded()) {
        log.info("Deleted expense with id: {}", ctx.pathParam("id"));
        ctx.response().setStatusCode(204).end();
      } else {
        ctx.response().setStatusCode(500).end();
      }
    });
  }
}
