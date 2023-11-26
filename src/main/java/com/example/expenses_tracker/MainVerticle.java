package com.example.expenses_tracker;

import com.example.expenses_tracker.config.JdbcConfig;
import com.example.expenses_tracker.eventbus.DeleteExpenseVerticle;
import com.example.expenses_tracker.handler.ExpensesHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  private JDBCPool jdbcPool = null;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);

    DatabindCodec.mapper().registerModule(new JavaTimeModule());

    BodyHandler bodyHandler = BodyHandler.create();
    router.post().handler(bodyHandler);


    final JdbcConfig jdbcConfig = new JdbcConfig(vertx);
    final ExpensesHandler expensesHandler = new ExpensesHandler(jdbcConfig.getJdbcPool());

    vertx.deployVerticle(new DeleteExpenseVerticle(jdbcConfig.getJdbcPool()));


    router.post("/expenses").handler(ctx -> {
      expensesHandler.addExpense(ctx);
    });

    router.get("/expenses").handler(ctx->{
      expensesHandler.getAllExpenses(ctx);
    });

    router.get("/expenses/:id").handler(ctx->{
      jdbcPool = jdbcConfig.getJdbcPool();
      expensesHandler.getExpenseById(ctx);
    });

    router.delete("/expenses/:id").handler(ctx->{
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
    });

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
