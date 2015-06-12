package org.kabir.example.concurrency.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mvn wildfly:deploy
 *
 *
 * http://localhost:8080/example-concurrency/test?sleep=10
 * :suspend
 * http://localhost:8080/example-concurrency/test should not work
 *
 * @author Kabir Khan
 */
@WebServlet(urlPatterns = "/test",
        asyncSupported = true)
public class ConcurrencyServlet extends HttpServlet {

    static final AtomicInteger REQUEST = new AtomicInteger(0);

    /** Configured in the ee subsystem */
    @Resource
    private ManagedScheduledExecutorService executor;

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {
        AsyncContext asycnCtx
                = req.startAsync(req, resp);

        executor.execute(
                new LongRunningJob(
                        asycnCtx,
                        getMilliSeconds(req)));
    }

    private static class LongRunningJob implements Runnable {
        private final AsyncContext asyncCtx;
        private final int sleep;

        private LongRunningJob(AsyncContext asyncCtx, int sleep) {
            this.asyncCtx = asyncCtx;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(sleep);
                asyncCtx.getResponse().getWriter()
                        .write("Processed response " +
                                REQUEST.incrementAndGet() +
                                " sleeping " + sleep/1000 + "s");
                asyncCtx.complete();
            } catch (Exception e) {
                //Lazy exception handling
                e.printStackTrace();
            }
        }
    }

    private int getMilliSeconds(HttpServletRequest req) {
        String sleep = req.getParameter("sleep");
        if (sleep != null) {
            return Integer.valueOf(sleep) * 1000;
        } else {
            return 0;
        }

    }
}
