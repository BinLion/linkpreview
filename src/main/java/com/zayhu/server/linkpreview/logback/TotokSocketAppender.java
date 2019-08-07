package com.zayhu.server.linkpreview.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;

import java.util.concurrent.*;

/**
 * @author liaoyebin
 * @date 2019/8/6 11:35
 * @description
 */

public class TotokSocketAppender extends ch.qos.logback.classic.net.SocketAppender {
    private final ExecutorService exector = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS
            , new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());

    /**
     * 防止日志造成阻塞 ， 使用异步线程
     *
     * @param event
     */
    @Override
    protected void postProcessEvent(ILoggingEvent event) {
        CompletableFuture.runAsync(() -> super.postProcessEvent(event), exector);
    }

    @Override
    public void setContext(Context context) {
        context.putProperty("service_name", "link-preview-srv");
        super.setContext(context);
    }
}
